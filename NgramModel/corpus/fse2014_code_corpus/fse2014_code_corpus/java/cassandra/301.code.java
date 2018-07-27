package org.apache.cassandra.locator;
import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public abstract class AbstractEndpointSnitch implements IEndpointSnitch
{
    private static final Logger logger = LoggerFactory.getLogger(AbstractEndpointSnitch.class);
    public abstract List<InetAddress> getSortedListByProximity(InetAddress address, Collection<InetAddress> unsortedAddress);
    public abstract void sortByProximity(InetAddress address, List<InetAddress> addresses);
    public int compareEndpoints(InetAddress target, InetAddress a1, InetAddress a2)
    {
        return a1.getHostAddress().compareTo(a2.getHostAddress());
    }
    public void gossiperStarting()
    {
    }
}
