package org.apache.cassandra.config;
import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;
import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.auth.AllowAllAuthenticator;
import org.apache.cassandra.auth.AllowAllAuthority;
import org.apache.cassandra.auth.IAuthenticator;
import org.apache.cassandra.auth.IAuthority;
import org.apache.cassandra.config.Config.RequestSchedulerId;
import org.apache.cassandra.db.ColumnFamilyType;
import org.apache.cassandra.db.DefsTable;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.db.commitlog.CommitLog;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.migration.Migration;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.io.sstable.Descriptor;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.locator.*;
import org.apache.cassandra.scheduler.IRequestScheduler;
import org.apache.cassandra.scheduler.NoScheduler;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Pair;
import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
public class DatabaseDescriptor
{
    private static Logger logger = LoggerFactory.getLogger(DatabaseDescriptor.class);
    private static IEndpointSnitch snitch;
    private static InetAddress listenAddress; 
    private static InetAddress rpcAddress;
    private static SeedProvider seedProvider;
    private static int currentIndex = 0;
    private static int consistencyThreads = 4; 
    static Map<String, KSMetaData> tables = new HashMap<String, KSMetaData>();
    private static IPartitioner partitioner;
    private static Config.DiskAccessMode indexAccessMode;
    private static Config conf;
    private static IAuthenticator authenticator = new AllowAllAuthenticator();
    private static IAuthority authority = new AllowAllAuthority();
    private final static String DEFAULT_CONFIGURATION = "cassandra.yaml";
    private static IRequestScheduler requestScheduler;
    private static RequestSchedulerId requestSchedulerId;
    private static RequestSchedulerOptions requestSchedulerOptions;
    public static final UUID INITIAL_VERSION = new UUID(4096, 0); 
    private static UUID defsVersion = INITIAL_VERSION;
    static URL getStorageConfigURL() throws ConfigurationException
    {
        String configUrl = System.getProperty("cassandra.config");
        if (configUrl == null)
            configUrl = DEFAULT_CONFIGURATION;
        URL url;
        try
        {
            url = new URL(configUrl);
            url.openStream(); 
        }
        catch (Exception e)
        {
            ClassLoader loader = DatabaseDescriptor.class.getClassLoader();
            url = loader.getResource(configUrl);
            if (url == null)
                throw new ConfigurationException("Cannot locate " + configUrl);
        }
        return url;
    }
    static
    {
        try
        {
            URL url = getStorageConfigURL();
            logger.info("Loading settings from " + url);
            InputStream input = url.openStream();
            org.yaml.snakeyaml.constructor.Constructor constructor = new org.yaml.snakeyaml.constructor.Constructor(Config.class);
            TypeDescription desc = new TypeDescription(Config.class);
            desc.putListPropertyType("keyspaces", RawKeyspace.class);
            TypeDescription ksDesc = new TypeDescription(RawKeyspace.class);
            ksDesc.putListPropertyType("column_families", RawColumnFamily.class);
            TypeDescription cfDesc = new TypeDescription(RawColumnFamily.class);
            cfDesc.putListPropertyType("column_metadata", RawColumnDefinition.class);
            TypeDescription seedDesc = new TypeDescription(SeedProviderDef.class);
            seedDesc.putMapPropertyType("parameters", String.class, String.class);
            constructor.addTypeDescription(desc);
            constructor.addTypeDescription(ksDesc);
            constructor.addTypeDescription(cfDesc);
            Yaml yaml = new Yaml(new Loader(constructor));
            conf = (Config)yaml.load(input);
            if (conf.commitlog_sync == null)
            {
                throw new ConfigurationException("Missing required directive CommitLogSync");
            }
            if (conf.commitlog_sync == Config.CommitLogSync.batch)
            {
                if (conf.commitlog_sync_batch_window_in_ms == null)
                {
                    throw new ConfigurationException("Missing value for commitlog_sync_batch_window_in_ms: Double expected.");
                } 
                else if (conf.commitlog_sync_period_in_ms != null)
                {
                    throw new ConfigurationException("Batch sync specified, but commitlog_sync_period_in_ms found. Only specify commitlog_sync_batch_window_in_ms when using batch sync");
                }
                logger.debug("Syncing log with a batch window of " + conf.commitlog_sync_batch_window_in_ms);
            }
            else
            {
                if (conf.commitlog_sync_period_in_ms == null)
                {
                    throw new ConfigurationException("Missing value for commitlog_sync_period_in_ms: Integer expected");
                }
                else if (conf.commitlog_sync_batch_window_in_ms != null)
                {
                    throw new ConfigurationException("commitlog_sync_period_in_ms specified, but commitlog_sync_batch_window_in_ms found.  Only specify commitlog_sync_period_in_ms when using periodic sync.");
                }
                logger.debug("Syncing log with a period of " + conf.commitlog_sync_period_in_ms);
            }
            if (conf.disk_access_mode == Config.DiskAccessMode.auto)
            {
                conf.disk_access_mode = System.getProperty("os.arch").contains("64") ? Config.DiskAccessMode.mmap : Config.DiskAccessMode.standard;
                indexAccessMode = conf.disk_access_mode;
                logger.info("DiskAccessMode 'auto' determined to be " + conf.disk_access_mode + ", indexAccessMode is " + indexAccessMode );
            }
            else if (conf.disk_access_mode == Config.DiskAccessMode.mmap_index_only)
            {
                conf.disk_access_mode = Config.DiskAccessMode.standard;
                indexAccessMode = Config.DiskAccessMode.mmap;
                logger.info("DiskAccessMode is " + conf.disk_access_mode + ", indexAccessMode is " + indexAccessMode );
            }
            else
            {
                indexAccessMode = conf.disk_access_mode;
                logger.info("DiskAccessMode is " + conf.disk_access_mode + ", indexAccessMode is " + indexAccessMode );
            }
            if (conf.authenticator != null)
                authenticator = FBUtilities.<IAuthenticator>construct(conf.authenticator, "authenticator");
            if (conf.authority != null)
                authority = FBUtilities.<IAuthority>construct(conf.authority, "authority");
            authenticator.validateConfiguration();
            authority.validateConfiguration();
            if (conf.partitioner == null)
            {
                throw new ConfigurationException("Missing directive: partitioner");
            }
            try
            {
                partitioner = FBUtilities.newPartitioner(conf.partitioner);
            }
            catch (Exception e)
            {
                throw new ConfigurationException("Invalid partitioner class " + conf.partitioner);
            }
            if (conf.phi_convict_threshold < 5 || conf.phi_convict_threshold > 16)
            {
                throw new ConfigurationException("phi_convict_threshold must be between 5 and 16");
            }
            if (conf.concurrent_reads != null && conf.concurrent_reads < 2) 
            {
                throw new ConfigurationException("concurrent_reads must be at least 2");
            }
            if (conf.concurrent_writes != null && conf.concurrent_writes < 2)
            {
                throw new ConfigurationException("concurrent_writes must be at least 2");
            }
            if (conf.concurrent_replicates != null && conf.concurrent_replicates < 2)
            {
                throw new ConfigurationException("conf.concurrent_replicates must be at least 2");
            }
            if (conf.memtable_flush_writers != null && conf.memtable_flush_writers < 1)
            {
                throw new ConfigurationException("memtable_flush_writers must be at least 1");
            }
            else if (conf.memtable_flush_writers == null)
            {
                conf.memtable_flush_writers = conf.data_file_directories.length;
            }
            if (conf.listen_address != null)
            {
                if (conf.listen_address.equals("0.0.0.0"))
                {
                    throw new ConfigurationException("listen_address must be a single interface.  See http://wiki.apache.org/cassandra/FAQ#cant_listen_on_ip_any");
                }
                try
                {
                    listenAddress = InetAddress.getByName(conf.listen_address);
                }
                catch (UnknownHostException e)
                {
                    throw new ConfigurationException("Unknown listen_address '" + conf.listen_address + "'");
                }
            }
            if (conf.rpc_address != null)
                rpcAddress = InetAddress.getByName(conf.rpc_address);
            if (conf.thrift_framed_transport_size_in_mb > 0 && conf.thrift_max_message_length_in_mb < conf.thrift_framed_transport_size_in_mb)
            {
                throw new ConfigurationException("thrift_max_message_length_in_mb must be greater than thrift_framed_transport_size_in_mb when using TFramedTransport");
            }
            if (conf.compaction_thread_priority < Thread.MIN_PRIORITY || conf.compaction_thread_priority > Thread.NORM_PRIORITY)
            {
                throw new ConfigurationException("compaction_thread_priority must be between 1 and 5");
            }
            if (conf.endpoint_snitch == null)
            {
                throw new ConfigurationException("Missing endpoint_snitch directive");
            }
            snitch = createEndpointSnitch(conf.endpoint_snitch);
            EndpointSnitchInfo.create();
            requestSchedulerOptions = conf.request_scheduler_options;
            if (conf.request_scheduler != null)
            {
                try
                {
                    if (requestSchedulerOptions == null)
                    {
                        requestSchedulerOptions = new RequestSchedulerOptions();
                    }
                    Class cls = Class.forName(conf.request_scheduler);
                    requestScheduler = (IRequestScheduler) cls.getConstructor(RequestSchedulerOptions.class).newInstance(requestSchedulerOptions);
                }
                catch (ClassNotFoundException e)
                {
                    throw new ConfigurationException("Invalid Request Scheduler class " + conf.request_scheduler);
                }
            }
            else
            {
                requestScheduler = new NoScheduler();
            }
            if (conf.request_scheduler_id == RequestSchedulerId.keyspace)
            {
                requestSchedulerId = conf.request_scheduler_id;
            }
            else
            {
                requestSchedulerId = RequestSchedulerId.keyspace;
            }
            if (logger.isDebugEnabled() && conf.auto_bootstrap != null)
            {
                logger.debug("setting auto_bootstrap to " + conf.auto_bootstrap);
            }
           if (conf.in_memory_compaction_limit_in_mb != null && conf.in_memory_compaction_limit_in_mb <= 0)
            {
                throw new ConfigurationException("in_memory_compaction_limit_in_mb must be a positive integer");
            }
            if (conf.commitlog_directory != null && conf.data_file_directories != null && conf.saved_caches_directory != null)
            {
                for (String datadir : conf.data_file_directories)
                {
                    if (datadir.equals(conf.commitlog_directory))
                        throw new ConfigurationException("commitlog_directory must not be the same as any data_file_directories");
                    if (datadir.equals(conf.saved_caches_directory))
                        throw new ConfigurationException("saved_caches_directory must not be the same as any data_file_directories");
                }
                if (conf.commitlog_directory.equals(conf.saved_caches_directory))
                    throw new ConfigurationException("saved_caches_directory must not be the same as the commitlog_directory");
            }
            else
            {
                if (conf.commitlog_directory == null)
                    throw new ConfigurationException("commitlog_directory missing");
                if (conf.data_file_directories == null)
                    throw new ConfigurationException("data_file_directories missing; at least one data directory must be specified");
                if (conf.saved_caches_directory == null)
                    throw new ConfigurationException("saved_caches_directory missing");
            }
            if (conf.commitlog_rotation_threshold_in_mb != null)
                CommitLog.setSegmentSize(conf.commitlog_rotation_threshold_in_mb * 1024 * 1024);
            KSMetaData systemMeta = new KSMetaData(Table.SYSTEM_TABLE,
                                                   LocalStrategy.class,
                                                   null,
                                                   1,
                                                   CFMetaData.StatusCf,
                                                   CFMetaData.HintsCf,
                                                   CFMetaData.MigrationsCf,
                                                   CFMetaData.SchemaCf,
                                                   CFMetaData.IndexCf);
            CFMetaData.map(CFMetaData.StatusCf);
            CFMetaData.map(CFMetaData.HintsCf);
            CFMetaData.map(CFMetaData.MigrationsCf);
            CFMetaData.map(CFMetaData.SchemaCf);
            CFMetaData.map(CFMetaData.IndexCf);
            tables.put(Table.SYSTEM_TABLE, systemMeta);
            if (conf.seed_provider == null)
            {
                throw new ConfigurationException("seeds configuration is missing; a minimum of one seed is required.");
            }
            Class seedProviderClass = Class.forName(conf.seed_provider.class_name);
            seedProvider = (SeedProvider)seedProviderClass.getConstructor(Map.class).newInstance(conf.seed_provider.parameters);
            if (seedProvider.getSeeds().size() == 0)
                throw new ConfigurationException("The seed provider lists no seeds.");
        }
        catch (UnknownHostException e)
        {
            logger.error("Fatal error: " + e.getMessage());
            System.err.println("Unable to start with unknown hosts configured.  Use IP addresses instead of hostnames.");
            System.exit(2);
        }
        catch (ConfigurationException e)
        {
            logger.error("Fatal error: " + e.getMessage(), e);
            System.err.println("Bad configuration; unable to start server");
            System.exit(1);
        }
        catch (YAMLException e)
        {
            logger.error("Fatal error: " + e.getMessage(), e);
            System.err.println("Bad configuration; unable to start server");
            System.exit(1);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    private static IEndpointSnitch createEndpointSnitch(String endpointSnitchClassName) throws ConfigurationException
    {
        IEndpointSnitch snitch = FBUtilities.construct(endpointSnitchClassName, "snitch");
        return conf.dynamic_snitch ? new DynamicEndpointSnitch(snitch) : snitch;
    }
    public static void loadSchemas() throws IOException                         
    {
        UUID uuid = Migration.getLastMigrationId();
        if (uuid == null)
        {
            logger.info("Couldn't detect any schema definitions in local storage.");
            boolean hasExistingTables = false;
            for (String dataDir : DatabaseDescriptor.getAllDataFileLocations())
            {
                File dataPath = new File(dataDir);
                if (dataPath.exists() && dataPath.isDirectory())
                {
                    int dirCount = dataPath.listFiles(new FileFilter()
                    {
                        public boolean accept(File pathname)
                        {
                            return pathname.isDirectory();
                        }
                    }).length;
                    if (dirCount > 0)
                        hasExistingTables = true;
                }
                if (hasExistingTables)
                {
                    break;
                }
            }
            if (hasExistingTables)
                logger.info("Found table data in data directories. Consider using JMX to call org.apache.cassandra.service.StorageService.loadSchemaFromYaml().");
            else
                logger.info("Consider using JMX to org.apache.cassandra.service.StorageService.loadSchemaFromYaml() or set up a schema using the system_* calls provided via thrift.");
        }
        else
        {
            logger.info("Loading schema version " + uuid.toString());
            Collection<KSMetaData> tableDefs = DefsTable.loadFromStorage(uuid);   
            for (KSMetaData def : tableDefs)
            {
                if (!def.name.matches(Migration.NAME_VALIDATOR_REGEX))
                    throw new RuntimeException("invalid keyspace name: " + def.name);
                for (CFMetaData cfm : def.cfMetaData().values())
                {
                    if (!cfm.cfName.matches(Migration.NAME_VALIDATOR_REGEX))
                        throw new RuntimeException("invalid column family name: " + cfm.cfName);
                    try
                    {
                        CFMetaData.map(cfm);
                    }
                    catch (ConfigurationException ex)
                    {
                        throw new IOError(ex);
                    }
                }
                DatabaseDescriptor.setTableDefinition(def, uuid);
            }
            if (tableDefs.size() == 0)
            {
                logger.warn("No schema definitions were found in local storage.");
                defsVersion = uuid;
            }
            if (conf.keyspaces != null && conf.keyspaces.size() > 0)
                logger.warn("Schema definitions were defined both locally and in " + DEFAULT_CONFIGURATION +
                    ". Definitions in " + DEFAULT_CONFIGURATION + " were ignored.");
        }
        CFMetaData.fixMaxId();
    }
    public static Collection<KSMetaData> readTablesFromYaml() throws ConfigurationException
    {
        List<KSMetaData> defs = new ArrayList<KSMetaData>();
        for (RawKeyspace keyspace : conf.keyspaces)
        {
            if (keyspace.name == null)
            {
                throw new ConfigurationException("Keyspace name attribute is required");
            }
            if (keyspace.name.equalsIgnoreCase(Table.SYSTEM_TABLE))
            {
                throw new ConfigurationException("'system' is a reserved table name for Cassandra internals");
            }
            if (keyspace.replica_placement_strategy == null)
            {
                throw new ConfigurationException("Missing replica_placement_strategy directive for " + keyspace.name);
            }
            String strategyClassName = KSMetaData.convertOldStrategyName(keyspace.replica_placement_strategy);
            Class<AbstractReplicationStrategy> strategyClass = FBUtilities.classForName(strategyClassName, "replication-strategy");
            if (keyspace.replication_factor == null)
            {
                throw new ConfigurationException("Missing replication_factor directory for keyspace " + keyspace.name);
            }
            int size2 = keyspace.column_families.length;
            CFMetaData[] cfDefs = new CFMetaData[size2];
            int j = 0;
            for (RawColumnFamily cf : keyspace.column_families)
            {
                if (cf.name == null)
                {
                    throw new ConfigurationException("ColumnFamily name attribute is required");
                }
                if (!cf.name.matches(Migration.NAME_VALIDATOR_REGEX))
                {
                    throw new ConfigurationException("ColumnFamily name contains invalid characters.");
                }
                AbstractType comparator = getComparator(cf.compare_with);
                AbstractType subcolumnComparator = null;
                AbstractType default_validator = getComparator(cf.default_validation_class);
                ColumnFamilyType cfType = cf.column_type == null ? ColumnFamilyType.Standard : cf.column_type;
                if (cfType == ColumnFamilyType.Super)
                {
                    subcolumnComparator = getComparator(cf.compare_subcolumns_with);
                }
                else if (cf.compare_subcolumns_with != null)
                {
                    throw new ConfigurationException("compare_subcolumns_with is only a valid attribute on super columnfamilies (not regular columnfamily " + cf.name + ")");
                }
                if (cf.read_repair_chance < 0.0 || cf.read_repair_chance > 1.0)
                {                        
                    throw new ConfigurationException("read_repair_chance must be between 0.0 and 1.0 (0% and 100%)");
                }
                if (conf.dynamic_snitch_badness_threshold < 0.0 || conf.dynamic_snitch_badness_threshold > 1.0)
                {
                    throw new ConfigurationException("dynamic_snitch_badness_threshold must be between 0.0 and 1.0 (0% and 100%)");
                }
                if (cf.min_compaction_threshold < 0 || cf.max_compaction_threshold < 0)
                {
                    throw new ConfigurationException("min/max_compaction_thresholds must be positive integers.");
                }
                if ((cf.min_compaction_threshold > cf.max_compaction_threshold) && cf.max_compaction_threshold != 0)
                {
                    throw new ConfigurationException("min_compaction_threshold must be smaller than max_compaction_threshold, or either must be 0 (disabled)");
                }
                if (cf.memtable_throughput_in_mb == null)
                {
                    cf.memtable_throughput_in_mb = CFMetaData.sizeMemtableThroughput();
                    logger.info("memtable_throughput_in_mb not configured for " + cf.name + ", using " + cf.memtable_throughput_in_mb);
                }
                if (cf.memtable_operations_in_millions == null)
                {
                    cf.memtable_operations_in_millions = CFMetaData.sizeMemtableOperations(cf.memtable_throughput_in_mb);
                    logger.info("memtable_operations_in_millions not configured for " + cf.name + ", using " + cf.memtable_operations_in_millions);
                }
                if (cf.memtable_operations_in_millions != null && cf.memtable_operations_in_millions <= 0)
                {
                    throw new ConfigurationException("memtable_operations_in_millions must be a positive double");
                }
                 Map<ByteBuffer, ColumnDefinition> metadata = new TreeMap<ByteBuffer, ColumnDefinition>();
                for (RawColumnDefinition rcd : cf.column_metadata)
                {
                    if (rcd.name == null)
                    {
                        throw new ConfigurationException("name is required for column definitions.");
                    }
                    if (rcd.validator_class == null)
                    {
                        throw new ConfigurationException("validator is required for column definitions");
                    }
                    if ((rcd.index_type == null) && (rcd.index_name != null))
                    {
                        throw new ConfigurationException("index_name cannot be set if index_type is not also set");
                    }
                    ByteBuffer columnName = ByteBuffer.wrap(rcd.name.getBytes(Charsets.UTF_8));
                    metadata.put(columnName, new ColumnDefinition(columnName, rcd.validator_class, rcd.index_type, rcd.index_name));
                }
                cfDefs[j++] = new CFMetaData(keyspace.name, 
                                             cf.name, 
                                             cfType,
                                             comparator, 
                                             subcolumnComparator, 
                                             cf.comment, 
                                             cf.rows_cached,
                                             cf.keys_cached, 
                                             cf.read_repair_chance,
                                             cf.replicate_on_write,
                                             cf.gc_grace_seconds,
                                             default_validator,
                                             cf.min_compaction_threshold,
                                             cf.max_compaction_threshold,
                                             cf.row_cache_save_period_in_seconds,
                                             cf.key_cache_save_period_in_seconds,
                                             cf.memtable_flush_after_mins,
                                             cf.memtable_throughput_in_mb,
                                             cf.memtable_operations_in_millions,
                                             metadata);
            }
            defs.add(new KSMetaData(keyspace.name,
                                    strategyClass,
                                    keyspace.strategy_options,
                                    keyspace.replication_factor,
                                    cfDefs));
        }
        return defs;
    }
    public static IAuthenticator getAuthenticator()
    {
        return authenticator;
    }
    public static IAuthority getAuthority()
    {
        return authority;
    }
    public static boolean isThriftFramed()
    {
        return conf.thrift_framed_transport_size_in_mb > 0;
    }
    public static int getThriftMaxMessageLength()
    {
        return conf.thrift_max_message_length_in_mb * 1024 * 1024;
    }
    public static int getThriftFramedTransportSize() 
    {
        return conf.thrift_framed_transport_size_in_mb * 1024 * 1024;
    }
    public static AbstractType getComparator(CharSequence compareWith) throws ConfigurationException
    {
        if (compareWith == null)
            compareWith = "BytesType";
        return FBUtilities.getComparator(compareWith.toString());
    }
    public static void createAllDirectories() throws IOException
    {
        try {
            if (conf.data_file_directories.length == 0)
            {
                throw new ConfigurationException("At least one DataFileDirectory must be specified");
            }
            for ( String dataFileDirectory : conf.data_file_directories )
                FileUtils.createDirectory(dataFileDirectory);
            if (conf.commitlog_directory == null)
            {
                throw new ConfigurationException("commitlog_directory must be specified");
            }
            FileUtils.createDirectory(conf.commitlog_directory);
            if (conf.saved_caches_directory == null)
            {
                throw new ConfigurationException("saved_caches_directory must be specified");
            }
            FileUtils.createDirectory(conf.saved_caches_directory);
        }
        catch (ConfigurationException ex) {
            logger.error("Fatal error: " + ex.getMessage());
            System.err.println("Bad configuration; unable to start server");
            System.exit(1);
        }
    }
    public static IPartitioner getPartitioner()
    {
        return partitioner;
    }
    public static IEndpointSnitch getEndpointSnitch()
    {
        return snitch;
    }
    public static void setEndpointSnitch(IEndpointSnitch eps)
    {
        snitch = eps;
    }
    public static IRequestScheduler getRequestScheduler()
    {
        return requestScheduler;
    }
    public static RequestSchedulerOptions getRequestSchedulerOptions()
    {
        return requestSchedulerOptions;
    }
    public static RequestSchedulerId getRequestSchedulerId()
    {
        return requestSchedulerId;
    }
    public static KSMetaData getKSMetaData(String table)
    {
        assert table != null;
        return tables.get(table);
    }
    public static String getJobTrackerAddress()
    {
        return conf.job_tracker_host;
    }
    public static int getColumnIndexSize()
    {
    	return conf.column_index_size_in_kb * 1024;
    }
    public static String getInitialToken()
    {
        return conf.initial_token;
    }
   public static String getClusterName()
    {
        return conf.cluster_name;
    }
    public static String getJobJarLocation()
    {
        return conf.job_jar_file_location;
    }
    public static Map<String, CFMetaData> getTableMetaData(String tableName)
    {
        assert tableName != null;
        KSMetaData ksm = tables.get(tableName);
        assert ksm != null;
        return ksm.cfMetaData();
    }
    public static CFMetaData getCFMetaData(String tableName, String cfName)
    {
        assert tableName != null;
        KSMetaData ksm = tables.get(tableName);
        if (ksm == null)
            return null;
        return ksm.cfMetaData().get(cfName);
    }
    public static CFMetaData getCFMetaData(Integer cfId)
    {
        Pair<String,String> cf = CFMetaData.getCF(cfId);
        if (cf == null)
            return null;
        return getCFMetaData(cf.left, cf.right);
    }
    public static ColumnFamilyType getColumnFamilyType(String tableName, String cfName)
    {
        assert tableName != null && cfName != null;
        CFMetaData cfMetaData = getCFMetaData(tableName, cfName);
        if (cfMetaData == null)
            return null;
        return cfMetaData.cfType;
    }
    public static Set<String> getTables()
    {
        return tables.keySet();
    }
    public static List<String> getNonSystemTables()
    {
        List<String> tableslist = new ArrayList<String>(tables.keySet());
        tableslist.remove(Table.SYSTEM_TABLE);
        return Collections.unmodifiableList(tableslist);
    }
    public static int getStoragePort()
    {
        return conf.storage_port;
    }
    public static int getRpcPort()
    {
        return conf.rpc_port;
    }
    public static long getRpcTimeout()
    {
        return conf.rpc_timeout_in_ms;
    }
    public static int getPhiConvictThreshold()
    {
        return conf.phi_convict_threshold;
    }
    public static int getConsistencyThreads()
    {
        return consistencyThreads;
    }
    public static int getConcurrentReaders()
    {
        return conf.concurrent_reads;
    }
    public static int getConcurrentWriters()
    {
        return conf.concurrent_writes;
    }
    public static int getConcurrentReplicators()
    {
        return conf.concurrent_replicates;
    }
    public static int getFlushWriters()
    {
            return conf.memtable_flush_writers;
    }
    public static int getInMemoryCompactionLimit()
    {
        return conf.in_memory_compaction_limit_in_mb * 1024 * 1024;
    }
    public static String[] getAllDataFileLocations()
    {
        return conf.data_file_directories;
    }
    public static String[] getAllDataFileLocationsForTable(String table)
    {
        String[] tableLocations = new String[conf.data_file_directories.length];
        for (int i = 0; i < conf.data_file_directories.length; i++)
        {
            tableLocations[i] = conf.data_file_directories[i] + File.separator + table;
        }
        return tableLocations;
    }
    public synchronized static String getNextAvailableDataLocation()
    {
        String dataFileDirectory = conf.data_file_directories[currentIndex];
        currentIndex = (currentIndex + 1) % conf.data_file_directories.length;
        return dataFileDirectory;
    }
    public static String getCommitLogLocation()
    {
        return conf.commitlog_directory;
    }
    public static String getSavedCachesLocation()
    {
        return conf.saved_caches_directory;
    }
    public static Set<InetAddress> getSeeds()
    {
        return Collections.unmodifiableSet(new HashSet(seedProvider.getSeeds()));
    }
    public static String getDataFileLocationForTable(String table, long expectedCompactedFileSize)
    {
      long maxFreeDisk = 0;
      int maxDiskIndex = 0;
      String dataFileDirectory = null;
      String[] dataDirectoryForTable = getAllDataFileLocationsForTable(table);
      for ( int i = 0 ; i < dataDirectoryForTable.length ; i++ )
      {
        File f = new File(dataDirectoryForTable[i]);
        if( maxFreeDisk < f.getUsableSpace())
        {
          maxFreeDisk = f.getUsableSpace();
          maxDiskIndex = i;
        }
      }
      maxFreeDisk = (long)(0.9 * maxFreeDisk);
      if( expectedCompactedFileSize < maxFreeDisk )
      {
        dataFileDirectory = dataDirectoryForTable[maxDiskIndex];
        currentIndex = (maxDiskIndex + 1 )%dataDirectoryForTable.length ;
      }
      else
      {
        currentIndex = maxDiskIndex;
      }
        return dataFileDirectory;
    }
    public static AbstractType getComparator(String tableName, String cfName)
    {
        assert tableName != null;
        CFMetaData cfmd = getCFMetaData(tableName, cfName);
        if (cfmd == null)
            throw new IllegalArgumentException("Unknown ColumnFamily " + cfName + " in keyspace " + tableName);
        return cfmd.comparator;
    }
    public static AbstractType getSubComparator(String tableName, String cfName)
    {
        assert tableName != null;
        return getCFMetaData(tableName, cfName).subcolumnComparator;
    }
    public static int getKeysCachedFor(String tableName, String columnFamilyName, long expectedKeys)
    {
        CFMetaData cfm = getCFMetaData(tableName, columnFamilyName);
        double v = (cfm == null) ? CFMetaData.DEFAULT_KEY_CACHE_SIZE : cfm.getKeyCacheSize();
        return (int)Math.min(FBUtilities.absoluteFromFraction(v, expectedKeys), Integer.MAX_VALUE);
    }
    public static int getRowsCachedFor(String tableName, String columnFamilyName, long expectedRows)
    {
        CFMetaData cfm = getCFMetaData(tableName, columnFamilyName);
        double v = (cfm == null) ? CFMetaData.DEFAULT_ROW_CACHE_SIZE : cfm.getRowCacheSize();
        return (int)Math.min(FBUtilities.absoluteFromFraction(v, expectedRows), Integer.MAX_VALUE);
    }
    public static KSMetaData getTableDefinition(String table)
    {
        return tables.get(table);
    }
    public static void setTableDefinition(KSMetaData ksm, UUID newVersion)
    {
        if (ksm != null)
            tables.put(ksm.name, ksm);
        DatabaseDescriptor.defsVersion = newVersion;
    }
    public static void clearTableDefinition(KSMetaData ksm, UUID newVersion)
    {
        tables.remove(ksm.name);
        DatabaseDescriptor.defsVersion = newVersion;
    }
    public static UUID getDefsVersion()
    {
        return defsVersion;
    }
    public static InetAddress getListenAddress()
    {
        return listenAddress;
    }
    public static InetAddress getRpcAddress()
    {
        return rpcAddress;
    }
    public static boolean getRpcKeepAlive()
    {
        return conf.rpc_keepalive;
    }
    public static Integer getRpcSendBufferSize()
    {
        return conf.rpc_send_buff_size_in_bytes;
    }
    public static Integer getRpcRecvBufferSize()
    {
        return conf.rpc_recv_buff_size_in_bytes;
    }
    public static double getCommitLogSyncBatchWindow()
    {
        return conf.commitlog_sync_batch_window_in_ms;
    }
    public static int getCommitLogSyncPeriod() {
        return conf.commitlog_sync_period_in_ms;
    }
    public static Config.CommitLogSync getCommitLogSync()
    {
        return conf.commitlog_sync;
    }
    public static Config.DiskAccessMode getDiskAccessMode()
    {
        return conf.disk_access_mode;
    }
    public static Config.DiskAccessMode getIndexAccessMode()
    {
        return indexAccessMode;
    }
    public static int getIndexedReadBufferSizeInKB()
    {
        return conf.column_index_size_in_kb;
    }
    public static int getSlicedReadBufferSizeInKB()
    {
        return conf.sliced_buffer_size_in_kb;
    }
    public static int getBMTThreshold()
    {
        return conf.binary_memtable_throughput_in_mb;
    }
    public static int getCompactionThreadPriority()
    {
        return conf.compaction_thread_priority;
    }
    public static boolean isSnapshotBeforeCompaction()
    {
        return conf.snapshot_before_compaction;
    }
    public static boolean isAutoBootstrap()
    {
        return conf.auto_bootstrap;
    }
    public static boolean hintedHandoffEnabled()
    {
        return conf.hinted_handoff_enabled;
    }
    public static int getMaxHintWindow()
    {
        return conf.max_hint_window_in_ms;
    }
    public static AbstractType getValueValidator(String keyspace, String cf, ByteBuffer column)
    {
        return getCFMetaData(keyspace, cf).getValueValidator(column);
    }
    public static CFMetaData getCFMetaData(Descriptor desc)
    {
        return getCFMetaData(desc.ksname, desc.cfname);
    }
    public static Integer getIndexInterval()
    {
        return conf.index_interval;
    }
    public static File getSerializedRowCachePath(String ksName, String cfName)
    {
        return new File(conf.saved_caches_directory + File.separator + ksName + "-" + cfName + "-RowCache");
    }
    public static File getSerializedKeyCachePath(String ksName, String cfName)
    {
        return new File(conf.saved_caches_directory + File.separator + ksName + "-" + cfName + "-KeyCache");
    }
    public static int getDynamicUpdateInterval()
    {
        return conf.dynamic_snitch_update_interval_in_ms;
    }
    public static void setDynamicUpdateInterval(Integer dynamicUpdateInterval)
    {
        conf.dynamic_snitch_update_interval_in_ms = dynamicUpdateInterval;
    }
    public static int getDynamicResetInterval()
    {
        return conf.dynamic_snitch_reset_interval_in_ms;
    }
    public static void setDynamicResetInterval(Integer dynamicResetInterval)
    {
        conf.dynamic_snitch_reset_interval_in_ms = dynamicResetInterval;
    }
    public static double getDynamicBadnessThreshold()
    {
        return conf.dynamic_snitch_badness_threshold;
    }
    public static void setDynamicBadnessThreshold(Double dynamicBadnessThreshold)
    {
        conf.dynamic_snitch_badness_threshold = dynamicBadnessThreshold;
    }
    public static EncryptionOptions getEncryptionOptions()
    {
        return conf.encryption_options;
    }
}
