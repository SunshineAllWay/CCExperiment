package org.apache.cassandra.utils;
import java.util.concurrent.atomic.AtomicLong;
public class LatencyTracker
{
    private final AtomicLong opCount = new AtomicLong(0);
    private final AtomicLong totalLatency = new AtomicLong(0);
    private long lastLatency = 0;
    private long lastOpCount = 0;
    private EstimatedHistogram totalHistogram = new EstimatedHistogram();
    private EstimatedHistogram recentHistogram = new EstimatedHistogram();
    public void addNano(long nanos)
    {
        addMicro(nanos / 1000);
    }
    public void addMicro(long micros)
    {
        opCount.incrementAndGet();
        totalLatency.addAndGet(micros);
        totalHistogram.add(micros);
        recentHistogram.add(micros);
    }
    public long getOpCount()
    {
        return opCount.get();
    }
    public long getTotalLatencyMicros()
    {
        return totalLatency.get();
    }
    public double getRecentLatencyMicros()
    {
        long ops = opCount.get();
        long n = totalLatency.get();
        try
        {
            return ((double)n - lastLatency) / (ops - lastOpCount);
        }
        finally
        {
            lastLatency = n;
            lastOpCount = ops;
        }
    }
    public long[] getTotalLatencyHistogramMicros()
    {
        return totalHistogram.get(false);
    }
    public long[] getRecentLatencyHistogramMicros()
    {
        return recentHistogram.get(true);
    }
}
