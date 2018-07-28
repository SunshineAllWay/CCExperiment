package org.apache.cassandra.service;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.collect.Multimap;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.locator.IEndpointSnitch;
import org.apache.cassandra.locator.NetworkTopologyStrategy;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.FBUtilities;
public class DatacenterSyncWriteResponseHandler extends AbstractWriteResponseHandler
{
    private static final IEndpointSnitch snitch = DatabaseDescriptor.getEndpointSnitch();
    private static final String localdc;
    static
    {
        localdc = snitch.getDatacenter(FBUtilities.getLocalAddress());
    }
	private final NetworkTopologyStrategy strategy;
    private HashMap<String, AtomicInteger> responses = new HashMap<String, AtomicInteger>();
    protected DatacenterSyncWriteResponseHandler(Collection<InetAddress> writeEndpoints, Multimap<InetAddress, InetAddress> hintedEndpoints, ConsistencyLevel consistencyLevel, String table)
    {
        super(writeEndpoints, hintedEndpoints, consistencyLevel);
        assert consistencyLevel == ConsistencyLevel.LOCAL_QUORUM;
        strategy = (NetworkTopologyStrategy) Table.open(table).getReplicationStrategy();
        for (String dc : strategy.getDatacenters())
        {
            int rf = strategy.getReplicationFactor(dc);
            responses.put(dc, new AtomicInteger((rf / 2) + 1));
        }
    }
    public static IWriteResponseHandler create(Collection<InetAddress> writeEndpoints, Multimap<InetAddress, InetAddress> hintedEndpoints, ConsistencyLevel consistencyLevel, String table)
    {
        return new DatacenterSyncWriteResponseHandler(writeEndpoints, hintedEndpoints, consistencyLevel, table);
    }
    public void response(Message message)
    {
        String dataCenter = message == null
                            ? localdc
                            : snitch.getDatacenter(message.getFrom());
        responses.get(dataCenter).getAndDecrement();
        for (AtomicInteger i : responses.values())
        {
            if (0 < i.get())
                return;
        }
        condition.signal();
    }
    public void assureSufficientLiveNodes() throws UnavailableException
    {   
		Map<String, AtomicInteger> dcEndpoints = new HashMap<String, AtomicInteger>();
        for (String dc: strategy.getDatacenters())
            dcEndpoints.put(dc, new AtomicInteger());
        for (InetAddress destination : hintedEndpoints.keySet())
        {
            assert writeEndpoints.contains(destination);
            String destinationDC = snitch.getDatacenter(destination);
            dcEndpoints.get(destinationDC).incrementAndGet();
        }
        for (String dc: strategy.getDatacenters())
        {
        	if (dcEndpoints.get(dc).get() != responses.get(dc).get())
                throw new UnavailableException();
        }
    }
}
