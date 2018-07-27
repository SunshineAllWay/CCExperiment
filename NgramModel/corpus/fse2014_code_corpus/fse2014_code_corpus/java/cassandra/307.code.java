package org.apache.cassandra.locator;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.cassandra.config.DatabaseDescriptor;
public class EndpointSnitchInfo implements EndpointSnitchInfoMBean
{
    public static void create()
    {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try
        {
            mbs.registerMBean(new EndpointSnitchInfo(), new ObjectName("org.apache.cassandra.db:type=EndpointSnitchInfo"));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    public String getDatacenter(String host) throws UnknownHostException
    {
        return DatabaseDescriptor.getEndpointSnitch().getDatacenter(InetAddress.getByName(host));
    }
    public String getRack(String host) throws UnknownHostException
    {
        return DatabaseDescriptor.getEndpointSnitch().getRack(InetAddress.getByName(host));
    }
}
