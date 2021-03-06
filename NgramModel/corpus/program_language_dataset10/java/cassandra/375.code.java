package org.apache.cassandra.service;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.UnavailableException;
public class WriteResponseHandler extends AbstractWriteResponseHandler
{
    protected static final Logger logger = LoggerFactory.getLogger(WriteResponseHandler.class);
    protected final AtomicInteger responses;
    protected WriteResponseHandler(Collection<InetAddress> writeEndpoints, Multimap<InetAddress, InetAddress> hintedEndpoints, ConsistencyLevel consistencyLevel, String table)
    {
        super(writeEndpoints, hintedEndpoints, consistencyLevel);
        responses = new AtomicInteger(determineBlockFor(table));
    }
    protected WriteResponseHandler(InetAddress endpoint)
    {
        super(Arrays.asList(endpoint),
              ImmutableMultimap.<InetAddress, InetAddress>builder().put(endpoint, endpoint).build(),
              ConsistencyLevel.ALL);
        responses = new AtomicInteger(1);
    }
    public static IWriteResponseHandler create(Collection<InetAddress> writeEndpoints, Multimap<InetAddress, InetAddress> hintedEndpoints, ConsistencyLevel consistencyLevel, String table)
    {
        return new WriteResponseHandler(writeEndpoints, hintedEndpoints, consistencyLevel, table);
    }
    public static IWriteResponseHandler create(InetAddress endpoint)
    {
        return new WriteResponseHandler(endpoint);
    }
    public void response(Message m)
    {
        if (responses.decrementAndGet() == 0)
            condition.signal();
    }
    protected int determineBlockFor(String table)
    {
        int blockFor = 0;
        switch (consistencyLevel)
        {
            case ONE:
                blockFor = 1;
                break;
            case ANY:
                blockFor = 1;
                break;
            case QUORUM:
                blockFor = (writeEndpoints.size() / 2) + 1;
                break;
            case ALL:
                blockFor = writeEndpoints.size();
                break;
            default:
                throw new UnsupportedOperationException("invalid consistency level: " + consistencyLevel.toString());
        }
        assert 1 <= blockFor && blockFor <= 2 * Table.open(table).getReplicationStrategy().getReplicationFactor()
            : String.format("invalid response count %d for replication factor %d",
                            blockFor, Table.open(table).getReplicationStrategy().getReplicationFactor());
        return blockFor;
    }
    public void assureSufficientLiveNodes() throws UnavailableException
    {
        if (consistencyLevel == ConsistencyLevel.ANY)
        {
            if (hintedEndpoints.keySet().size() < responses.get())
                throw new UnavailableException();
        }
        int liveNodes = 0;
        for (InetAddress destination : hintedEndpoints.keySet())
        {
            if (writeEndpoints.contains(destination))
                liveNodes++;
        }
        if (liveNodes < responses.get())
        {
            throw new UnavailableException();
        }
    }
}
