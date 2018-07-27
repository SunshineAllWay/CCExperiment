package org.apache.cassandra.locator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
public abstract class AbstractNetworkTopologySnitch extends AbstractEndpointSnitch
{
    abstract public String getRack(InetAddress endpoint);
    abstract public String getDatacenter(InetAddress endpoint);
    public List<InetAddress> getSortedListByProximity(final InetAddress address, Collection<InetAddress> addresses)
    {
        List<InetAddress> preferred = new ArrayList<InetAddress>(addresses);
        sortByProximity(address, preferred);
        return preferred;
    }
    public void sortByProximity(final InetAddress address, List<InetAddress> addresses)
    {
        Collections.sort(addresses, new Comparator<InetAddress>()
        {
            public int compare(InetAddress a1, InetAddress a2)
            {
                   return compareEndpoints(address, a1, a2);
            }
        });
    }
    public int compareEndpoints(InetAddress address, InetAddress a1, InetAddress a2)
    {
        if (address.equals(a1) && !address.equals(a2))
            return -1;
        if (address.equals(a2) && !address.equals(a1))
            return 1;
        String addressRack = getRack(address);
        String a1Rack = getRack(a1);
        String a2Rack = getRack(a2);
        if (addressRack.equals(a1Rack) && !addressRack.equals(a2Rack))
            return -1;
        if (addressRack.equals(a2Rack) && !addressRack.equals(a1Rack))
            return 1;
        String addressDatacenter = getDatacenter(address);
        String a1Datacenter = getDatacenter(a1);
        String a2Datacenter = getDatacenter(a2);
        if (addressDatacenter.equals(a1Datacenter) && !addressDatacenter.equals(a2Datacenter))
            return -1;
        if (addressDatacenter.equals(a2Datacenter) && !addressDatacenter.equals(a1Datacenter))
            return 1;
        return 0;
    }
}
