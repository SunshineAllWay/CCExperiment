package org.apache.cassandra.cql;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.antlr.runtime.*;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.dht.Bounds;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.*;
import static org.apache.cassandra.thrift.ThriftValidation.validateKey;
import static org.apache.cassandra.thrift.ThriftValidation.validateColumnFamily;
public class QueryProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(QueryProcessor.class);
    private static List<org.apache.cassandra.db.Row> getSlice(String keyspace, SelectStatement select)
    throws InvalidRequestException, TimedOutException, UnavailableException
    {
        List<org.apache.cassandra.db.Row> rows = null;
        QueryPath queryPath = new QueryPath(select.getColumnFamily());
        List<ReadCommand> commands = new ArrayList<ReadCommand>();
        assert select.getKeys().size() == 1;
        ByteBuffer key = select.getKeys().get(0).getByteBuffer();
        validateKey(key);
        if (!select.isColumnRange())
        {
            Collection<ByteBuffer> columnNames = new ArrayList<ByteBuffer>();
            for (Term column : select.getColumnNames())
                columnNames.add(column.getByteBuffer());
            commands.add(new SliceByNamesReadCommand(keyspace, key, queryPath, columnNames));
        }
        else
        {
            commands.add(new SliceFromReadCommand(keyspace,
                                                  key,
                                                  queryPath,
                                                  select.getColumnStart().getByteBuffer(),
                                                  select.getColumnFinish().getByteBuffer(),
                                                  select.isColumnsReversed(),
                                                  select.getColumnsLimit()));
        }
        try
        {
            rows = StorageProxy.read(commands, select.getConsistencyLevel());
        }
        catch (TimeoutException e)
        {
            throw new TimedOutException();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return rows;
    }
    private static List<org.apache.cassandra.db.Row> multiRangeSlice(String keyspace, SelectStatement select)
    throws TimedOutException, UnavailableException
    {
        List<org.apache.cassandra.db.Row> rows = null;
        ByteBuffer startKey = (select.getKeyStart() != null) ? select.getKeyStart().getByteBuffer() : (new Term()).getByteBuffer();
        ByteBuffer finishKey = (select.getKeyFinish() != null) ? select.getKeyFinish().getByteBuffer() : (new Term()).getByteBuffer();
        IPartitioner<?> p = StorageService.getPartitioner();
        AbstractBounds bounds = new Bounds(p.getToken(startKey), p.getToken(finishKey));
        SlicePredicate thriftSlicePredicate = slicePredicateFromSelect(select);
        try
        {
            rows = StorageProxy.getRangeSlice(new RangeSliceCommand(keyspace,
                                                                    select.getColumnFamily(),
                                                                    null,
                                                                    thriftSlicePredicate,
                                                                    bounds,
                                                                    select.getNumRecords()),
                                              select.getConsistencyLevel());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        catch (org.apache.cassandra.thrift.UnavailableException e)
        {
            throw new UnavailableException();
        }
        catch (TimeoutException e)
        {
            throw new TimedOutException();
        }
        return rows;
    }
    private static List<org.apache.cassandra.db.Row> getIndexedSlices(String keyspace, SelectStatement select)
    throws TimedOutException, UnavailableException
    {
        SlicePredicate thriftSlicePredicate = slicePredicateFromSelect(select);
        List<IndexExpression> expressions = new ArrayList<IndexExpression>();
        for (Relation columnRelation : select.getColumnRelations())
        {
            expressions.add(new IndexExpression(columnRelation.getEntity().getByteBuffer(),
                                                IndexOperator.valueOf(columnRelation.operator().toString()),
                                                columnRelation.getValue().getByteBuffer()));
        }
        ByteBuffer startKey = (!select.isKeyRange()) ? (new Term()).getByteBuffer() : select.getKeyStart().getByteBuffer();
        IndexClause thriftIndexClause = new IndexClause(expressions, startKey, select.getNumRecords());
        List<org.apache.cassandra.db.Row> rows;
        try
        {
            rows = StorageProxy.scan(keyspace,
                                     select.getColumnFamily(),
                                     thriftIndexClause,
                                     thriftSlicePredicate,
                                     select.getConsistencyLevel());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        catch (TimeoutException e)
        {
            throw new TimedOutException();
        }
        return rows;
    }
    private static void batchUpdate(String keyspace, List<UpdateStatement> updateStatements, ConsistencyLevel consistency)
    throws InvalidRequestException, UnavailableException, TimedOutException
    {
        List<RowMutation> rowMutations = new ArrayList<RowMutation>();
        for (UpdateStatement update : updateStatements)
        {
            ByteBuffer key = update.getKey().getByteBuffer();
            validateKey(key);
            validateColumnFamily(keyspace, update.getColumnFamily());
            RowMutation rm = new RowMutation(keyspace, key);
            for (Map.Entry<Term, Term> column : update.getColumns().entrySet())
            {
                rm.add(new QueryPath(update.getColumnFamily(), null, column.getKey().getByteBuffer()),
                       column.getValue().getByteBuffer(),
                       System.currentTimeMillis());
            }
            rowMutations.add(rm);
        }
        try
        {
            StorageProxy.mutate(rowMutations, consistency);
        }
        catch (org.apache.cassandra.thrift.UnavailableException e)
        {
            throw new UnavailableException();
        }
        catch (TimeoutException e)
        {
            throw new TimedOutException();
        }
    }
    private static SlicePredicate slicePredicateFromSelect(SelectStatement select)
    {
        SlicePredicate thriftSlicePredicate = new SlicePredicate();
        if (select.isColumnRange() || select.getColumnNames().size() == 0)
        {
            SliceRange sliceRange = new SliceRange();
            sliceRange.start = select.getColumnStart().getByteBuffer();
            sliceRange.finish = select.getColumnFinish().getByteBuffer();
            sliceRange.reversed = select.isColumnsReversed();
            sliceRange.count = select.getColumnsLimit();
            thriftSlicePredicate.slice_range = sliceRange;
        }
        else
        {
            List<ByteBuffer> columnNames = new ArrayList<ByteBuffer>();
            for (Term column : select.getColumnNames())
                columnNames.add(column.getByteBuffer());
            thriftSlicePredicate.column_names = columnNames;
        }
        return thriftSlicePredicate;
    }
    private static void validateSelect(String keyspace, SelectStatement select) throws InvalidRequestException
    {
        if (select.isCountOperation() && (select.isKeyRange() || select.getKeys().size() < 1))
            throw new InvalidRequestException("Counts can only be performed for a single record (Hint: KEY=term)");
        if (!select.isKeyRange() && (select.getKeyFinish() != null))
            throw new InvalidRequestException("Key range clauses must include a start key (i.e. KEY > term)");
        if (select.isKeyRange() && select.getKeys().size() > 0)
            throw new InvalidRequestException("You cannot combine key range and by-key clauses in a SELECT");
        if (select.isKeyRange() && (select.getKeyFinish() != null) && (select.getColumnRelations().size() > 0))
            throw new InvalidRequestException("You cannot combine key range and by-column clauses in a SELECT");
        if (select.getKeys().size() > 1)
            throw new InvalidRequestException("SELECTs can contain only by by-key clause");
        if (select.getColumnRelations().size() > 0)
        {
            Set<ByteBuffer> indexed = Table.open(keyspace).getColumnFamilyStore(select.getColumnFamily()).getIndexedColumns();
            for (Relation relation : select.getColumnRelations())
            {
                if ((relation.operator().equals(RelationType.EQ)) && indexed.contains(relation.getEntity().getByteBuffer()))
                    return;
            }
            throw new InvalidRequestException("No indexed columns present in by-columns clause with \"equals\" operator");
        }
    }
    public static CqlResult process(String queryString, ClientState clientState)
    throws RecognitionException, UnavailableException, InvalidRequestException, TimedOutException
    {
        logger.trace("CQL QUERY: {}", queryString);
        CqlParser parser = getParser(queryString);
        CQLStatement statement = parser.query();
        parser.throwLastRecognitionError();
        String keyspace = clientState.getKeyspace();
        CqlResult avroResult = new CqlResult();
        switch (statement.type)
        {
            case SELECT:
                SelectStatement select = (SelectStatement)statement.statement;
                validateColumnFamily(keyspace, select.getColumnFamily());
                validateSelect(keyspace, select);
                List<org.apache.cassandra.db.Row> rows = null;
                if (!select.isKeyRange() && (select.getKeys().size() > 0))
                {
                    rows = getSlice(keyspace, select);
                    if (select.isCountOperation())
                    {
                        avroResult.type = CqlResultType.INT;
                        if (rows.size() > 0)
                            avroResult.setNum(rows.get(0).cf != null ? rows.get(0).cf.getSortedColumns().size() : 0);
                        else
                            avroResult.setNum(0);
                        return avroResult;
                    }
                }
                else
                {
                    if ((select.getKeyFinish() != null) || (select.getColumnRelations().size() == 0))
                    {
                        rows = multiRangeSlice(keyspace, select);
                    }
                    else
                    {
                        rows = getIndexedSlices(keyspace, select);
                    }
                }
                List<CqlRow> avroRows = new ArrayList<CqlRow>();
                avroResult.type = CqlResultType.ROWS;
                for (org.apache.cassandra.db.Row row : rows)
                {
                    if (row.cf == null)
                        continue;
                    List<Column> avroColumns = new ArrayList<Column>();
                    for (IColumn column : row.cf.getSortedColumns())
                    {
                        if (column.isMarkedForDelete())
                            continue;
                        Column avroColumn = new Column();
                        avroColumn.name = column.name();
                        avroColumn.value = column.value();
                        avroColumn.timestamp = column.timestamp();
                        avroColumns.add(avroColumn);
                    }
                    CqlRow avroRow = new CqlRow();
                    avroRow.key = row.key.key;
                    avroRow.columns = avroColumns;
                    if (select.isColumnsReversed())
                        Collections.reverse(avroRow.columns);
                    avroRows.add(avroRow);
                }
                avroResult.rows = avroRows;
                return avroResult;
            case UPDATE:
                UpdateStatement update = (UpdateStatement)statement.statement;
                batchUpdate(keyspace, Collections.singletonList(update), update.getConsistencyLevel());
                avroResult.type = CqlResultType.VOID;
                return avroResult;
            case BATCH_UPDATE:
                BatchUpdateStatement batch = (BatchUpdateStatement)statement.statement;
                for (UpdateStatement up : batch.getUpdates())
                    if (up.isSetConsistencyLevel())
                        throw new InvalidRequestException(
                                "Consistency level must be set on the BATCH, not individual UPDATE statements");
                batchUpdate(keyspace, batch.getUpdates(), batch.getConsistencyLevel());
                avroResult.type = CqlResultType.VOID;
                return avroResult;
            case USE:
                clientState.setKeyspace((String)statement.statement);
                avroResult.type = CqlResultType.VOID;
                return avroResult;
            case TRUNCATE:
                String columnFamily = (String)statement.statement;
                try
                {
                    StorageProxy.truncateBlocking(keyspace, columnFamily);
                }
                catch (TimeoutException e)
                {
                    throw (UnavailableException) new UnavailableException().initCause(e);
                }
                catch (IOException e)
                {
                    throw (UnavailableException) new UnavailableException().initCause(e);
                }
                avroResult.type = CqlResultType.VOID;
                return avroResult;
            case DELETE:
                DeleteStatement delete = (DeleteStatement)statement.statement;
                List<RowMutation> rowMutations = new ArrayList<RowMutation>();
                for (Term key : delete.getKeys())
                {
                    RowMutation rm = new RowMutation(keyspace, key.getByteBuffer());
                    if (delete.getColumns().size() < 1)     
                        rm.delete(new QueryPath(delete.getColumnFamily()), System.currentTimeMillis());
                    else    
                    {
                        for (Term column : delete.getColumns())
                            rm.delete(new QueryPath(delete.getColumnFamily(), null, column.getByteBuffer()),
                                      System.currentTimeMillis());
                    }
                    rowMutations.add(rm);
                }
                try
                {
                    StorageProxy.mutate(rowMutations, delete.getConsistencyLevel());
                }
                catch (TimeoutException e)
                {
                    throw new TimedOutException();
                }
                avroResult.type = CqlResultType.VOID;
                return avroResult;
        }
        return null;    
    }
    private static CqlParser getParser(String queryStr)
    {
        CharStream stream = new ANTLRStringStream(queryStr);
        CqlLexer lexer = new CqlLexer(stream);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        return new CqlParser(tokenStream);
    }
}
