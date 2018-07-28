package org.apache.cassandra.service;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.CompactionManager;
import org.apache.cassandra.db.SystemTable;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.db.commitlog.CommitLog;
import org.apache.cassandra.db.migration.Migration;
import org.apache.cassandra.utils.CLibrary;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Mx4jTool;
import org.mortbay.thread.ThreadPool;
public abstract class AbstractCassandraDaemon implements CassandraDaemon
{
    static
    {
        String config = System.getProperty("log4j.configuration", "log4j-server.properties");
        URL configLocation = null;
        try 
        {
            configLocation = new URL(config);
        }
        catch (MalformedURLException ex) 
        {
            configLocation = AbstractCassandraDaemon.class.getClassLoader().getResource(config);
            if (configLocation == null)
                throw new RuntimeException("Couldn't figure out log4j configuration.");
        }
        PropertyConfigurator.configureAndWatch(configLocation.getFile(), 10000);
        org.apache.log4j.Logger.getLogger(AbstractCassandraDaemon.class).info("Logging initialized");
    }
    private static Logger logger = LoggerFactory.getLogger(AbstractCassandraDaemon.class);
    protected InetAddress listenAddr;
    protected int listenPort;
    public static final int MIN_WORKER_THREADS = 64;
    protected void setup() throws IOException
    {
        logger.info("Heap size: {}/{}", Runtime.getRuntime().totalMemory(), Runtime.getRuntime().maxMemory());
    	CLibrary.tryMlockall();
        listenPort = DatabaseDescriptor.getRpcPort();
        listenAddr = DatabaseDescriptor.getRpcAddress();
        if (listenAddr == null)
            listenAddr = FBUtilities.getLocalAddress();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            public void uncaughtException(Thread t, Throwable e)
            {
                logger.error("Fatal exception in thread " + t, e);
                if (e instanceof OutOfMemoryError)
                {
                    System.exit(100);
                }
            }
        });
        for (CFMetaData cfm : DatabaseDescriptor.getTableMetaData(Table.SYSTEM_TABLE).values())
            ColumnFamilyStore.scrubDataDirectories(Table.SYSTEM_TABLE, cfm.cfName);
        try
        {
            SystemTable.checkHealth();
        }
        catch (ConfigurationException e)
        {
            logger.error("Fatal exception during initialization", e);
            System.exit(100);
        }
        try
        {
            DatabaseDescriptor.loadSchemas();
        }
        catch (IOException e)
        {
            logger.error("Fatal exception during initialization", e);
            System.exit(100);
        }
        for (String table : DatabaseDescriptor.getTables()) 
        {
            for (CFMetaData cfm : DatabaseDescriptor.getTableMetaData(table).values())
            {
                ColumnFamilyStore.scrubDataDirectories(table, cfm.cfName);
            }
        }
        for (String table : DatabaseDescriptor.getTables())
        {
            if (logger.isDebugEnabled())
                logger.debug("opening keyspace " + table);
            Table.open(table);
        }
        CommitLog.recover();
        CompactionManager.instance.checkAllColumnFamilies();
        UUID currentMigration = DatabaseDescriptor.getDefsVersion();
        UUID lastMigration = Migration.getLastMigrationId();
        if ((lastMigration != null) && (lastMigration.timestamp() > currentMigration.timestamp()))
        {
            MigrationManager.applyMigrations(currentMigration, lastMigration);
        }
        SystemTable.purgeIncompatibleHints();
        try
        {
            StorageService.instance.initServer();
        }
        catch (ConfigurationException e)
        {
            logger.error("Fatal error: " + e.getMessage());
            System.err.println("Bad configuration; unable to start server");
            System.exit(1);
        }
        Mx4jTool.maybeLoad();
    }
    public void init(String[] arguments) throws IOException
    {
        setup();
    }
    public abstract void start() throws IOException;
    public abstract void stop();
    public void destroy()
    {}
    public void activate()
    {
        String pidFile = System.getProperty("cassandra-pidfile");
        try
        {
            setup();
            if (pidFile != null)
            {
                new File(pidFile).deleteOnExit();
            }
            if (System.getProperty("cassandra-foreground") == null)
            {
                System.out.close();
                System.err.close();
            }
            start();
        } catch (Throwable e)
        {
            String msg = "Exception encountered during startup.";
            logger.error(msg, e);
            System.out.println(msg);
            e.printStackTrace();
            System.exit(3);
        }
    }
    public void deactivate()
    {
        stop();
        destroy();
    }
    public static class CleaningThreadPool extends ThreadPoolExecutor implements ThreadPool
    {
        private ThreadLocal<ClientState> state;
        public CleaningThreadPool(ThreadLocal<ClientState> state, int minWorkerThread, int maxWorkerThreads)
        {
            super(minWorkerThread, maxWorkerThreads, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
            this.state = state;
        }
        @Override
        protected void afterExecute(Runnable r, Throwable t)
        {
            super.afterExecute(r, t);
            state.get().logout();
        }
        public boolean dispatch(Runnable job)
        {
            try
            {       
                execute(job);
                return true;
            }
            catch(RejectedExecutionException e)
            {
                logger.error("Failed to dispatch thread:", e);
                return false;
            }
        }
        public int getIdleThreads()
        {
            return getPoolSize()-getActiveCount();
        }
        public int getThreads()
        {
            return getPoolSize();
        }
        public boolean isLowOnThreads()
        {
            return getActiveCount()>=getMaximumPoolSize();
        }
        public void join() throws InterruptedException
        {
            this.awaitTermination(Long.MAX_VALUE,TimeUnit.MILLISECONDS);
        }
    }
}
