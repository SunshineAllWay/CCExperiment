package org.apache.cassandra.utils;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
public class SimpleCondition implements Condition
{
    boolean set;
    public synchronized void await() throws InterruptedException
    {
        while (!set)
            wait();
    }
    public synchronized void reset()
    {
        set = false;
    }
    public synchronized boolean await(long time, TimeUnit unit) throws InterruptedException
    {
        assert unit == TimeUnit.DAYS || unit == TimeUnit.HOURS || unit == TimeUnit.MINUTES || unit == TimeUnit.SECONDS || unit == TimeUnit.MILLISECONDS;
        long end = System.currentTimeMillis() + unit.convert(time, TimeUnit.MILLISECONDS);
        while (!set && end > System.currentTimeMillis())
        {
            TimeUnit.MILLISECONDS.timedWait(this, end - System.currentTimeMillis());
        }
        return set;
    }
    public synchronized void signal()
    {
        set = true;
        notify();
    }
    public synchronized void signalAll()
    {
        set = true;
        notifyAll();
    }
    public synchronized boolean isSignaled()
    {
        return set;
    }
    public void awaitUninterruptibly()
    {
        throw new UnsupportedOperationException();
    }
    public long awaitNanos(long nanosTimeout) throws InterruptedException
    {
        throw new UnsupportedOperationException();
    }
    public boolean awaitUntil(Date deadline) throws InterruptedException
    {
        throw new UnsupportedOperationException();
    }
}
