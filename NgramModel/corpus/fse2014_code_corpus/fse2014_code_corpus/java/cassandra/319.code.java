package org.apache.cassandra.locator;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
public class SimpleSnitch extends AbstractEndpointSnitch
{
    public String getRack(InetAddress endpoint)
    {
        return "rack1";
    }
    public String getDatacenter(InetAddress endpoint)
    {
        return "datacenter1";
    }
    public List<InetAddress> getSortedListByProximity(final InetAddress address, Collection<InetAddress> addresses)
    {
        return new ArrayList<InetAddress>(addresses);
    }
    public void sortByProximity(final InetAddress address, List<InetAddress> addresses)
    {
    }
}
