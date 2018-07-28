package org.apache.cassandra.cli;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;
import com.google.common.base.Charsets;
import org.antlr.runtime.tree.Tree;
import org.apache.cassandra.auth.SimpleAuthenticator;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.db.ColumnFamilyStoreMBean;
import org.apache.cassandra.db.CompactionManagerMBean;
import org.apache.cassandra.db.marshal.*;
import org.apache.cassandra.locator.SimpleSnitch;
import org.apache.cassandra.thrift.*;
import org.apache.cassandra.tools.NodeProbe;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.UUIDGen;
import org.apache.thrift.TBaseHelper;
import org.apache.thrift.TException;
import org.safehaus.uuid.UUIDGenerator;
public class CliClient extends CliUserHelp
{
    public enum Function
    {
        BYTES       (BytesType.instance),
        INTEGER     (IntegerType.instance),
        LONG        (LongType.instance),
        LEXICALUUID (LexicalUUIDType.instance),
        TIMEUUID    (TimeUUIDType.instance),
        UTF8        (UTF8Type.instance),
        ASCII       (AsciiType.instance);
        private AbstractType validator;
        Function(AbstractType validator)
        {
            this.validator = validator;  
        }
        public AbstractType getValidator()
        {
            return this.validator;
        }
        public static String getFunctionNames()
        {
            Function[] functions = Function.values();
            StringBuilder functionNames = new StringBuilder();
            for (int i = 0; i < functions.length; i++)
            {
                StringBuilder currentName = new StringBuilder(functions[i].name().toLowerCase());
                functionNames.append(currentName.append(((i != functions.length-1) ? ", " : ".")));
            }
            return functionNames.toString();
        }
    }
    private enum AddKeyspaceArgument {
        REPLICATION_FACTOR,
        PLACEMENT_STRATEGY,
        STRATEGY_OPTIONS
    }
    private static final String DEFAULT_PLACEMENT_STRATEGY = "org.apache.cassandra.locator.NetworkTopologyStrategy";
    private Cassandra.Client thriftClient = null;
    private CliSessionState sessionState  = null;
    private String keySpace = null;
    private String username = null;
    private Map<String, KsDef> keyspacesMap = new HashMap<String, KsDef>();
    private Map<String, AbstractType> cfKeysComparators;
    public CliClient(CliSessionState cliSessionState, Cassandra.Client thriftClient)
    {
        this.sessionState = cliSessionState;
        this.thriftClient = thriftClient;
        this.cfKeysComparators = new HashMap<String, AbstractType>();
    }
    public void executeCLIStatement(String statement)
    {
        Tree tree = CliCompiler.compileQuery(statement);
        try
        {
            switch (tree.getType())
            {
                case CliParser.NODE_EXIT:
                    cleanupAndExit();
                    break;
                case CliParser.NODE_THRIFT_GET:
                    executeGet(tree);
                    break;
                case CliParser.NODE_THRIFT_GET_WITH_CONDITIONS:
                    executeGetWithConditions(tree);
                    break;
                case CliParser.NODE_HELP:
                    printCmdHelp(tree, sessionState);
                    break;
                case CliParser.NODE_THRIFT_SET:
                    executeSet(tree);
                    break;
                case CliParser.NODE_THRIFT_DEL:
                    executeDelete(tree);
                    break;
                case CliParser.NODE_THRIFT_COUNT:
                    executeCount(tree);
                    break;
                case CliParser.NODE_ADD_KEYSPACE:
                    executeAddKeySpace(tree.getChild(0));
                    break;
                case CliParser.NODE_ADD_COLUMN_FAMILY:
                    executeAddColumnFamily(tree.getChild(0));
                    break;
                case CliParser.NODE_UPDATE_KEYSPACE:
                    executeUpdateKeySpace(tree.getChild(0));
                    break;
                case CliParser.NODE_UPDATE_COLUMN_FAMILY:
                    executeUpdateColumnFamily(tree.getChild(0));
                    break;
                case CliParser.NODE_DEL_COLUMN_FAMILY:
                    executeDelColumnFamily(tree);
                    break;
                case CliParser.NODE_DEL_KEYSPACE:
                    executeDelKeySpace(tree);
                    break;
                case CliParser.NODE_SHOW_CLUSTER_NAME:
                    executeShowClusterName();
                    break;
                case CliParser.NODE_SHOW_VERSION:
                    executeShowVersion();
                    break;
                case CliParser.NODE_SHOW_KEYSPACES:
                    executeShowKeySpaces();
                    break;
                case CliParser.NODE_DESCRIBE_TABLE:
                    executeDescribeKeySpace(tree);
                    break;
                case CliParser.NODE_USE_TABLE:
                    executeUseKeySpace(tree);
                    break;
                case CliParser.NODE_CONNECT:
                    executeConnect(tree);
                    break;
                case CliParser.NODE_LIST:
                    executeList(tree);
                    break;
                case CliParser.NODE_TRUNCATE:
                    executeTruncate(tree.getChild(0).getText());
                    break;
                case CliParser.NODE_ASSUME:
                    executeAssumeStatement(tree);
                    break;
                case CliParser.NODE_NO_OP:
                    break;
                default:
                    sessionState.err.println("Invalid Statement (Type: " + tree.getType() + ")");
                    if (sessionState.batch)
                        System.exit(2);
                    break;
            }
        }
        catch (InvalidRequestException e)
        {
            throw new RuntimeException(e.getWhy());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }
    private void cleanupAndExit()
    {
        CliMain.disconnect();
        System.exit(0);
    }
    public KsDef getKSMetaData(String keyspace)
            throws NotFoundException, InvalidRequestException, TException
    {
        if (!(keyspacesMap.containsKey(keyspace)))
            keyspacesMap.put(keyspace, thriftClient.describe_keyspace(keyspace));
        return keyspacesMap.get(keyspace);
    }
    private void executeCount(Tree statement)
            throws TException, InvalidRequestException, UnavailableException, TimedOutException
    {
        if (!CliMain.isConnected() || !hasKeySpace())
            return;
        Tree columnFamilySpec = statement.getChild(0);
        String key = CliCompiler.getKey(columnFamilySpec);
        String columnFamily = CliCompiler.getColumnFamily(columnFamilySpec, keyspacesMap.get(keySpace).cf_defs);
        int columnSpecCnt = CliCompiler.numColumnSpecifiers(columnFamilySpec);
        ColumnParent colParent = new ColumnParent(columnFamily).setSuper_column((ByteBuffer) null);
        if (columnSpecCnt != 0)
        {
            byte[] superColumn = columnNameAsByteArray(CliCompiler.getColumn(columnFamilySpec, 0), columnFamily);
            colParent = new ColumnParent(columnFamily).setSuper_column(superColumn);
        }
        SliceRange range = new SliceRange(ByteBufferUtil.EMPTY_BYTE_BUFFER, ByteBufferUtil.EMPTY_BYTE_BUFFER, false, Integer.MAX_VALUE);
        SlicePredicate predicate = new SlicePredicate().setColumn_names(null).setSlice_range(range);
        int count = thriftClient.get_count(ByteBuffer.wrap(key.getBytes(Charsets.UTF_8)), colParent, predicate, ConsistencyLevel.ONE);
        sessionState.out.printf("%d columns%n", count);
    }
    private void executeDelete(Tree statement) 
            throws TException, InvalidRequestException, UnavailableException, TimedOutException
    {
        if (!CliMain.isConnected() || !hasKeySpace())
            return;
        Tree columnFamilySpec = statement.getChild(0);
        String key = CliCompiler.getKey(columnFamilySpec);
        String columnFamily = CliCompiler.getColumnFamily(columnFamilySpec, keyspacesMap.get(keySpace).cf_defs);
        int columnSpecCnt = CliCompiler.numColumnSpecifiers(columnFamilySpec);
        byte[] superColumnName = null;
        byte[] columnName = null;
        CfDef cfDef = getCfDef(columnFamily);
        boolean isSuper = cfDef.column_type.equals("Super");
        if ((columnSpecCnt < 0) || (columnSpecCnt > 2))
        {
            sessionState.out.println("Invalid row, super column, or column specification.");
            return;
        }
        if (columnSpecCnt == 1)
        {
            if (isSuper)
                superColumnName = columnNameAsByteArray(CliCompiler.getColumn(columnFamilySpec, 0), cfDef);
            else
                columnName = columnNameAsByteArray(CliCompiler.getColumn(columnFamilySpec, 0), cfDef);
        }
        else if (columnSpecCnt == 2)
        {
            superColumnName = columnNameAsByteArray(CliCompiler.getColumn(columnFamilySpec, 0), cfDef);
            columnName = subColumnNameAsByteArray(CliCompiler.getColumn(columnFamilySpec, 1), cfDef);
        }
        ColumnPath path = new ColumnPath(columnFamily);
        if (superColumnName != null)
            path.setSuper_column(superColumnName);
        if (columnName != null)
            path.setColumn(columnName);
        thriftClient.remove(ByteBuffer.wrap(key.getBytes(Charsets.UTF_8)), path,
                             FBUtilities.timestampMicros(), ConsistencyLevel.ONE);
        sessionState.out.println(String.format("%s removed.", (columnSpecCnt == 0) ? "row" : "column"));
    }
    private void doSlice(String keyspace, ByteBuffer key, String columnFamily, byte[] superColumnName)
            throws InvalidRequestException, UnavailableException, TimedOutException, TException, IllegalAccessException, NotFoundException, InstantiationException, NoSuchFieldException
    {
        ColumnParent parent = new ColumnParent(columnFamily);
        if(superColumnName != null)
            parent.setSuper_column(superColumnName);
        SliceRange range = new SliceRange(ByteBufferUtil.EMPTY_BYTE_BUFFER, ByteBufferUtil.EMPTY_BYTE_BUFFER, false, 1000000);
        List<ColumnOrSuperColumn> columns = thriftClient.get_slice(key, parent, new SlicePredicate().setColumn_names(null).setSlice_range(range), ConsistencyLevel.ONE);
        AbstractType validator;
        CfDef cfDef = getCfDef(columnFamily);
        for (ColumnOrSuperColumn cosc : columns)
        {
            if (cosc.isSetSuper_column())
            {
                SuperColumn superColumn = cosc.super_column;
                sessionState.out.printf("=> (super_column=%s,", formatSuperColumnName(keyspace, columnFamily, superColumn));
                for (Column col : superColumn.getColumns())
                {
                    validator = getValidatorForValue(cfDef, col.getName());
                    sessionState.out.printf("%n     (column=%s, value=%s, timestamp=%d%s)", formatSubcolumnName(keyspace, columnFamily, col),
                                                    validator.getString(col.value), col.timestamp,
                                                    col.isSetTtl() ? String.format(", ttl=%d", col.getTtl()) : "");
                }
                sessionState.out.println(")");
            }
            else
            {
                Column column = cosc.column;
                validator = getValidatorForValue(cfDef, column.getName());
                sessionState.out.printf("=> (column=%s, value=%s, timestamp=%d%s)%n", formatColumnName(keyspace, columnFamily, column),
                                                validator.getString(column.value), column.timestamp,
                                                column.isSetTtl() ? String.format(", ttl=%d", column.getTtl()) : "");
            }
        }
        sessionState.out.println("Returned " + columns.size() + " results.");
    }
    private AbstractType getFormatTypeForColumn(String compareWith)
    {
        Function function;
        try
        {
            function = Function.valueOf(compareWith.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            try
            {
                return FBUtilities.getComparator(compareWith);
            }
            catch (ConfigurationException ce)
            {
                StringBuilder errorMessage = new StringBuilder("Unknown comparator '" + compareWith + "'. ");
                errorMessage.append("Available functions: ");
                throw new RuntimeException(errorMessage.append(Function.getFunctionNames()).toString());
            }
        }
        return function.getValidator();
    }
    private void executeGet(Tree statement)
            throws TException, NotFoundException, InvalidRequestException, UnavailableException, TimedOutException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchFieldException
    {
        if (!CliMain.isConnected() || !hasKeySpace())
            return;
        Tree columnFamilySpec = statement.getChild(0);
        String columnFamily = CliCompiler.getColumnFamily(columnFamilySpec, keyspacesMap.get(keySpace).cf_defs);
        ByteBuffer key = getKeyAsBytes(columnFamily, columnFamilySpec.getChild(1));
        int columnSpecCnt = CliCompiler.numColumnSpecifiers(columnFamilySpec);
        CfDef cfDef = getCfDef(columnFamily);
        boolean isSuper = cfDef.column_type.equals("Super");
        byte[] superColumnName = null;
        ByteBuffer columnName;
        if (columnSpecCnt == 0)
        {
            doSlice(keySpace, key, columnFamily, superColumnName);
            return;
        }
        else if (columnSpecCnt == 1)
        {
            columnName = getColumnName(columnFamily, columnFamilySpec.getChild(2));
            if (isSuper)
            {
                superColumnName = columnName.array();
                doSlice(keySpace, key, columnFamily, superColumnName);
                return;
            }
        }
        else if (columnSpecCnt == 2)
        {
            superColumnName = getColumnName(columnFamily, columnFamilySpec.getChild(2)).array();
            columnName = getSubColumnName(columnFamily, columnFamilySpec.getChild(3));
        }
        else
        {
            sessionState.out.println("Invalid row, super column, or column specification.");
            return;
        }
        AbstractType validator = getValidatorForValue(cfDef, TBaseHelper.byteBufferToByteArray(columnName));
        ColumnPath path = new ColumnPath(columnFamily);
        if(superColumnName != null) path.setSuper_column(superColumnName);
        path.setColumn(columnName);
        Column column;
        try
        {
            column = thriftClient.get(key, path, ConsistencyLevel.ONE).column;
        }
        catch (NotFoundException e)
        {
            sessionState.out.println("Value was not found");
            return;
        }
        byte[] columnValue = column.getValue();       
        String valueAsString;
        if (statement.getChildCount() == 2)
        {
            Tree typeTree = statement.getChild(1).getChild(0);
            String typeName = CliUtils.unescapeSQLString(typeTree.getText());
            AbstractType valueValidator = getFormatTypeForColumn(typeName);
            valueAsString = valueValidator.getString(ByteBuffer.wrap(columnValue));
            updateColumnMetaData(cfDef, columnName, valueValidator.getClass().getName());
        }
        else
        {
            valueAsString = (validator == null) ? new String(columnValue, Charsets.UTF_8) : validator.getString(ByteBuffer.wrap(columnValue));
        }
        sessionState.out.printf("=> (column=%s, value=%s, timestamp=%d%s)%n",
                                formatColumnName(keySpace, columnFamily, column), valueAsString, column.timestamp,
                                column.isSetTtl() ? String.format(", ttl=%d", column.getTtl()) : "");
    }
    private void executeGetWithConditions(Tree statement)
    {
        if (!CliMain.isConnected() || !hasKeySpace())
            return;
        IndexClause clause = new IndexClause();
        String columnFamily = CliCompiler.getColumnFamily(statement, keyspacesMap.get(keySpace).cf_defs);
        Tree conditions = statement.getChild(1);
        CfDef columnFamilyDef = getCfDef(columnFamily);
        SlicePredicate predicate = new SlicePredicate();
        SliceRange sliceRange = new SliceRange();
        sliceRange.setStart(new byte[0]).setFinish(new byte[0]);
        predicate.setSlice_range(sliceRange);
        for (int i = 0; i < conditions.getChildCount(); i++)
        {
            Tree condition = conditions.getChild(i);
            String operator = condition.getChild(0).getText();
            String columnNameString  = CliUtils.unescapeSQLString(condition.getChild(1).getText());
            Tree valueTree = condition.getChild(2);
            try
            {
                ByteBuffer value;
                ByteBuffer columnName = columnNameAsBytes(columnNameString, columnFamily);
                if (valueTree.getType() == CliParser.FUNCTION_CALL)
                {
                    value = convertValueByFunction(valueTree, columnFamilyDef, columnName);
                }
                else
                {
                    String valueString = CliUtils.unescapeSQLString(valueTree.getText());
                    value = columnValueAsBytes(columnName, columnFamily, valueString);
                }
                IndexOperator idxOperator = CliUtils.getIndexOperator(operator);
                clause.addToExpressions(new IndexExpression(columnName, idxOperator, value));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.getMessage());
            }
        }
        List<KeySlice> slices;
        clause.setStart_key(new byte[] {});
        if (statement.getChildCount() == 3)
        {
            Tree limitNode = statement.getChild(2);
            int limitValue = Integer.parseInt(limitNode.getChild(0).getText());
            if (limitValue == 0)
            {
                throw new IllegalArgumentException("LIMIT should be greater than zero.");
            }
            clause.setCount(limitValue);    
        }
        try
        {
            ColumnParent parent = new ColumnParent(columnFamily);
            slices = thriftClient.get_indexed_slices(parent, clause, predicate, ConsistencyLevel.ONE);
            printSliceList(columnFamilyDef, slices);
        }
        catch (InvalidRequestException e)
        {
            throw new RuntimeException(e.getWhy());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }
    private void executeSet(Tree statement)
        throws TException, InvalidRequestException, UnavailableException, TimedOutException, NoSuchFieldException, InstantiationException, IllegalAccessException
    {
        if (!CliMain.isConnected() || !hasKeySpace())
            return;
        Tree columnFamilySpec = statement.getChild(0);
        Tree keyTree = columnFamilySpec.getChild(1); 
        String columnFamily = CliCompiler.getColumnFamily(columnFamilySpec, keyspacesMap.get(keySpace).cf_defs);
        CfDef cfDef = getCfDef(columnFamily);
        int columnSpecCnt = CliCompiler.numColumnSpecifiers(columnFamilySpec);
        String value = CliUtils.unescapeSQLString(statement.getChild(1).getText());
        Tree valueTree = statement.getChild(1);
        byte[] superColumnName = null;
        ByteBuffer columnName;
        if (columnSpecCnt == 0)
        {
            sessionState.err.println("No column name specified, (type 'help' or '?' for help on syntax).");
            return;
        }
        else if (columnSpecCnt == 1)
        {
            if (cfDef.column_type.equals("Super"))
            {
                sessionState.out.println("Column family " + columnFamily + " may only contain SuperColumns");
                return;
            }
            columnName = getColumnName(columnFamily, columnFamilySpec.getChild(2));
        }
        else
        {
            assert (columnSpecCnt == 2) : "serious parsing error (this is a bug).";
            superColumnName = getColumnName(columnFamily, columnFamilySpec.getChild(2)).array();
            columnName = getSubColumnName(columnFamily, columnFamilySpec.getChild(3));
        }
        ByteBuffer columnValueInBytes;
        switch (valueTree.getType())
        {
        case CliParser.FUNCTION_CALL:
            columnValueInBytes = convertValueByFunction(valueTree, cfDef, columnName, true);
            break;
        default:
            columnValueInBytes = columnValueAsBytes(columnName, columnFamily, value);
        }
        ColumnParent parent = new ColumnParent(columnFamily);
        if(superColumnName != null)
            parent.setSuper_column(superColumnName);
        Column columnToInsert = new Column(columnName, columnValueInBytes, FBUtilities.timestampMicros());
        if (statement.getChildCount() == 3)
        {
            String ttl = statement.getChild(2).getText();
            try
            {
                columnToInsert.setTtl(Integer.parseInt(ttl));
            }
            catch (NumberFormatException e)
            {
                sessionState.err.println(String.format("TTL '%s' is invalid, should be a positive integer.", ttl));
                return;
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.getMessage());
            }
        }
        thriftClient.insert(getKeyAsBytes(columnFamily, keyTree), parent, columnToInsert, ConsistencyLevel.ONE);
        sessionState.out.println("Value inserted.");
    }
    private void executeShowClusterName() throws TException
    {
        if (!CliMain.isConnected())
            return;
        sessionState.out.println(thriftClient.describe_cluster_name());
    }
    private void executeAddKeySpace(Tree statement)
    {
        if (!CliMain.isConnected())
            return;
        String keyspaceName = statement.getChild(0).getText();
        KsDef ksDef = new KsDef(keyspaceName, DEFAULT_PLACEMENT_STRATEGY, 1, new LinkedList<CfDef>());
        try
        {
            sessionState.out.println(thriftClient.system_add_keyspace(updateKsDefAttributes(statement, ksDef)));
            keyspacesMap.put(keyspaceName, thriftClient.describe_keyspace(keyspaceName));
        }
        catch (InvalidRequestException e)
        {
            throw new RuntimeException(e.getWhy());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    private void executeAddColumnFamily(Tree statement)
    {
        if (!CliMain.isConnected() || !hasKeySpace())
            return;
        CfDef cfDef = new CfDef(keySpace, statement.getChild(0).getText());
        try
        {
            sessionState.out.println(thriftClient.system_add_column_family(updateCfDefAttributes(statement, cfDef)));
            keyspacesMap.put(keySpace, thriftClient.describe_keyspace(keySpace));
        }
        catch (InvalidRequestException e)
        {
            throw new RuntimeException(e.getWhy());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    private void executeUpdateKeySpace(Tree statement)
    {
        if (!CliMain.isConnected())
            return;
        try
        {
            String keyspaceName = CliCompiler.getKeySpace(statement, thriftClient.describe_keyspaces());
            KsDef currentKsDef = getKSMetaData(keyspaceName);
            KsDef updatedKsDef = updateKsDefAttributes(statement, currentKsDef);
            sessionState.out.println(thriftClient.system_update_keyspace(updatedKsDef));
            keyspacesMap.put(keyspaceName, thriftClient.describe_keyspace(keyspaceName));
        }
        catch (InvalidRequestException e)
        {
            throw new RuntimeException(e.getWhy());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    private void executeUpdateColumnFamily(Tree statement)
    {
        if (!CliMain.isConnected() || !hasKeySpace())
            return;
        String cfName = CliCompiler.getColumnFamily(statement, keyspacesMap.get(keySpace).cf_defs);
        CfDef cfDef = getCfDef(cfName);
        try
        {
            sessionState.out.println(thriftClient.system_update_column_family(updateCfDefAttributes(statement, cfDef)));
            keyspacesMap.put(keySpace, thriftClient.describe_keyspace(keySpace));
        }
        catch (InvalidRequestException e)
        {
            throw new RuntimeException(e.getWhy());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    private KsDef updateKsDefAttributes(Tree statement, KsDef ksDefToUpdate)
    {
        KsDef ksDef = new KsDef(ksDefToUpdate);
        ksDef.setCf_defs(new LinkedList<CfDef>());
        for(int i = 1; i < statement.getChildCount(); i += 2)
        {
            String currentStatement = statement.getChild(i).getText().toUpperCase();
            AddKeyspaceArgument mArgument = AddKeyspaceArgument.valueOf(currentStatement);
            String mValue = statement.getChild(i + 1).getText();
            switch(mArgument)
            {
            case PLACEMENT_STRATEGY: 
                ksDef.setStrategy_class(CliUtils.unescapeSQLString(mValue));
                break;
            case REPLICATION_FACTOR:
                ksDef.setReplication_factor(Integer.parseInt(mValue));
                break;
            case STRATEGY_OPTIONS:
                ksDef.setStrategy_options(getStrategyOptionsFromTree(statement.getChild(i + 1)));
                break;
            default:
                assert(false);
            }
        }
        if (ksDef.getStrategy_class().contains(".NetworkTopologyStrategy"))
        {
            Map<String, String> currentStrategyOptions = ksDef.getStrategy_options();
            if (currentStrategyOptions == null || currentStrategyOptions.isEmpty())
            {
                SimpleSnitch snitch = new SimpleSnitch();
                Map<String, String> options = new HashMap<String, String>();
                try
                {
                    options.put(snitch.getDatacenter(InetAddress.getLocalHost()), "1");
                }
                catch (UnknownHostException e)
                {
                    throw new RuntimeException(e.getMessage());
                }
                ksDef.setStrategy_options(options);
            }
        }
        return ksDef;
    }
    private CfDef updateCfDefAttributes(Tree statement, CfDef cfDefToUpdate)
    {
        CfDef cfDef = new CfDef(cfDefToUpdate);
        for (int i = 1; i < statement.getChildCount(); i += 2)
        {
            String currentArgument = statement.getChild(i).getText().toUpperCase();
            ColumnFamilyArgument mArgument = ColumnFamilyArgument.valueOf(currentArgument);
            String mValue = statement.getChild(i + 1).getText();
            switch(mArgument)
            {
            case COLUMN_TYPE:
                cfDef.setColumn_type(CliUtils.unescapeSQLString(mValue));
                break;
            case COMPARATOR:
                cfDef.setComparator_type(CliUtils.unescapeSQLString(mValue));
                break;
            case SUBCOMPARATOR:
                cfDef.setSubcomparator_type(CliUtils.unescapeSQLString(mValue));
                break;
            case COMMENT:
                cfDef.setComment(CliUtils.unescapeSQLString(mValue));
                break;
            case ROWS_CACHED:
                cfDef.setRow_cache_size(Double.parseDouble(mValue));
                break;
            case KEYS_CACHED:
                cfDef.setKey_cache_size(Double.parseDouble(mValue));
                break;
            case READ_REPAIR_CHANCE:
                cfDef.setRead_repair_chance(Double.parseDouble(mValue));
                break;
            case GC_GRACE:
                cfDef.setGc_grace_seconds(Integer.parseInt(mValue));
                break;
            case COLUMN_METADATA:
                Tree arrayOfMetaAttributes = statement.getChild(i + 1);
                if (!arrayOfMetaAttributes.getText().equals("ARRAY"))
                    throw new RuntimeException("'column_metadata' format - [{ k:v, k:v, ..}, { ... }, ...]");
                cfDef.setColumn_metadata(getCFColumnMetaFromTree(cfDef, arrayOfMetaAttributes));
                break;
            case MEMTABLE_OPERATIONS:
                cfDef.setMemtable_operations_in_millions(Double.parseDouble(mValue));
                break;
            case MEMTABLE_FLUSH_AFTER:
                cfDef.setMemtable_flush_after_mins(Integer.parseInt(mValue));
                break;
            case MEMTABLE_THROUGHPUT:
                cfDef.setMemtable_throughput_in_mb(Integer.parseInt(mValue));
                break;
            case ROW_CACHE_SAVE_PERIOD:
                cfDef.setRow_cache_save_period_in_seconds(Integer.parseInt(mValue));
                break;
            case KEY_CACHE_SAVE_PERIOD:
                cfDef.setKey_cache_save_period_in_seconds(Integer.parseInt(mValue));
                break;
            case DEFAULT_VALIDATION_CLASS:
                cfDef.setDefault_validation_class(mValue);
                break;
            case MIN_COMPACTION_THRESHOLD:
                cfDef.setMin_compaction_threshold(Integer.parseInt(mValue));
                break;
            case MAX_COMPACTION_THRESHOLD:
                cfDef.setMax_compaction_threshold(Integer.parseInt(mValue));
                break;
            default:
                assert(false);
            }
        }
        return cfDef;
    }
    private void executeDelKeySpace(Tree statement)
            throws TException, InvalidRequestException, NotFoundException
    {
        if (!CliMain.isConnected())
            return;
        String keyspaceName = CliCompiler.getKeySpace(statement, thriftClient.describe_keyspaces());
        sessionState.out.println(thriftClient.system_drop_keyspace(keyspaceName));
    }
    private void executeDelColumnFamily(Tree statement) 
            throws TException, InvalidRequestException, NotFoundException
    {
        if (!CliMain.isConnected() || !hasKeySpace())
            return;
        String cfName = CliCompiler.getColumnFamily(statement, keyspacesMap.get(keySpace).cf_defs);
        sessionState.out.println(thriftClient.system_drop_column_family(cfName));
    }
    private void executeList(Tree statement)
        throws TException, InvalidRequestException, NotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException, UnavailableException, TimedOutException
    {
        if (!CliMain.isConnected() || !hasKeySpace())
            return;
        String columnFamily = CliCompiler.getColumnFamily(statement, keyspacesMap.get(keySpace).cf_defs);
        String rawStartKey = "";
        String rawEndKey = "";
        int limitCount = Integer.MAX_VALUE; 
        for (int i = 1; i < statement.getChildCount(); i++)
        {
            Tree child = statement.getChild(i);
            if (child.getType() == CliParser.NODE_KEY_RANGE)
            {
                if (child.getChildCount() > 0)
                {
                    rawStartKey = CliUtils.unescapeSQLString(child.getChild(0).getText());
                    if (child.getChildCount() > 1)
                        rawEndKey = CliUtils.unescapeSQLString(child.getChild(1).getText());
                }
            }
            else
            {
                if (child.getChildCount() != 1)
                {
                    sessionState.out.println("Invalid limit clause");
                    return;
                }
                limitCount = Integer.parseInt(child.getChild(0).getText());
                if (limitCount <= 0)
                {
                    sessionState.out.println("Invalid limit " + limitCount);
                    return;
                }
            }
        }
        if (limitCount == Integer.MAX_VALUE)
        {
            limitCount = 100;
            sessionState.out.println("Using default limit of 100");
        }
        CfDef columnFamilyDef = getCfDef(columnFamily);
        SlicePredicate predicate = new SlicePredicate();
        SliceRange sliceRange = new SliceRange();
        sliceRange.setStart(new byte[0]).setFinish(new byte[0]);
        sliceRange.setCount(Integer.MAX_VALUE);
        predicate.setSlice_range(sliceRange);
        KeyRange range = new KeyRange(limitCount);
        AbstractType keyComparator = this.cfKeysComparators.get(columnFamily);
        ByteBuffer startKey = rawStartKey.isEmpty() ? ByteBufferUtil.EMPTY_BYTE_BUFFER : getBytesAccordingToType(rawStartKey, keyComparator);
        ByteBuffer endKey = rawEndKey.isEmpty() ? ByteBufferUtil.EMPTY_BYTE_BUFFER : getBytesAccordingToType(rawEndKey, keyComparator);
        range.setStart_key(startKey).setEnd_key(endKey);
        ColumnParent columnParent = new ColumnParent(columnFamily);
        List<KeySlice> keySlices = thriftClient.get_range_slices(columnParent, predicate, range, ConsistencyLevel.ONE);
        printSliceList(columnFamilyDef, keySlices);
    }
    private void executeTruncate(String columnFamily)
    {
        if (!CliMain.isConnected() || !hasKeySpace())
            return;
        CfDef cfDef = getCfDef(CliCompiler.getColumnFamily(columnFamily, keyspacesMap.get(keySpace).cf_defs));
        try
        {
            thriftClient.truncate(cfDef.getName());
            sessionState.out.println(columnFamily + " truncated.");
        }
        catch (InvalidRequestException e)
        {
            throw new RuntimeException(e.getWhy());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }
    private void executeAssumeStatement(Tree statement)
    {
        if (!CliMain.isConnected() || !hasKeySpace())
            return;
        String cfName = CliCompiler.getColumnFamily(statement, keyspacesMap.get(keySpace).cf_defs);
        CfDef columnFamily = getCfDef(cfName);
        String assumptionElement = statement.getChild(1).getText().toUpperCase();
        AbstractType comparator;
        String defaultType = statement.getChild(2).getText();
        try
        {
            comparator = Function.valueOf(defaultType.toUpperCase()).getValidator();
        }
        catch (Exception e)
        {
            String functions = Function.getFunctionNames();
            sessionState.out.println("Type '" + defaultType + "' was not found. Available: " + functions);
            return;
        }
        if (assumptionElement.equals("COMPARATOR"))
        {
            columnFamily.setComparator_type(defaultType);
        }
        else if (assumptionElement.equals("SUB_COMPARATOR"))
        {
            columnFamily.setSubcomparator_type(defaultType);
        }
        else if (assumptionElement.equals("VALIDATOR"))
        {
            columnFamily.setDefault_validation_class(defaultType);
        }
        else if (assumptionElement.equals("KEYS"))
        {
            this.cfKeysComparators.put(columnFamily.getName(), comparator);
        }
        else
        {
            String elements = "VALIDATOR, COMPARATOR, KEYS, SUB_COMPARATOR.";
            sessionState.out.println(String.format("'%s' is invalid. Available: %s", assumptionElement, elements));
            return;
        }
        sessionState.out.println(String.format("Assumption for column family '%s' added successfully.", columnFamily.getName()));
    }
    private void executeShowVersion() throws TException
    {
        if (!CliMain.isConnected())
            return;
        sessionState.out.println(thriftClient.describe_version());
    }
    private void executeShowKeySpaces() throws TException, InvalidRequestException
    {
        if (!CliMain.isConnected())
            return;
        List<KsDef> keySpaces = thriftClient.describe_keyspaces();
        Collections.sort(keySpaces, new KsDefNamesComparator());
        for (KsDef keySpace : keySpaces)
        {
            describeKeySpace(keySpace.name, keySpace);
        }
    }
    private boolean hasKeySpace() 
    {
    	if (keySpace == null)
        {
            sessionState.out.println("Not authenticated to a working keyspace.");
            return false;
        }
        return true;
    }
    public String getKeySpace() 
    {
        return keySpace == null ? "unknown" : keySpace;
    }
    public void setKeySpace(String keySpace) throws NotFoundException, InvalidRequestException, TException 
    {
        this.keySpace = keySpace;
        getKSMetaData(keySpace);
    }
    public String getUsername() 
    {
        return username == null ? "default" : username;
    }
    public void setUsername(String username)
    {
        this.username = username;
    }
    private void executeUseKeySpace(Tree statement) throws TException
    {
        if (!CliMain.isConnected())
            return;
        int childCount = statement.getChildCount();
        String keySpaceName, username = null, password = null;
        keySpaceName = statement.getChild(0).getText();
        if (childCount == 3) {
            username  = statement.getChild(1).getText();
            password  = statement.getChild(2).getText();
        }
        if (keySpaceName == null)
        {
            sessionState.out.println("Keyspace argument required");
            return;
        }
        try 
        {
        	AuthenticationRequest authRequest;
        	Map<String, String> credentials = new HashMap<String, String>();
            keySpaceName = CliCompiler.getKeySpace(keySpaceName, thriftClient.describe_keyspaces());
            thriftClient.set_keyspace(keySpaceName);
        	if (username != null && password != null) 
        	{
        	    password = password.replace("\'", "");
        	    credentials.put(SimpleAuthenticator.USERNAME_KEY, username);
                credentials.put(SimpleAuthenticator.PASSWORD_KEY, password);
                authRequest = new AuthenticationRequest(credentials);
                thriftClient.login(authRequest);
        	}
            keySpace = keySpaceName;
            this.username = username != null ? username : "default";
            CliMain.updateCompletor(CliUtils.getCfNamesByKeySpace(getKSMetaData(keySpace)));
            sessionState.out.println("Authenticated to keyspace: " + keySpace);
        } 
        catch (AuthenticationException e) 
        {
            sessionState.err.println("Exception during authentication to the cassandra node: " +
            		                 "verify keyspace exists, and you are using correct credentials.");
        } 
        catch (AuthorizationException e) 
        {
            sessionState.err.println("You are not authorized to use keyspace: " + keySpaceName);
        }
        catch (InvalidRequestException e)
        {
            sessionState.err.println(keySpaceName + " does not exist.");
        }
        catch (NotFoundException e)
        {
            sessionState.err.println(keySpaceName + " does not exist.");
        } 
        catch (TException e) 
        {
            if (sessionState.debug)
                e.printStackTrace();
            sessionState.err.println("Login failure. Did you specify 'keyspace', 'username' and 'password'?");
        }
    }
    private void describeKeySpace(String keySpaceName, KsDef metadata) throws TException
    {
        NodeProbe probe = sessionState.getNodeProbe();
        CompactionManagerMBean compactionManagerMBean = (probe == null) ? null : probe.getCompactionManagerProxy();
        sessionState.out.println("Keyspace: " + keySpaceName + ":");
        try
        {
            KsDef ks_def;
            ks_def = metadata == null ? thriftClient.describe_keyspace(keySpaceName) : metadata;
            sessionState.out.println("  Replication Strategy: " + ks_def.strategy_class);
            if (ks_def.strategy_class.endsWith(".NetworkTopologyStrategy"))
                sessionState.out.println("    Options: " + FBUtilities.toString(ks_def.strategy_options));
            else
                sessionState.out.println("    Replication Factor: " + ks_def.replication_factor);
            sessionState.out.println("  Column Families:");
            boolean isSuper;
            Collections.sort(ks_def.cf_defs, new CfDefNamesComparator());
            for (CfDef cf_def : ks_def.cf_defs)
            {
                ColumnFamilyStoreMBean cfMBean = (probe == null) ? null : probe.getCfsProxy(ks_def.getName(), cf_def.getName());
                isSuper = cf_def.column_type.equals("Super");
                sessionState.out.printf("    ColumnFamily: %s%s%n", cf_def.name, isSuper ? " (Super)" : "");
                if (cf_def.comment != null && !cf_def.comment.isEmpty())
                {
                    sessionState.out.printf("    \"%s\"%n", cf_def.comment);
                }
                sessionState.out.printf("      Columns sorted by: %s%s%n", cf_def.comparator_type, cf_def.column_type.equals("Super") ? "/" + cf_def.subcomparator_type : "");
                sessionState.out.printf("      Row cache size / save period: %s/%s%n", cf_def.row_cache_size, cf_def.row_cache_save_period_in_seconds);
                sessionState.out.printf("      Key cache size / save period: %s/%s%n", cf_def.key_cache_size, cf_def.key_cache_save_period_in_seconds);
                sessionState.out.printf("      Memtable thresholds: %s/%s/%s%n",
                                cf_def.memtable_operations_in_millions, cf_def.memtable_throughput_in_mb, cf_def.memtable_flush_after_mins);
                sessionState.out.printf("      GC grace seconds: %s%n", cf_def.gc_grace_seconds);
                sessionState.out.printf("      Compaction min/max thresholds: %s/%s%n", cf_def.min_compaction_threshold, cf_def.max_compaction_threshold);
                sessionState.out.printf("      Read repair chance: %s%n", cf_def.read_repair_chance);
                if (cfMBean != null)
                {
                    sessionState.out.printf("      Built indexes: %s%n", cfMBean.getBuiltIndexes());
                }
                if (cf_def.getColumn_metadataSize() != 0)
                {
                    String leftSpace = "      ";
                    String columnLeftSpace = leftSpace + "    ";
                    AbstractType columnNameValidator = getFormatTypeForColumn(isSuper ? cf_def.subcomparator_type
                                                                                      : cf_def.comparator_type);
                    sessionState.out.println(leftSpace + "Column Metadata:");
                    for (ColumnDef columnDef : cf_def.getColumn_metadata())
                    {
                        String columnName = columnNameValidator.getString(columnDef.name);
                        if (columnNameValidator instanceof BytesType)
                        {
                            try
                            {
                                String columnString = UTF8Type.instance.getString(columnDef.name);
                                columnName = columnString + " (" + columnName + ")";
                            }
                            catch (MarshalException e)
                            {
                            }
                        }
                        sessionState.out.println(leftSpace + "  Column Name: " + columnName);
                        sessionState.out.println(columnLeftSpace + "Validation Class: " + columnDef.getValidation_class());
                        if (columnDef.isSetIndex_name())
                        {
                            sessionState.out.println(columnLeftSpace + "Index Name: " + columnDef.getIndex_name());
                        }
                        if (columnDef.isSetIndex_type())
                        {
                            sessionState.out.println(columnLeftSpace + "Index Type: " + columnDef.getIndex_type().name());
                        }
                    }
                }
            }
            if (compactionManagerMBean != null)
            {
                String compactionType = compactionManagerMBean.getCompactionType();
                if (compactionType != null && compactionType.contains("index build"))
                {
                    String indexName         = compactionManagerMBean.getColumnFamilyInProgress();
                    long bytesCompacted      = compactionManagerMBean.getBytesCompacted();
                    long totalBytesToProcess = compactionManagerMBean.getBytesTotalInProgress();
                    sessionState.out.printf("%nCurrently building index %s, completed %d of %d bytes.%n", indexName, bytesCompacted, totalBytesToProcess);
                }
            }
            if (probe != null)
                probe.close();
        }
        catch (InvalidRequestException e)
        {
            sessionState.out.println("Invalid request: " + e);
        }
        catch (NotFoundException e)
        {
            sessionState.out.println("Keyspace " + keySpaceName + " could not be found.");
        }
        catch (IOException e)
        {
            sessionState.out.println("Error while closing JMX connection: " + e.getMessage());
        }
    }
    private void executeDescribeKeySpace(Tree statement) throws TException, InvalidRequestException
    {
        if (!CliMain.isConnected())
            return;
        String keySpaceName = CliCompiler.getKeySpace(statement, thriftClient.describe_keyspaces());
        if( keySpaceName == null ) {
            sessionState.out.println("Keyspace argument required");
            return;
        }
        describeKeySpace(keySpaceName, null);
    }
    private void executeConnect(Tree statement)
    {
        Tree idList = statement.getChild(0);
        int portNumber = Integer.parseInt(statement.getChild(1).getText());
        StringBuilder hostName = new StringBuilder();
        int idCount = idList.getChildCount(); 
        for (int idx = 0; idx < idCount; idx++)
        {
            hostName.append(idList.getChild(idx).getText());
        }
        CliMain.disconnect();
        sessionState.hostName = hostName.toString();
        sessionState.thriftPort = portNumber;
        CliMain.connect(sessionState.hostName, sessionState.thriftPort);
    }
    private CfDef getCfDef(String keySpaceName, String columnFamilyName)
    {
        KsDef keySpaceDefinition = keyspacesMap.get(keySpaceName);
        for (CfDef columnFamilyDef : keySpaceDefinition.cf_defs)
        {
            if (columnFamilyDef.name.equals(columnFamilyName))
            {
                return columnFamilyDef;
            }
        }
        throw new RuntimeException("No such column family: " + columnFamilyName);
    }
    private CfDef getCfDef(String columnFamilyName)
    {
        return getCfDef(this.keySpace, columnFamilyName);
    }
    private List<ColumnDef> getCFColumnMetaFromTree(CfDef cfDef, Tree meta)
    {
        List<ColumnDef> columnDefinitions = new ArrayList<ColumnDef>();
        for (int i = 0; i < meta.getChildCount(); i++)
        {
            Tree metaHash = meta.getChild(i);
            ColumnDef columnDefinition = new ColumnDef();
            for (int j = 0; j < metaHash.getChildCount(); j++)
            {
                Tree metaPair = metaHash.getChild(j);
                String metaKey = CliUtils.unescapeSQLString(metaPair.getChild(0).getText());
                String metaVal = CliUtils.unescapeSQLString(metaPair.getChild(1).getText());
                if (metaKey.equals("column_name"))
                {
                    if (cfDef.column_type.equals("Super"))
                        columnDefinition.setName(subColumnNameAsByteArray(metaVal, cfDef));
                    else
                        columnDefinition.setName(columnNameAsByteArray(metaVal, cfDef));
                }
                else if (metaKey.equals("validation_class"))
                {
                    columnDefinition.setValidation_class(metaVal);
                }
                else if (metaKey.equals("index_type"))
                {
                    columnDefinition.setIndex_type(getIndexTypeFromString(metaVal));
                }
                else if (metaKey.equals("index_name"))
                {
                    columnDefinition.setIndex_name(metaVal);    
                }
            }
            try
            {
                columnDefinition.validate();
            }
            catch (TException e)
            {
                throw new RuntimeException(e.getMessage(), e);
            }
            columnDefinitions.add(columnDefinition);
        }
        return columnDefinitions;
    }
    private IndexType getIndexTypeFromString(String indexTypeAsString)
    {
        IndexType indexType;
        try
        {
            indexType = IndexType.findByValue(new Integer(indexTypeAsString));
        }
        catch (NumberFormatException e)
        {
            try
            {
                indexType = IndexType.valueOf(indexTypeAsString);
            }
            catch (IllegalArgumentException ie)
            {
                throw new RuntimeException("IndexType '" + indexTypeAsString + "' is unsupported.");
            }
        }
        if (indexType == null)
        {
            throw new RuntimeException("IndexType '" + indexTypeAsString + "' is unsupported.");
        }
        return indexType;
    }
    private ByteBuffer getBytesAccordingToType(String object, AbstractType comparator)
    {
        if (comparator == null) 
            comparator = BytesType.instance;
        return comparator.fromString(object);
    }
    private ByteBuffer columnNameAsBytes(String column, String columnFamily) 
    {
        CfDef columnFamilyDef = getCfDef(columnFamily);
        return columnNameAsBytes(column, columnFamilyDef);
    }
    private ByteBuffer columnNameAsBytes(String column, CfDef columnFamilyDef) 
    {
        String comparatorClass = columnFamilyDef.comparator_type;
        return getBytesAccordingToType(column, getFormatTypeForColumn(comparatorClass));   
    }
    private byte[] columnNameAsByteArray(String column, String columnFamily)
    {
        return TBaseHelper.byteBufferToByteArray(columnNameAsBytes(column, columnFamily));
    }
    private byte[] columnNameAsByteArray(String column, CfDef cfDef)
    {
        return TBaseHelper.byteBufferToByteArray(columnNameAsBytes(column, cfDef));
    }
    private ByteBuffer subColumnNameAsBytes(String superColumn, String columnFamily)
    {
        CfDef columnFamilyDef = getCfDef(columnFamily);
        return subColumnNameAsBytes(superColumn, columnFamilyDef);
    }
    private ByteBuffer subColumnNameAsBytes(String superColumn, CfDef columnFamilyDef) 
    {
        String comparatorClass = columnFamilyDef.subcomparator_type;
        if (comparatorClass == null)
        {
            sessionState.out.println(String.format("Notice: defaulting to BytesType subcomparator for '%s'", columnFamilyDef.getName()));
            comparatorClass = "BytesType";
        }
        return getBytesAccordingToType(superColumn, getFormatTypeForColumn(comparatorClass));   
    }
    private byte[] subColumnNameAsByteArray(String superColumn, String columnFamily)
    {
        return TBaseHelper.byteBufferToByteArray(subColumnNameAsBytes(superColumn, columnFamily));
    }
    private byte[] subColumnNameAsByteArray(String superColumn, CfDef cfDef)
    {
        return TBaseHelper.byteBufferToByteArray(subColumnNameAsBytes(superColumn, cfDef));
    }
    private ByteBuffer columnValueAsBytes(ByteBuffer columnName, String columnFamilyName, String columnValue)
    {
        CfDef columnFamilyDef = getCfDef(columnFamilyName);
        for (ColumnDef columnDefinition : columnFamilyDef.getColumn_metadata())
        {
            byte[] currentColumnName = columnDefinition.getName();
            if (ByteBufferUtil.compare(currentColumnName, columnName) == 0)
            {
                try
                {
                    String validationClass = columnDefinition.getValidation_class();
                    return getBytesAccordingToType(columnValue, getFormatTypeForColumn(validationClass));
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        return ByteBuffer.wrap(columnValue.getBytes());
    }
    private AbstractType getValidatorForValue(CfDef ColumnFamilyDef, byte[] columnNameInBytes)
    {
        String defaultValidator = ColumnFamilyDef.default_validation_class;
        for (ColumnDef columnDefinition : ColumnFamilyDef.getColumn_metadata())
        {
            byte[] nameInBytes = columnDefinition.getName();
            if (Arrays.equals(nameInBytes, columnNameInBytes))
            {
                return getFormatTypeForColumn(columnDefinition.getValidation_class());
            }
        }
        if (defaultValidator != null && !defaultValidator.isEmpty()) 
        {
            return getFormatTypeForColumn(defaultValidator);
        }
        return null;
    }
    private Map<String, String> getStrategyOptionsFromTree(Tree options)
    {
        Map<String, String> strategyOptions = new HashMap<String, String>();
        for (int i = 0; i < options.getChildCount(); i++)
        {
            Tree optionsHash = options.getChild(i);
            for (int j = 0; j < optionsHash.getChildCount(); j++)
            {
                Tree optionPair = optionsHash.getChild(j);
                String key = CliUtils.unescapeSQLString(optionPair.getChild(0).getText());
                String val = CliUtils.unescapeSQLString(optionPair.getChild(1).getText());
                strategyOptions.put(key, val);
            }
        }
        return strategyOptions;
    }
    private ByteBuffer convertValueByFunction(Tree functionCall, CfDef columnFamily, ByteBuffer columnName)
    {
        return convertValueByFunction(functionCall, columnFamily, columnName, false);
    }
    private ByteBuffer convertValueByFunction(Tree functionCall, CfDef columnFamily, ByteBuffer columnName, boolean withUpdate)
    {
        String functionName = functionCall.getChild(0).getText();
        Tree argumentTree = functionCall.getChild(1);
        String functionArg  = (argumentTree == null) ? "" : CliUtils.unescapeSQLString(argumentTree.getText());
        AbstractType validator = getTypeByFunction(functionName);
        try
        {
            ByteBuffer value;
            if (functionArg.isEmpty())
            {
                if (validator instanceof TimeUUIDType)
                {
                    value = ByteBuffer.wrap(UUIDGenerator.getInstance().generateTimeBasedUUID().asByteArray());
                }
                else if (validator instanceof LexicalUUIDType)
                {
                    value = ByteBuffer.wrap(UUIDGen.decompose(UUID.randomUUID()));
                }
                else if (validator instanceof BytesType)
                {
                    value = ByteBuffer.wrap(new byte[0]);
                }
                else
                {
                    throw new RuntimeException(String.format("Argument for '%s' could not be empty.", functionName));
                }
            }
            else
            {
                value = getBytesAccordingToType(functionArg, validator);
            }
            if (withUpdate)
            {
                updateColumnMetaData(columnFamily, columnName, validator.getClass().getName());
            }
            return value;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }
    public static AbstractType getTypeByFunction(String functionName)
    {
        Function function;
        try
        {
            function = Function.valueOf(functionName.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            StringBuilder errorMessage = new StringBuilder("Function '" + functionName + "' not found. ");
            errorMessage.append("Available functions: ");
            throw new RuntimeException(errorMessage.append(Function.getFunctionNames()).toString());
        }
        return function.getValidator();
    }
    private void updateColumnMetaData(CfDef columnFamily, ByteBuffer columnName, String validationClass)
    {
        List<ColumnDef> columnMetaData = columnFamily.getColumn_metadata();
        ColumnDef column = getColumnDefByName(columnFamily, columnName);
        if (column != null)
        {
            if (column.getValidation_class().equals(validationClass))
                return;
            column.setValidation_class(validationClass);
        }
        else
        {
            columnMetaData.add(new ColumnDef(columnName, validationClass));
        }
    }
    private ColumnDef getColumnDefByName(CfDef columnFamily, ByteBuffer columnName)
    {
        for (ColumnDef columnDef : columnFamily.getColumn_metadata())
        {
            byte[] currName = columnDef.getName();
            if (ByteBufferUtil.compare(currName, columnName) == 0)
            {
                return columnDef;
            }
        }
        return null;
    }
    private void printSliceList(CfDef columnFamilyDef, List<KeySlice> slices)
            throws NotFoundException, TException, IllegalAccessException, InstantiationException, NoSuchFieldException
    {
        AbstractType validator;
        String columnFamilyName = columnFamilyDef.getName();
        AbstractType keyComparator = this.cfKeysComparators.get(columnFamilyName);
        for (KeySlice ks : slices)
        {
            String keyName = (keyComparator == null) ? ByteBufferUtil.string(ks.key, Charsets.UTF_8) : keyComparator.getString(ks.key);
            sessionState.out.printf("-------------------%n");
            sessionState.out.printf("RowKey: %s%n", keyName);
            Iterator<ColumnOrSuperColumn> iterator = ks.getColumnsIterator();
            while (iterator.hasNext())
            {
                ColumnOrSuperColumn columnOrSuperColumn = iterator.next();
                if (columnOrSuperColumn.column != null)
                {
                    Column col = columnOrSuperColumn.column;
                    validator = getValidatorForValue(columnFamilyDef, col.getName());
                    sessionState.out.printf("=> (column=%s, value=%s, timestamp=%d%s)%n",
                                    formatColumnName(keySpace, columnFamilyName, col), validator.getString(col.value), col.timestamp,
                                    col.isSetTtl() ? String.format(", ttl=%d", col.getTtl()) : "");
                }
                else if (columnOrSuperColumn.super_column != null)
                {
                    SuperColumn superCol = columnOrSuperColumn.super_column;
                    sessionState.out.printf("=> (super_column=%s,", formatSuperColumnName(keySpace, columnFamilyName, superCol));
                    for (Column col : superCol.columns)
                    {
                        validator = getValidatorForValue(columnFamilyDef, col.getName());
                        sessionState.out.printf("%n     (column=%s, value=%s, timestamp=%d%s)",
                                        formatSubcolumnName(keySpace, columnFamilyName, col), validator.getString(col.value), col.timestamp,
                                        col.isSetTtl() ? String.format(", ttl=%d", col.getTtl()) : "");
                    }
                    sessionState.out.println(")");
                }
            }
        }
        sessionState.out.printf("%n%d Row%s Returned.%n", slices.size(), (slices.size() > 1 ? "s" : ""));
    }
    private String formatSuperColumnName(String keyspace, String columnFamily, SuperColumn column)
            throws NotFoundException, TException, IllegalAccessException, InstantiationException, NoSuchFieldException
    {
        return getFormatTypeForColumn(getCfDef(keyspace,columnFamily).comparator_type).getString(column.name);
    }
    private String formatSubcolumnName(String keyspace, String columnFamily, Column subcolumn)
            throws NotFoundException, TException, IllegalAccessException, InstantiationException, NoSuchFieldException
    {
        return getFormatTypeForColumn(getCfDef(keyspace,columnFamily).subcomparator_type).getString(subcolumn.name);
    }
    private String formatColumnName(String keyspace, String columnFamily, Column column)
            throws NotFoundException, TException, IllegalAccessException, InstantiationException, NoSuchFieldException
    {
        return getFormatTypeForColumn(getCfDef(keyspace, columnFamily).comparator_type).getString(ByteBuffer.wrap(column.getName()));
    }
    private ByteBuffer getColumnName(String columnFamily, Tree columnTree)
    {
        return (columnTree.getType() == CliParser.FUNCTION_CALL)
                    ? convertValueByFunction(columnTree, null, null)
                    : columnNameAsBytes(CliUtils.unescapeSQLString(columnTree.getText()), columnFamily);
    }
    private ByteBuffer getSubColumnName(String columnFamily, Tree columnTree)
    {
        return (columnTree.getType() == CliParser.FUNCTION_CALL)
                    ? convertValueByFunction(columnTree, null, null)
                    : subColumnNameAsBytes(CliUtils.unescapeSQLString(columnTree.getText()), columnFamily);
    }
    public ByteBuffer getKeyAsBytes(String columnFamily, Tree keyTree)
    {
        if (keyTree.getType() == CliParser.FUNCTION_CALL)
            return convertValueByFunction(keyTree, null, null);
        String key = CliUtils.unescapeSQLString(keyTree.getText());
        AbstractType keyComparator = this.cfKeysComparators.get(columnFamily);
        return getBytesAccordingToType(key, keyComparator);
    }
    private static class KsDefNamesComparator implements Comparator<KsDef>
    {
        public int compare(KsDef a, KsDef b)
        {
            return a.name.compareTo(b.name);
        }
    }
    private static class CfDefNamesComparator implements Comparator<CfDef>
    {
        public int compare(CfDef a, CfDef b)
        {
            return a.name.compareTo(b.name);
        }
    }
}
