package org.apache.cassandra.service;
import java.net.InetAddress;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.locator.IEndpointSnitch;
import org.apache.cassandra.locator.NetworkTopologyStrategy;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.FBUtilities;
public class DatacenterReadCallback<T> extends ReadCallback<T>
{
    private static final IEndpointSnitch snitch = DatabaseDescriptor.getEndpointSnitch();
	private static final String localdc = snitch.getDatacenter(FBUtilities.getLocalAddress());
    private AtomicInteger localResponses;
    public DatacenterReadCallback(IResponseResolver<T> resolver, ConsistencyLevel consistencyLevel, String table)
    {
        super(resolver, consistencyLevel, table);
        localResponses = new AtomicInteger(blockfor);
    }
    @Override
    public void response(Message message)
    {
        resolver.preprocess(message);
        int n;
        n = localdc.equals(snitch.getDatacenter(message.getFrom())) 
                ? localResponses.decrementAndGet()
                : localResponses.get();
        if (n == 0 && resolver.isDataPresent())
        {
            condition.signal();
        }
    }
    @Override
    public int determineBlockFor(ConsistencyLevel consistency_level, String table)
	{
        NetworkTopologyStrategy stategy = (NetworkTopologyStrategy) Table.open(table).getReplicationStrategy();
		return (stategy.getReplicationFactor(localdc) / 2) + 1;
	}
    @Override
    public void assureSufficientLiveNodes(Collection<InetAddress> endpoints) throws UnavailableException
    {
        int localEndpoints = 0;
        for (InetAddress endpoint : endpoints)
        {
            if (localdc.equals(snitch.getDatacenter(endpoint)))
                localEndpoints++;
        }
        if(localEndpoints < blockfor)
            throw new UnavailableException();
    }
}
