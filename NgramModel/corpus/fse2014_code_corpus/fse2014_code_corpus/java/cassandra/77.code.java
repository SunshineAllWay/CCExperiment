package org.apache.cassandra.cache;
public interface JMXInstrumentedCacheMBean
{
    public int getCapacity();
    public void setCapacity(int capacity);
    public int getSize();
    public long getRequests();
    public long getHits();
    public double getRecentHitRate();
}
