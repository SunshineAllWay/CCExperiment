package org.apache.cassandra.locator;
public interface ILatencyPublisher
{
    public void register(ILatencySubscriber subcriber);
}
