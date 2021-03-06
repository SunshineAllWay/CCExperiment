package org.apache.cassandra.locator;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.utils.FBUtilities;
public class LocalStrategy extends AbstractReplicationStrategy
{
    public LocalStrategy(String table, TokenMetadata tokenMetadata, IEndpointSnitch snitch, Map<String, String> configOptions)
    {
        super(table, tokenMetadata, snitch, configOptions);
    }
    public List<InetAddress> calculateNaturalEndpoints(Token token, TokenMetadata metadata)
    {
        return Arrays.asList(FBUtilities.getLocalAddress());
    }
}
