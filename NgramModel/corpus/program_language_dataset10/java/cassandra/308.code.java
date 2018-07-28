package org.apache.cassandra.locator;
import java.net.UnknownHostException;
public interface EndpointSnitchInfoMBean
{
    public String getRack(String host) throws UnknownHostException;
    public String getDatacenter(String host) throws UnknownHostException;
}
