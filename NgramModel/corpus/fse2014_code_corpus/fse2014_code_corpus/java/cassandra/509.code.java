package org.apache.cassandra.locator;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.dht.StringToken;
import org.apache.cassandra.dht.Token;
import org.xml.sax.SAXException;
public class NetworkTopologyStrategyTest
{
    private String table = "Keyspace1";
    @Test
    public void testProperties() throws IOException, ParserConfigurationException, SAXException, ConfigurationException
    {
        IEndpointSnitch snitch = new PropertyFileSnitch();
        TokenMetadata metadata = new TokenMetadata();
        createDummyTokens(metadata);
        Map<String, String> configOptions = new HashMap<String, String>();
        configOptions.put("DC1", "3");
        configOptions.put("DC2", "2");
        configOptions.put("DC3", "1");
        NetworkTopologyStrategy strategy = new NetworkTopologyStrategy(table, metadata, snitch, configOptions);
        assert strategy.getReplicationFactor("DC1") == 3;
        assert strategy.getReplicationFactor("DC2") == 2;
        assert strategy.getReplicationFactor("DC3") == 1;
        ArrayList<InetAddress> endpoints = strategy.getNaturalEndpoints(new StringToken("123"));
        assert 6 == endpoints.size();
        assert 6 == new HashSet<InetAddress>(endpoints).size(); 
    }
    public void createDummyTokens(TokenMetadata metadata) throws UnknownHostException
    {
        tokenFactory(metadata, "123", new byte[]{ 10, 0, 0, 10 });
        tokenFactory(metadata, "234", new byte[]{ 10, 0, 0, 11 });
        tokenFactory(metadata, "345", new byte[]{ 10, 0, 0, 12 });
        tokenFactory(metadata, "789", new byte[]{ 10, 20, 114, 10 });
        tokenFactory(metadata, "890", new byte[]{ 10, 20, 114, 11 });
        tokenFactory(metadata, "456", new byte[]{ 10, 21, 119, 13 });
        tokenFactory(metadata, "567", new byte[]{ 10, 21, 119, 10 });
        tokenFactory(metadata, "90A", new byte[]{ 10, 0, 0, 13 });
        tokenFactory(metadata, "0AB", new byte[]{ 10, 21, 119, 14 });
        tokenFactory(metadata, "ABC", new byte[]{ 10, 20, 114, 15 });
    }
    public void tokenFactory(TokenMetadata metadata, String token, byte[] bytes) throws UnknownHostException
    {
        Token token1 = new StringToken(token);
        InetAddress add1 = InetAddress.getByAddress(bytes);
        metadata.updateNormalToken(token1, add1);
    }
}
