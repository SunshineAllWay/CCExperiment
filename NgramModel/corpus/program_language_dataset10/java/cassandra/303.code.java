package org.apache.cassandra.locator;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.util.*;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.cassandra.gms.Gossiper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.gms.FailureDetector;
import org.apache.cassandra.service.*;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.utils.FBUtilities;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
public abstract class AbstractReplicationStrategy
{
    private static final Logger logger = LoggerFactory.getLogger(AbstractReplicationStrategy.class);
    public final String table;
    public final Map<String, String> configOptions;
    private final TokenMetadata tokenMetadata;
    public IEndpointSnitch snitch;
    AbstractReplicationStrategy(String table, TokenMetadata tokenMetadata, IEndpointSnitch snitch, Map<String, String> configOptions)
    {
        assert table != null;
        assert snitch != null;
        assert tokenMetadata != null;
        this.tokenMetadata = tokenMetadata;
        this.snitch = snitch;
        this.tokenMetadata.register(this);
        this.configOptions = configOptions;
        this.table = table;
    }
    private final Map<Token, ArrayList<InetAddress>> cachedEndpoints = new NonBlockingHashMap<Token, ArrayList<InetAddress>>();
    public ArrayList<InetAddress> getCachedEndpoints(Token t)
    {
        return cachedEndpoints.get(t);
    }
    public void cacheEndpoint(Token t, ArrayList<InetAddress> addr)
    {
        cachedEndpoints.put(t, addr);
    }
    public void clearEndpointCache()
    {
        logger.debug("clearing cached endpoints");
        cachedEndpoints.clear();
    }
    public ArrayList<InetAddress> getNaturalEndpoints(Token searchToken) throws IllegalStateException
    {
        Token keyToken = TokenMetadata.firstToken(tokenMetadata.sortedTokens(), searchToken);
        ArrayList<InetAddress> endpoints = getCachedEndpoints(keyToken);
        if (endpoints == null)
        {
            TokenMetadata tokenMetadataClone = tokenMetadata.cloneOnlyTokenMap();
            keyToken = TokenMetadata.firstToken(tokenMetadataClone.sortedTokens(), searchToken);
            endpoints = new ArrayList<InetAddress>(calculateNaturalEndpoints(searchToken, tokenMetadataClone));
            cacheEndpoint(keyToken, endpoints);
            assert getReplicationFactor() <= endpoints.size() : String.format("endpoints %s generated for RF of %s",
                                                                              Arrays.toString(endpoints.toArray()),
                                                                              getReplicationFactor());
        }
        return new ArrayList<InetAddress>(endpoints);
    }
    public abstract List<InetAddress> calculateNaturalEndpoints(Token searchToken, TokenMetadata tokenMetadata) throws IllegalStateException;
    public IWriteResponseHandler getWriteResponseHandler(Collection<InetAddress> writeEndpoints,
                                                         Multimap<InetAddress, InetAddress> hintedEndpoints,
                                                         ConsistencyLevel consistencyLevel)
    {
        return WriteResponseHandler.create(writeEndpoints, hintedEndpoints, consistencyLevel, table);
    }
    public int getReplicationFactor()
    {
        return DatabaseDescriptor.getTableDefinition(table).replicationFactor;
    }
    public Multimap<InetAddress, InetAddress> getHintedEndpoints(Collection<InetAddress> targets)
    {
        Multimap<InetAddress, InetAddress> map = HashMultimap.create(targets.size(), 1);
        for (InetAddress ep : targets)
        {
            if (FailureDetector.instance.isAlive(ep))
                map.put(ep, ep);
        }
        if (map.size() == targets.size() || !StorageProxy.isHintedHandoffEnabled())
            return map;
        InetAddress localAddress = FBUtilities.getLocalAddress();
        for (InetAddress ep : targets)
        {
            if (map.containsKey(ep))
                continue;
            if (!StorageProxy.shouldHint(ep))
            {
                if (logger.isDebugEnabled())
                    logger.debug("not hinting " + ep + " which has been down " + Gossiper.instance.getEndpointDowntime(ep) + "ms");
                continue;
            }
            InetAddress destination = map.isEmpty()
                                    ? localAddress
                                    : snitch.getSortedListByProximity(localAddress, map.keySet()).get(0);
            map.put(destination, ep);
        }
        return map;
    }
    public Multimap<InetAddress, Range> getAddressRanges(TokenMetadata metadata)
    {
        Multimap<InetAddress, Range> map = HashMultimap.create();
        for (Token token : metadata.sortedTokens())
        {
            Range range = metadata.getPrimaryRangeFor(token);
            for (InetAddress ep : calculateNaturalEndpoints(token, metadata))
            {
                map.put(ep, range);
            }
        }
        return map;
    }
    public Multimap<Range, InetAddress> getRangeAddresses(TokenMetadata metadata)
    {
        Multimap<Range, InetAddress> map = HashMultimap.create();
        for (Token token : metadata.sortedTokens())
        {
            Range range = metadata.getPrimaryRangeFor(token);
            for (InetAddress ep : calculateNaturalEndpoints(token, metadata))
            {
                map.put(range, ep);
            }
        }
        return map;
    }
    public Multimap<InetAddress, Range> getAddressRanges()
    {
        return getAddressRanges(tokenMetadata);
    }
    public Collection<Range> getPendingAddressRanges(TokenMetadata metadata, Token pendingToken, InetAddress pendingAddress)
    {
        TokenMetadata temp = metadata.cloneOnlyTokenMap();
        temp.updateNormalToken(pendingToken, pendingAddress);
        return getAddressRanges(temp).get(pendingAddress);
    }
    public void invalidateCachedTokenEndpointValues()
    {
        clearEndpointCache();
    }
    public static AbstractReplicationStrategy createReplicationStrategy(String table,
                                                                        Class<? extends AbstractReplicationStrategy> strategyClass,
                                                                        TokenMetadata tokenMetadata,
                                                                        IEndpointSnitch snitch,
                                                                        Map<String, String> strategyOptions)
            throws ConfigurationException
    {
        AbstractReplicationStrategy strategy;
        Class [] parameterTypes = new Class[] {String.class, TokenMetadata.class, IEndpointSnitch.class, Map.class};
        try
        {
            Constructor<? extends AbstractReplicationStrategy> constructor = strategyClass.getConstructor(parameterTypes);
            strategy = constructor.newInstance(table, tokenMetadata, snitch, strategyOptions);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return strategy;
    }
    public static AbstractReplicationStrategy createReplicationStrategy(String table,
                                                                        String strategyClassName,
                                                                        TokenMetadata tokenMetadata,
                                                                        IEndpointSnitch snitch,
                                                                        Map<String, String> strategyOptions)
            throws ConfigurationException
    {
        Class<AbstractReplicationStrategy> c = getClass(strategyClassName);
        return createReplicationStrategy(table, c, tokenMetadata, snitch, strategyOptions);
    }
    public static Class<AbstractReplicationStrategy> getClass(String cls) throws ConfigurationException
    {
        String className = cls.contains(".") ? cls : "org.apache.cassandra.locator." + cls;
        return FBUtilities.classForName(className, "replication strategy");
    }
}
