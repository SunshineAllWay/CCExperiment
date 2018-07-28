package org.apache.cassandra.service;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.net.IAsyncCallback;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.SimpleCondition;
public class ReadCallback<T> implements IAsyncCallback
{
    protected static final Logger logger = LoggerFactory.getLogger( ReadCallback.class );
    public final IResponseResolver<T> resolver;
    protected final SimpleCondition condition = new SimpleCondition();
    private final long startTime;
    protected final int blockfor;
    public ReadCallback(IResponseResolver<T> resolver, ConsistencyLevel consistencyLevel, String table)
    {
        this.blockfor = determineBlockFor(consistencyLevel, table);
        this.resolver = resolver;
        this.startTime = System.currentTimeMillis();
        logger.debug("ReadCallback blocking for {} responses", blockfor);
    }
    public T get() throws TimeoutException, DigestMismatchException, IOException
    {
        long timeout = DatabaseDescriptor.getRpcTimeout() - (System.currentTimeMillis() - startTime);
        boolean success;
        try
        {
            success = condition.await(timeout, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException ex)
        {
            throw new AssertionError(ex);
        }
        if (!success)
        {
            StringBuilder sb = new StringBuilder("");
            for (Message message : resolver.getMessages())
                sb.append(message.getFrom()).append(", ");
            throw new TimeoutException("Operation timed out - received only " + resolver.getMessageCount() + " responses from " + sb.toString() + " .");
        }
        return blockfor == 1 ? resolver.getData() : resolver.resolve();
    }
    public void response(Message message)
    {
        resolver.preprocess(message);
        if (resolver.getMessageCount() < blockfor)
            return;
        if (resolver.isDataPresent())
            condition.signal();
    }
    public int determineBlockFor(ConsistencyLevel consistencyLevel, String table)
    {
        switch (consistencyLevel)
        {
            case ONE:
            case ANY:
                return 1;
            case QUORUM:
                return (Table.open(table).getReplicationStrategy().getReplicationFactor() / 2) + 1;
            case ALL:
                return Table.open(table).getReplicationStrategy().getReplicationFactor();
            default:
                throw new UnsupportedOperationException("invalid consistency level: " + consistencyLevel);
        }
    }
    public void assureSufficientLiveNodes(Collection<InetAddress> endpoints) throws UnavailableException
    {
        if (endpoints.size() < blockfor)
            throw new UnavailableException();
    }
}
