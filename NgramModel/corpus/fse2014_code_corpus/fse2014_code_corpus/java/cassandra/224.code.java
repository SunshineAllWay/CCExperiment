package org.apache.cassandra.dht;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.*;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.locks.Condition;
 import com.google.common.base.Charsets;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.apache.cassandra.config.ConfigurationException;
 import org.apache.cassandra.config.DatabaseDescriptor;
 import org.apache.cassandra.db.Table;
 import org.apache.cassandra.gms.FailureDetector;
 import org.apache.cassandra.gms.IFailureDetector;
 import org.apache.cassandra.locator.AbstractReplicationStrategy;
 import org.apache.cassandra.locator.TokenMetadata;
 import org.apache.cassandra.net.IAsyncCallback;
 import org.apache.cassandra.net.IVerbHandler;
 import org.apache.cassandra.net.Message;
 import org.apache.cassandra.net.MessagingService;
 import org.apache.cassandra.service.StorageService;
 import org.apache.cassandra.streaming.OperationType;
 import org.apache.cassandra.streaming.StreamIn;
 import org.apache.cassandra.utils.FBUtilities;
 import org.apache.cassandra.utils.SimpleCondition;
public class BootStrapper
{
    private static final Logger logger = LoggerFactory.getLogger(BootStrapper.class);
    protected final InetAddress address;
    protected final Token token;
    protected final TokenMetadata tokenMetadata;
    public BootStrapper(InetAddress address, Token token, TokenMetadata tmd)
    {
        assert address != null;
        assert token != null;
        this.address = address;
        this.token = token;
        tokenMetadata = tmd;
    }
    public void bootstrap() throws IOException
    {
        if (logger.isDebugEnabled())
            logger.debug("Beginning bootstrap process");
        final Multimap<String, Map.Entry<InetAddress, Collection<Range>>> rangesToFetch = HashMultimap.create();
        int requests = 0;
        for (String table : DatabaseDescriptor.getNonSystemTables())
        {
            Map<InetAddress, Collection<Range>> workMap = getWorkMap(getRangesWithSources(table)).asMap();
            for (Map.Entry<InetAddress, Collection<Range>> entry : workMap.entrySet())
            {
                requests++;
                rangesToFetch.put(table, entry);
            }
        }
        final CountDownLatch latch = new CountDownLatch(requests);
        for (final String table : rangesToFetch.keySet())
        {
            for (Map.Entry<InetAddress, Collection<Range>> entry : rangesToFetch.get(table))
            {
                final InetAddress source = entry.getKey();
                Collection<Range> ranges = entry.getValue();
                final Runnable callback = new Runnable()
                {
                    public void run()
                    {
                        latch.countDown();
                        if (logger.isDebugEnabled())
                            logger.debug(String.format("Removed %s/%s as a bootstrap source; remaining is %s",
                                                       source, table, latch.getCount()));
                    }
                };
                if (logger.isDebugEnabled())
                    logger.debug("Bootstrapping from " + source + " ranges " + StringUtils.join(entry.getValue(), ", "));
                StreamIn.requestRanges(source, table, entry.getValue(), callback, OperationType.BOOTSTRAP);
            }
        }
        try
        {
            latch.await();
            StorageService.instance.finishBootstrapping();
        }
        catch (InterruptedException e)
        {
            throw new AssertionError(e);
        }
    }
    public static Token getBootstrapToken(final TokenMetadata metadata, final Map<InetAddress, Double> load) throws IOException, ConfigurationException
    {
        if (DatabaseDescriptor.getInitialToken() != null)
        {
            logger.debug("token manually specified as " + DatabaseDescriptor.getInitialToken());
            Token token = StorageService.getPartitioner().getTokenFactory().fromString(DatabaseDescriptor.getInitialToken());
            if (metadata.getEndpoint(token) != null)
                throw new ConfigurationException("Bootstraping to existing token " + token + " is not allowed (decommission/removetoken the old node first).");
            return token;
        }
        return getBalancedToken(metadata, load);
    }
    public static Token getBalancedToken(TokenMetadata metadata, Map<InetAddress, Double> load)
    {
        InetAddress maxEndpoint = getBootstrapSource(metadata, load);
        Token<?> t = getBootstrapTokenFrom(maxEndpoint);
        logger.info("New token will be " + t + " to assume load from " + maxEndpoint);
        return t;
    }
    static InetAddress getBootstrapSource(final TokenMetadata metadata, final Map<InetAddress, Double> load)
    {
        List<InetAddress> endpoints = new ArrayList<InetAddress>(load.size());
        for (InetAddress endpoint : load.keySet())
        {
            if (!metadata.isMember(endpoint))
                continue;
            endpoints.add(endpoint);
        }
        if (endpoints.isEmpty())
            throw new RuntimeException("No other nodes seen!  Unable to bootstrap");
        Collections.sort(endpoints, new Comparator<InetAddress>()
        {
            public int compare(InetAddress ia1, InetAddress ia2)
            {
                int n1 = metadata.pendingRangeChanges(ia1);
                int n2 = metadata.pendingRangeChanges(ia2);
                if (n1 != n2)
                    return -(n1 - n2); 
                double load1 = load.get(ia1);
                double load2 = load.get(ia2);
                if (load1 == load2)
                    return 0;
                return load1 < load2 ? -1 : 1;
            }
        });
        InetAddress maxEndpoint = endpoints.get(endpoints.size() - 1);
        assert !maxEndpoint.equals(FBUtilities.getLocalAddress());
        if (metadata.pendingRangeChanges(maxEndpoint) > 0)
            throw new RuntimeException("Every node is a bootstrap source! Please specify an initial token manually or wait for an existing bootstrap operation to finish.");
        return maxEndpoint;
    }
    Multimap<Range, InetAddress> getRangesWithSources(String table)
    {
        assert tokenMetadata.sortedTokens().size() > 0;
        final AbstractReplicationStrategy strat = Table.open(table).getReplicationStrategy();
        Collection<Range> myRanges = strat.getPendingAddressRanges(tokenMetadata, token, address);
        Multimap<Range, InetAddress> myRangeAddresses = ArrayListMultimap.create();
        Multimap<Range, InetAddress> rangeAddresses = strat.getRangeAddresses(tokenMetadata);
        for (Range myRange : myRanges)
        {
            for (Range range : rangeAddresses.keySet())
            {
                if (range.contains(myRange))
                {
                    List<InetAddress> preferred = DatabaseDescriptor.getEndpointSnitch().getSortedListByProximity(address, rangeAddresses.get(range));
                    myRangeAddresses.putAll(myRange, preferred);
                    break;
                }
            }
            assert myRangeAddresses.keySet().contains(myRange);
        }
        return myRangeAddresses;
    }
    static Token<?> getBootstrapTokenFrom(InetAddress maxEndpoint)
    {
        Message message = new Message(FBUtilities.getLocalAddress(), StorageService.Verb.BOOTSTRAP_TOKEN, ArrayUtils.EMPTY_BYTE_ARRAY);
        BootstrapTokenCallback btc = new BootstrapTokenCallback();
        MessagingService.instance().sendRR(message, maxEndpoint, btc);
        return btc.getToken();
    }
    static Multimap<InetAddress, Range> getWorkMap(Multimap<Range, InetAddress> rangesWithSourceTarget)
    {
        return getWorkMap(rangesWithSourceTarget, FailureDetector.instance);
    }
    static Multimap<InetAddress, Range> getWorkMap(Multimap<Range, InetAddress> rangesWithSourceTarget, IFailureDetector failureDetector)
    {
        Multimap<InetAddress, Range> sources = ArrayListMultimap.create();
        for (Range range : rangesWithSourceTarget.keySet())
        {
            for (InetAddress source : rangesWithSourceTarget.get(range))
            {
                if (failureDetector.isAlive(source))
                {
                    sources.put(source, range);
                    break;
                }
            }
        }
        return sources;
    }
    public static class BootstrapTokenVerbHandler implements IVerbHandler
    {
        public void doVerb(Message message)
        {
            StorageService ss = StorageService.instance;
            String tokenString = StorageService.getPartitioner().getTokenFactory().toString(ss.getBootstrapToken());
            Message response = message.getInternalReply(tokenString.getBytes(Charsets.UTF_8));
            MessagingService.instance().sendOneWay(response, message.getFrom());
        }
    }
    private static class BootstrapTokenCallback implements IAsyncCallback
    {
        private volatile Token<?> token;
        private final Condition condition = new SimpleCondition();
        public Token<?> getToken()
        {
            try
            {
                condition.await();
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            return token;
        }
        public void response(Message msg)
        {
            token = StorageService.getPartitioner().getTokenFactory().fromString(new String(msg.getMessageBody(), Charsets.UTF_8));
            condition.signalAll();
        }
    }
}
