package org.apache.cassandra.service;
import java.net.InetAddress;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.google.common.collect.Multimap;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.SimpleCondition;
public abstract class AbstractWriteResponseHandler implements IWriteResponseHandler
{
    protected final SimpleCondition condition = new SimpleCondition();
    protected final long startTime;
    protected final Collection<InetAddress> writeEndpoints;
    protected final Multimap<InetAddress, InetAddress> hintedEndpoints;
    protected final ConsistencyLevel consistencyLevel;
    protected AbstractWriteResponseHandler(Collection<InetAddress> writeEndpoints, Multimap<InetAddress, InetAddress> hintedEndpoints, ConsistencyLevel consistencyLevel)
    {
        startTime = System.currentTimeMillis();
        this.consistencyLevel = consistencyLevel;
        this.hintedEndpoints = hintedEndpoints;
        this.writeEndpoints = writeEndpoints;
    }
    public void get() throws TimeoutException
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
            throw new TimeoutException();
        }
    }
    public void addHintCallback(Message hintedMessage, InetAddress destination)
    {
        if (writeEndpoints.contains(destination) || consistencyLevel == ConsistencyLevel.ANY)
            MessagingService.instance().addCallback(this, hintedMessage.getMessageId());
    }
    public abstract void response(Message msg);
    public abstract void assureSufficientLiveNodes() throws UnavailableException;
}
