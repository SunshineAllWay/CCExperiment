package org.apache.cassandra.locator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.dht.*;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.service.StorageServiceAccessor;
import static org.junit.Assert.*;
public class SimpleStrategyTest extends CleanupHelper
{
    @Test
    public void tryValidTable()
    {
        assert Table.open("Keyspace1").getReplicationStrategy() != null;
    }
    @Test
    public void testBigIntegerEndpoints() throws UnknownHostException, ConfigurationException
    {
        List<Token> endpointTokens = new ArrayList<Token>();
        List<Token> keyTokens = new ArrayList<Token>();
        for (int i = 0; i < 5; i++) {
            endpointTokens.add(new BigIntegerToken(String.valueOf(10 * i)));
            keyTokens.add(new BigIntegerToken(String.valueOf(10 * i + 5)));
        }
        verifyGetNaturalEndpoints(endpointTokens.toArray(new Token[0]), keyTokens.toArray(new Token[0]));
    }
    @Test
    public void testStringEndpoints() throws UnknownHostException, ConfigurationException
    {
        IPartitioner partitioner = new OrderPreservingPartitioner();
        List<Token> endpointTokens = new ArrayList<Token>();
        List<Token> keyTokens = new ArrayList<Token>();
        for (int i = 0; i < 5; i++) {
            endpointTokens.add(new StringToken(String.valueOf((char)('a' + i * 2))));
            keyTokens.add(partitioner.getToken(ByteBuffer.wrap(String.valueOf((char)('a' + i * 2 + 1)).getBytes())));
        }
        verifyGetNaturalEndpoints(endpointTokens.toArray(new Token[0]), keyTokens.toArray(new Token[0]));
    }
    private void verifyGetNaturalEndpoints(Token[] endpointTokens, Token[] keyTokens) throws UnknownHostException, ConfigurationException
    {
        TokenMetadata tmd;
        AbstractReplicationStrategy strategy;
        for (String table : DatabaseDescriptor.getNonSystemTables())
        {
            tmd = new TokenMetadata();
            strategy = getStrategy(table, tmd);
            List<InetAddress> hosts = new ArrayList<InetAddress>();
            for (int i = 0; i < endpointTokens.length; i++)
            {
                InetAddress ep = InetAddress.getByName("127.0.0." + String.valueOf(i + 1));
                tmd.updateNormalToken(endpointTokens[i], ep);
                hosts.add(ep);
            }
            for (int i = 0; i < keyTokens.length; i++)
            {
                List<InetAddress> endpoints = strategy.getNaturalEndpoints(keyTokens[i]);
                assertEquals(strategy.getReplicationFactor(), endpoints.size());
                List<InetAddress> correctEndpoints = new ArrayList<InetAddress>();
                for (int j = 0; j < endpoints.size(); j++)
                    correctEndpoints.add(hosts.get((i + j + 1) % hosts.size()));
                assertEquals(new HashSet<InetAddress>(correctEndpoints), new HashSet<InetAddress>(endpoints));
            }
        }
    }
    @Test
    public void testGetEndpointsDuringBootstrap() throws UnknownHostException, ConfigurationException
    {
        final int RING_SIZE = 10;
        TokenMetadata tmd = new TokenMetadata();
        TokenMetadata oldTmd = StorageServiceAccessor.setTokenMetadata(tmd);
        Token[] endpointTokens = new Token[RING_SIZE];
        Token[] keyTokens = new Token[RING_SIZE];
        for (int i = 0; i < RING_SIZE; i++)
        {
            endpointTokens[i] = new BigIntegerToken(String.valueOf(RING_SIZE * 2 * i));
            keyTokens[i] = new BigIntegerToken(String.valueOf(RING_SIZE * 2 * i + RING_SIZE));
        }
        List<InetAddress> hosts = new ArrayList<InetAddress>();
        for (int i = 0; i < endpointTokens.length; i++)
        {
            InetAddress ep = InetAddress.getByName("127.0.0." + String.valueOf(i + 1));
            tmd.updateNormalToken(endpointTokens[i], ep);
            hosts.add(ep);
        }
        Token bsToken = new BigIntegerToken(String.valueOf(210));
        InetAddress bootstrapEndpoint = InetAddress.getByName("127.0.0.11");
        tmd.addBootstrapToken(bsToken, bootstrapEndpoint);
        AbstractReplicationStrategy strategy = null;
        for (String table : DatabaseDescriptor.getNonSystemTables())
        {
            strategy = getStrategy(table, tmd);
            StorageService.calculatePendingRanges(strategy, table);
            int replicationFactor = strategy.getReplicationFactor();
            for (int i = 0; i < keyTokens.length; i++)
            {
                Collection<InetAddress> endpoints = tmd.getWriteEndpoints(keyTokens[i], table, strategy.getNaturalEndpoints(keyTokens[i]));
                assertTrue(endpoints.size() >= replicationFactor);
                for (int j = 0; j < replicationFactor; j++)
                {
                    assertTrue(endpoints.contains(hosts.get((i + j + 1) % hosts.size())));
                }
                if (i < RING_SIZE - replicationFactor)
                    assertFalse(endpoints.contains(bootstrapEndpoint));
                else
                    assertTrue(endpoints.contains(bootstrapEndpoint));
            }
        }
        StorageServiceAccessor.setTokenMetadata(oldTmd);
    }
    private AbstractReplicationStrategy getStrategyWithNewTokenMetadata(AbstractReplicationStrategy strategy, TokenMetadata newTmd) throws ConfigurationException
    {
        return AbstractReplicationStrategy.createReplicationStrategy(
                strategy.table,
                strategy.getClass().getName(),
                newTmd,
                strategy.snitch,
                strategy.configOptions);
    }
    private AbstractReplicationStrategy getStrategy(String table, TokenMetadata tmd) throws ConfigurationException
    {
        return AbstractReplicationStrategy.createReplicationStrategy(
                table,
                "org.apache.cassandra.locator.SimpleStrategy",
                tmd,
                new SimpleSnitch(),
                null);
    }
}
