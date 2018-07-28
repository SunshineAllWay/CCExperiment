package org.apache.cassandra.locator;
import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
public interface IEndpointSnitch
{
    public String getRack(InetAddress endpoint);
    public String getDatacenter(InetAddress endpoint);
    public List<InetAddress> getSortedListByProximity(InetAddress address, Collection<InetAddress> unsortedAddress);
    public void sortByProximity(InetAddress address, List<InetAddress> addresses);
    public int compareEndpoints(InetAddress target, InetAddress a1, InetAddress a2);
    public void gossiperStarting();
}