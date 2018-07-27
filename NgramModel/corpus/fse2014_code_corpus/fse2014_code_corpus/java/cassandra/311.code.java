package org.apache.cassandra.locator;
import java.net.InetAddress;
public interface ILatencySubscriber
{
    public void receiveTiming(InetAddress address, Double latency);
}
