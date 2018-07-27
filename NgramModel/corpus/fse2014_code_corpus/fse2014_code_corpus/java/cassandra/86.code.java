package org.apache.cassandra.client;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.TokenRange;
import org.apache.thrift.TException;
import org.apache.cassandra.thrift.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
public class RingCache
{
    final private static Logger logger_ = LoggerFactory.getLogger(RingCache.class);
    private final Set<String> seeds_ = new HashSet<String>();
    private final int port_;
    private final IPartitioner partitioner_;
    private final String keyspace;
    private Multimap<Range, InetAddress> rangeMap;
    public RingCache(String keyspace, IPartitioner partitioner, String addresses, int port) throws IOException
    {
        for (String seed : addresses.split(","))
            seeds_.add(seed);
        this.port_ = port;
        this.keyspace = keyspace;
        this.partitioner_ = partitioner;
        refreshEndpointMap();
    }
    public void refreshEndpointMap()
    {
        for (String seed : seeds_)
        {
            try
            {
                TSocket socket = new TSocket(seed, port_);
                TBinaryProtocol binaryProtocol = new TBinaryProtocol(new TFramedTransport(socket));
                Cassandra.Client client = new Cassandra.Client(binaryProtocol);
                socket.open();
                List<TokenRange> ring = client.describe_ring(keyspace);
                rangeMap = ArrayListMultimap.create();
                for (TokenRange range : ring)
                {
                    Token<?> left = partitioner_.getTokenFactory().fromString(range.start_token);
                    Token<?> right = partitioner_.getTokenFactory().fromString(range.end_token);
                    Range r = new Range(left, right, partitioner_);
                    for (String host : range.endpoints)
                    {
                        try
                        {
                            rangeMap.put(r, InetAddress.getByName(host));
                        }
                        catch (UnknownHostException e)
                        {
                            throw new AssertionError(e); 
                        }
                    }
                }
                break;
            }
            catch (InvalidRequestException e)
            {
                throw new RuntimeException(e);
            }
            catch (TException e)
            {
                logger_.debug("Error contacting seed " + seed + " " + e.getMessage());
            }
        }
    }
    @SuppressWarnings(value="unchecked")
    public List<InetAddress> getEndpoint(Range range)
    {
        return (List<InetAddress>) rangeMap.get(range);
    }
    public List<InetAddress> getEndpoint(ByteBuffer key)
    {
        return getEndpoint(getRange(key));
    }
    public Range getRange(ByteBuffer key)
    {
        Token<?> t = partitioner_.getToken(key);
        for (Range range : rangeMap.keySet())
            if (range.contains(t))
                return range;
        throw new RuntimeException("Invalid token information returned by describe_ring: " + rangeMap);
    }
}
