package org.apache.cassandra.locator;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.cassandra.dht.Token;
public class OldNetworkTopologyStrategy extends AbstractReplicationStrategy
{
    public OldNetworkTopologyStrategy(String table, TokenMetadata tokenMetadata, IEndpointSnitch snitch, Map<String, String> configOptions)
    {
        super(table, tokenMetadata, snitch, configOptions);
    }
    public List<InetAddress> calculateNaturalEndpoints(Token token, TokenMetadata metadata)
    {
        int replicas = getReplicationFactor();
        List<InetAddress> endpoints = new ArrayList<InetAddress>(replicas);
        ArrayList<Token> tokens = metadata.sortedTokens();
        if (tokens.isEmpty())
            return endpoints;
        Iterator<Token> iter = TokenMetadata.ringIterator(tokens, token, false);
        Token primaryToken = iter.next();
        endpoints.add(metadata.getEndpoint(primaryToken));
        boolean bDataCenter = false;
        boolean bOtherRack = false;
        while (endpoints.size() < replicas && iter.hasNext())
        {
            Token t = iter.next();
            if (!snitch.getDatacenter(metadata.getEndpoint(primaryToken)).equals(snitch.getDatacenter(metadata.getEndpoint(t))))
            {
                if (!bDataCenter)
                {
                    endpoints.add(metadata.getEndpoint(t));
                    bDataCenter = true;
                }
                continue;
            }
            if (!snitch.getRack(metadata.getEndpoint(primaryToken)).equals(snitch.getRack(metadata.getEndpoint(t))) &&
                snitch.getDatacenter(metadata.getEndpoint(primaryToken)).equals(snitch.getDatacenter(metadata.getEndpoint(t))))
            {
                if (!bOtherRack)
                {
                    endpoints.add(metadata.getEndpoint(t));
                    bOtherRack = true;
                }
            }
        }
        if (endpoints.size() < replicas)
        {
            iter = TokenMetadata.ringIterator(tokens, token, false);
            while (endpoints.size() < replicas && iter.hasNext())
            {
                Token t = iter.next();
                if (!endpoints.contains(metadata.getEndpoint(t)))
                    endpoints.add(metadata.getEndpoint(t));
            }
            if (endpoints.size() < replicas)
                throw new IllegalStateException(String.format("replication factor (%s) exceeds number of endpoints (%s)", replicas, endpoints.size()));
        }
        return endpoints;
    }
}
