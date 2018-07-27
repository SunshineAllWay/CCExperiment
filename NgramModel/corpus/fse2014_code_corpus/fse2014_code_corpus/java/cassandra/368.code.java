package org.apache.cassandra.service;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.net.IAsyncCallback;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.utils.SimpleCondition;
public class RepairCallback<T> implements IAsyncCallback
{
    private final IResponseResolver<T> resolver;
    private final List<InetAddress> endpoints;
    private final SimpleCondition condition = new SimpleCondition();
    private final long startTime;
    public RepairCallback(IResponseResolver<T> resolver, List<InetAddress> endpoints)
    {
        this.resolver = resolver;
        this.endpoints = endpoints;
        this.startTime = System.currentTimeMillis();
    }
    public T get() throws TimeoutException, DigestMismatchException, IOException
    {
        long timeout = DatabaseDescriptor.getRpcTimeout() - (System.currentTimeMillis() - startTime);
        try
        {
            condition.await(timeout, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException ex)
        {
            throw new AssertionError(ex);
        }
        return resolver.isDataPresent() ? resolver.resolve() : null;
    }
    public void response(Message message)
    {
        resolver.preprocess(message);
        if (resolver.getMessageCount() == endpoints.size())
            condition.signal();
    }
}
