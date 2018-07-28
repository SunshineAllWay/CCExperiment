package org.apache.cassandra.cql.driver;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class ConnectionPool implements IConnectionPool
{
    public static final int DEFAULT_MAX_CONNECTIONS = 25;
    public static final int DEFAULT_PORT = 9160;
    public static final int DEFAULT_MAX_IDLE = 5;
    public static final long DEFAULT_EVICTION_DELAY_MILLIS = 10000; 
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    private ConcurrentLinkedQueue<Connection> connections = new ConcurrentLinkedQueue<Connection>();
    private Timer eviction;
    private String hostName;
    private int portNo;
    private int maxConns, maxIdle;
    public ConnectionPool(String hostName) throws TTransportException
    {
        this(hostName, DEFAULT_PORT, DEFAULT_MAX_CONNECTIONS, DEFAULT_MAX_IDLE, DEFAULT_EVICTION_DELAY_MILLIS);
    }
    public ConnectionPool(String hostName, int portNo) throws TTransportException
    {
        this(hostName, portNo, DEFAULT_MAX_CONNECTIONS, DEFAULT_MAX_IDLE, DEFAULT_EVICTION_DELAY_MILLIS);
    }
    public ConnectionPool(String hostName, int portNo, int maxConns, int maxIdle, long evictionDelay)
    throws TTransportException
    {
        this.hostName = hostName;
        this.portNo = portNo;
        this.maxConns = maxConns;
        this.maxIdle = maxIdle;
        eviction = new Timer("EVICTION-THREAD", true);
        eviction.schedule(new EvictionTask(), new Date(), evictionDelay);
        connections.add(new Connection(hostName, portNo));
    }
    public Connection borrowConnection()
    {
        Connection conn = null;
        if ((conn = connections.poll()) == null)
        {
            try
            {
                conn = new Connection(hostName, portNo);
            }
            catch (TTransportException error)
            {
                logger.error(String.format("Error connecting to %s:%s", hostName, portNo), error);
            }
        }
        return conn;
    }
    public void returnConnection(Connection connection)
    {
        if (connections.size() >= maxConns)
        {
            if (connection.isOpen()) connection.close();
            logger.warn("Max pool size reached; Connection discarded.");
            return;
        }
        if (!connection.isOpen())
        {
            logger.warn("Stubbornly refusing to return a closed connection to the pool (discarded instead).");
            return;
        }
        connections.add(connection);
    }
    private class EvictionTask extends TimerTask
    {
        public void run()
        {
            int count = 0;
            while (connections.size() > maxIdle)
            {
                Connection conn = connections.poll();
                if (conn.isOpen()) conn.close();
                count++;
            }
            if (count > 0)
                logger.debug("Eviction run complete: {} connections evicted.", count);
        }
    }
}
