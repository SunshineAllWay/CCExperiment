package org.apache.cassandra.service;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.net.IAsyncCallback;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.utils.SimpleCondition;
public class TruncateResponseHandler implements IAsyncCallback
{
    protected static final Logger logger = LoggerFactory.getLogger(TruncateResponseHandler.class);
    protected final SimpleCondition condition = new SimpleCondition();
    private final int responseCount;
    protected AtomicInteger responses = new AtomicInteger(0);
    private final long startTime;
    public TruncateResponseHandler(int responseCount)
    {
        assert 1 <= responseCount: "invalid response count " + responseCount;
        this.responseCount = responseCount;
        startTime = System.currentTimeMillis();
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
            throw new TimeoutException("Truncate timed out - received only " + responses.get() + " responses");
        }
    }
    public void response(Message message)
    {
        responses.incrementAndGet();
        if (responses.get() >= responseCount)
            condition.signal();
    }
}
