package org.apache.cassandra.locator;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.cassandra.dht.Token;
public class SimpleStrategy extends AbstractReplicationStrategy
{
    public SimpleStrategy(String table, TokenMetadata tokenMetadata, IEndpointSnitch snitch, Map<String, String> configOptions)
    {
        super(table, tokenMetadata, snitch, configOptions);
    }
    public List<InetAddress> calculateNaturalEndpoints(Token token, TokenMetadata metadata)
    {
        int replicas = getReplicationFactor();
        ArrayList<Token> tokens = metadata.sortedTokens();
        List<InetAddress> endpoints = new ArrayList<InetAddress>(replicas);
        if (tokens.isEmpty())
            return endpoints;
        Iterator<Token> iter = TokenMetadata.ringIterator(tokens, token, false);
        while (endpoints.size() < replicas && iter.hasNext())
        {
            endpoints.add(metadata.getEndpoint(iter.next()));
        }
        if (endpoints.size() < replicas)
            throw new IllegalStateException(String.format("replication factor (%s) exceeds number of endpoints (%s)", replicas, endpoints.size()));
        return endpoints;
    }
}
