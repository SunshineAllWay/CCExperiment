package org.apache.cassandra.service;
import java.io.IOError;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.cassandra.locator.*;
import org.apache.log4j.Level;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.concurrent.*;
import org.apache.cassandra.config.*;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.migration.AddKeyspace;
import org.apache.cassandra.db.migration.Migration;
import org.apache.cassandra.dht.BootStrapper;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.gms.*;
import org.apache.cassandra.io.DeletionService;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.net.IAsyncResult;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.net.ResponseVerbHandler;
import org.apache.cassandra.service.AntiEntropyService.TreeRequestVerbHandler;
import org.apache.cassandra.streaming.*;
import org.apache.cassandra.thrift.Constants;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.SkipNullRepresenter;
import org.apache.cassandra.utils.WrappedRunnable;
import org.yaml.snakeyaml.Dumper;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
public class StorageService implements IEndpointStateChangeSubscriber, StorageServiceMBean
{
    private static Logger logger_ = LoggerFactory.getLogger(StorageService.class);     
    public static final int RING_DELAY = 30 * 1000; 
    public enum Verb
    {
        MUTATION,
        BINARY,
        READ_REPAIR,
        READ,
        REQUEST_RESPONSE, 
        STREAM_INITIATE, 
        STREAM_INITIATE_DONE, 
        STREAM_REPLY,
        STREAM_REQUEST,
        RANGE_SLICE,
        BOOTSTRAP_TOKEN,
        TREE_REQUEST,
        TREE_RESPONSE,
        JOIN, 
        GOSSIP_DIGEST_SYN,
        GOSSIP_DIGEST_ACK,
        GOSSIP_DIGEST_ACK2,
        DEFINITIONS_ANNOUNCE,
        DEFINITIONS_UPDATE_RESPONSE,
        TRUNCATE,
        SCHEMA_CHECK,
        INDEX_SCAN,
        REPLICATION_FINISHED,
        INTERNAL_RESPONSE, 
        COUNTER_MUTATION,
        UNUSED_1,
        UNUSED_2,
        UNUSED_3,
        ;
    }
    public static final Verb[] VERBS = Verb.values();
    public static final EnumMap<StorageService.Verb, Stage> verbStages = new EnumMap<StorageService.Verb, Stage>(StorageService.Verb.class)
    {{
        put(Verb.MUTATION, Stage.MUTATION);
        put(Verb.BINARY, Stage.MUTATION);
        put(Verb.READ_REPAIR, Stage.MUTATION);
        put(Verb.READ, Stage.READ);
        put(Verb.REQUEST_RESPONSE, Stage.REQUEST_RESPONSE);
        put(Verb.STREAM_REPLY, Stage.MISC); 
        put(Verb.STREAM_REQUEST, Stage.STREAM);
        put(Verb.RANGE_SLICE, Stage.READ);
        put(Verb.BOOTSTRAP_TOKEN, Stage.MISC);
        put(Verb.TREE_REQUEST, Stage.ANTI_ENTROPY);
        put(Verb.TREE_RESPONSE, Stage.ANTI_ENTROPY);
        put(Verb.GOSSIP_DIGEST_ACK, Stage.GOSSIP);
        put(Verb.GOSSIP_DIGEST_ACK2, Stage.GOSSIP);
        put(Verb.GOSSIP_DIGEST_SYN, Stage.GOSSIP);
        put(Verb.DEFINITIONS_ANNOUNCE, Stage.READ);
        put(Verb.DEFINITIONS_UPDATE_RESPONSE, Stage.READ);
        put(Verb.TRUNCATE, Stage.MUTATION);
        put(Verb.SCHEMA_CHECK, Stage.MIGRATION);
        put(Verb.INDEX_SCAN, Stage.READ);
        put(Verb.REPLICATION_FINISHED, Stage.MISC);
        put(Verb.INTERNAL_RESPONSE, Stage.INTERNAL_RESPONSE);
        put(Verb.COUNTER_MUTATION, Stage.MUTATION);
        put(Verb.UNUSED_1, Stage.INTERNAL_RESPONSE);
        put(Verb.UNUSED_2, Stage.INTERNAL_RESPONSE);
        put(Verb.UNUSED_3, Stage.INTERNAL_RESPONSE);
    }};
    public static final RetryingScheduledThreadPoolExecutor scheduledTasks = new RetryingScheduledThreadPoolExecutor("ScheduledTasks");
    private static IPartitioner partitioner_ = DatabaseDescriptor.getPartitioner();
    public static VersionedValue.VersionedValueFactory valueFactory = new VersionedValue.VersionedValueFactory(partitioner_);
    public static final StorageService instance = new StorageService();
    public static IPartitioner getPartitioner() {
        return partitioner_;
    }
    public Collection<Range> getLocalRanges(String table)
    {
        return getRangesForEndpoint(table, FBUtilities.getLocalAddress());
    }
    public Range getLocalPrimaryRange()
    {
        return getPrimaryRangeForEndpoint(FBUtilities.getLocalAddress());
    }
    private TokenMetadata tokenMetadata_ = new TokenMetadata();
    private ExecutorService consistencyManager_ = new JMXEnabledThreadPoolExecutor(DatabaseDescriptor.getConsistencyThreads(),
                                                                                   DatabaseDescriptor.getConsistencyThreads(),
                                                                                   StageManager.KEEPALIVE,
                                                                                   TimeUnit.SECONDS,
                                                                                   new LinkedBlockingQueue<Runnable>(),
                                                                                   new NamedThreadFactory("ReadRepair"),
                                                                                   "request");
    private Set<InetAddress> replicatingNodes;
    private InetAddress removingNode;
    private boolean isBootstrapMode;
    private boolean isClientMode;
    private boolean initialized;
    private String operationMode;
    private MigrationManager migrationManager = new MigrationManager();
    private volatile int totalCFs, remainingCFs;
    public void finishBootstrapping()
    {
        isBootstrapMode = false;
        setToken(getLocalToken());
        logger_.info("Bootstrap/move completed! Now serving reads.");
    }
    public void setToken(Token token)
    {
        if (logger_.isDebugEnabled())
            logger_.debug("Setting token to {}", token);
        SystemTable.updateToken(token);
        tokenMetadata_.updateNormalToken(token, FBUtilities.getLocalAddress());
        Gossiper.instance.addLocalApplicationState(ApplicationState.STATUS, valueFactory.normal(getLocalToken()));
        setMode("Normal", false);
    }
    public StorageService()
    {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try
        {
            mbs.registerMBean(this, new ObjectName("org.apache.cassandra.db:type=StorageService"));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        MessagingService.instance().registerVerbHandlers(Verb.BINARY, new BinaryVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.MUTATION, new RowMutationVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.READ_REPAIR, new ReadRepairVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.READ, new ReadVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.RANGE_SLICE, new RangeSliceVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.INDEX_SCAN, new IndexScanVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.COUNTER_MUTATION, new CounterMutationVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.BOOTSTRAP_TOKEN, new BootStrapper.BootstrapTokenVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.STREAM_REQUEST, new StreamRequestVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.STREAM_REPLY, new StreamReplyVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.REPLICATION_FINISHED, new ReplicationFinishedVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.REQUEST_RESPONSE, new ResponseVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.INTERNAL_RESPONSE, new ResponseVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.TREE_REQUEST, new TreeRequestVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.TREE_RESPONSE, new AntiEntropyService.TreeResponseVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.GOSSIP_DIGEST_SYN, new GossipDigestSynVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.GOSSIP_DIGEST_ACK, new GossipDigestAckVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.GOSSIP_DIGEST_ACK2, new GossipDigestAck2VerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.DEFINITIONS_ANNOUNCE, new DefinitionsAnnounceVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.DEFINITIONS_UPDATE_RESPONSE, new DefinitionsUpdateResponseVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.TRUNCATE, new TruncateVerbHandler());
        MessagingService.instance().registerVerbHandlers(Verb.SCHEMA_CHECK, new SchemaCheckVerbHandler());
        if (StreamingService.instance == null)
            throw new RuntimeException("Streaming service is unavailable.");
    }
    public void stopGossiping()
    {
        if (initialized)
        {
            logger_.warn("Stopping gossip by operator request");
            Gossiper.instance.stop();
            initialized = false;
        }
    }
    public void startGossiping()
    {
        if (!initialized)
        {
            logger_.warn("Starting gossip by operator request");
            Gossiper.instance.start(FBUtilities.getLocalAddress(), (int)(System.currentTimeMillis() / 1000));
            initialized = true;
        }
    }
    public void stopClient()
    {
        Gossiper.instance.unregister(migrationManager);
        Gossiper.instance.unregister(this);
        Gossiper.instance.stop();
        MessagingService.instance().shutdown();
        StageManager.shutdownNow();
    }
    public boolean isInitialized() 
    { 
        return initialized; 
    }
    public synchronized void initClient() throws IOException, ConfigurationException
    {
        if (initialized)
        {
            if (!isClientMode)
                throw new UnsupportedOperationException("StorageService does not support switching modes.");
            return;
        }
        initialized = true;
        isClientMode = true;
        logger_.info("Starting up client gossip");
        setMode("Client", false);
        Gossiper.instance.register(this);
        Gossiper.instance.start(FBUtilities.getLocalAddress(), (int)(System.currentTimeMillis() / 1000)); 
        MessagingService.instance().listen(FBUtilities.getLocalAddress());
        try
        {
            Thread.sleep(5000L);
        }
        catch (Exception ex)
        {
            throw new IOError(ex);
        }
        MigrationManager.announce(DatabaseDescriptor.getDefsVersion(), DatabaseDescriptor.getSeeds());
    }
    public synchronized void initServer() throws IOException, org.apache.cassandra.config.ConfigurationException
    {
        logger_.info("Cassandra version: " + FBUtilities.getReleaseVersionString());
        logger_.info("Thrift API version: " + Constants.VERSION);
        if (initialized)
        {
            if (isClientMode)
                throw new UnsupportedOperationException("StorageService does not support switching modes.");
            return;
        }
        initialized = true;
        isClientMode = false;
        try
        {
            GCInspector.instance.start();
        }
        catch (Throwable t)
        {
            logger_.warn("Unable to start GCInspector (currently only supported on the Sun JVM)");
        }
        if (Boolean.parseBoolean(System.getProperty("cassandra.load_ring_state", "true")))
        {
            logger_.info("Loading persisted ring state");
            for (Map.Entry<Token, InetAddress> entry : SystemTable.loadTokens().entrySet())
            {
                tokenMetadata_.updateNormalToken(entry.getKey(), entry.getValue());
                Gossiper.instance.addSavedEndpoint(entry.getValue());
            }
        }
        logger_.info("Starting up server gossip");
        Gossiper.instance.register(this);
        Gossiper.instance.register(migrationManager);
        Gossiper.instance.start(FBUtilities.getLocalAddress(), SystemTable.incrementAndGetGeneration()); 
        MessagingService.instance().listen(FBUtilities.getLocalAddress());
        StorageLoadBalancer.instance.startBroadcasting();
        MigrationManager.announce(DatabaseDescriptor.getDefsVersion(), DatabaseDescriptor.getSeeds());
        if (DatabaseDescriptor.isAutoBootstrap()
                && DatabaseDescriptor.getSeeds().contains(FBUtilities.getLocalAddress())
                && !SystemTable.isBootstrapped())
            logger_.info("This node will not auto bootstrap because it is configured to be a seed node.");
        Token token;
        boolean bootstrapped = false;
        if (DatabaseDescriptor.isAutoBootstrap()
            && !(DatabaseDescriptor.getSeeds().contains(FBUtilities.getLocalAddress()) || SystemTable.isBootstrapped()))
        {
            setMode("Joining: getting load information", true);
            StorageLoadBalancer.instance.waitForLoadInfo();
            if (logger_.isDebugEnabled())
                logger_.debug("... got load info");
            if (tokenMetadata_.isMember(FBUtilities.getLocalAddress()))
            {
                String s = "This node is already a member of the token ring; bootstrap aborted. (If replacing a dead node, remove the old one from the ring first.)";
                throw new UnsupportedOperationException(s);
            }
            setMode("Joining: getting bootstrap token", true);
            token = BootStrapper.getBootstrapToken(tokenMetadata_, StorageLoadBalancer.instance.getLoadInfo());
            if (DatabaseDescriptor.getNonSystemTables().size() > 0)
            {
                bootstrap(token);
                assert !isBootstrapMode; 
                bootstrapped = true;
                SystemTable.setBootstrapped(true); 
            }
        }
        else
        {
            token = SystemTable.getSavedToken();
            if (token == null)
            {
                String initialToken = DatabaseDescriptor.getInitialToken();
                if (initialToken == null)
                {
                    token = partitioner_.getRandomToken();
                    logger_.warn("Generated random token " + token + ". Random tokens will result in an unbalanced ring; see http://wiki.apache.org/cassandra/Operations");
                }
                else
                {
                    token = partitioner_.getTokenFactory().fromString(initialToken);
                    logger_.info("Saved token not found. Using " + token + " from configuration");
                }
            }
            else
            {
                logger_.info("Using saved token " + token);
            }
        } 
        SystemTable.setBootstrapped(true); 
        setToken(token);
        assert tokenMetadata_.sortedTokens().size() > 0;
    }
    private void setMode(String m, boolean log)
    {
        operationMode = m;
        if (log)
            logger_.info(m);
    }
    private void bootstrap(Token token) throws IOException
    {
        isBootstrapMode = true;
        SystemTable.updateToken(token); 
        Gossiper.instance.addLocalApplicationState(ApplicationState.STATUS, valueFactory.bootstrapping(token));
        setMode("Joining: sleeping " + RING_DELAY + " ms for pending range setup", true);
        try
        {
            Thread.sleep(RING_DELAY);
        }
        catch (InterruptedException e)
        {
            throw new AssertionError(e);
        }
        setMode("Bootstrapping", true);
        new BootStrapper(FBUtilities.getLocalAddress(), token, tokenMetadata_).bootstrap(); 
    }
    public boolean isBootstrapMode()
    {
        return isBootstrapMode;
    }
    public TokenMetadata getTokenMetadata()
    {
        return tokenMetadata_;
    }
    public Map<Range, List<String>> getRangeToEndpointMap(String keyspace)
    {
        if (keyspace == null)
            keyspace = DatabaseDescriptor.getNonSystemTables().get(0);
        Map<Range, List<String>> map = new HashMap<Range, List<String>>();
        for (Map.Entry<Range,List<InetAddress>> entry : getRangeToAddressMap(keyspace).entrySet())
        {
            map.put(entry.getKey(), stringify(entry.getValue()));
        }
        return map;
    }
    public Map<Range, List<String>> getPendingRangeToEndpointMap(String keyspace)
    {
        if (keyspace == null)
            keyspace = DatabaseDescriptor.getNonSystemTables().get(0);
        Map<Range, List<String>> map = new HashMap<Range, List<String>>();
        for (Map.Entry<Range, Collection<InetAddress>> entry : tokenMetadata_.getPendingRanges(keyspace).entrySet())
        {
            List<InetAddress> l = new ArrayList<InetAddress>(entry.getValue());
            map.put(entry.getKey(), stringify(l));
        }
        return map;
    }
    public Map<Range, List<InetAddress>> getRangeToAddressMap(String keyspace)
    {
        List<Range> ranges = getAllRanges(tokenMetadata_.sortedTokens());
        return constructRangeToEndpointMap(keyspace, ranges);
    }
    public Map<Token, String> getTokenToEndpointMap()
    {
        Map<Token, InetAddress> mapInetAddress = tokenMetadata_.getTokenToEndpointMap();
        Map<Token, String> mapString = new HashMap<Token, String>(mapInetAddress.size());
        for (Map.Entry<Token, InetAddress> entry : mapInetAddress.entrySet())
        {
            mapString.put(entry.getKey(), entry.getValue().getHostAddress());
        }
        return mapString;
    }
    private Map<Range, List<InetAddress>> constructRangeToEndpointMap(String keyspace, List<Range> ranges)
    {
        Map<Range, List<InetAddress>> rangeToEndpointMap = new HashMap<Range, List<InetAddress>>();
        for (Range range : ranges)
        {
            rangeToEndpointMap.put(range, Table.open(keyspace).getReplicationStrategy().getNaturalEndpoints(range.right));
        }
        return rangeToEndpointMap;
    }
    public void onChange(InetAddress endpoint, ApplicationState state, VersionedValue value)
    {
        if (state != ApplicationState.STATUS)
            return;
        String apStateValue = value.value;
        String[] pieces = apStateValue.split(VersionedValue.DELIMITER_STR, -1);
        assert (pieces.length > 0);
        String moveName = pieces[0];
        if (moveName.equals(VersionedValue.STATUS_BOOTSTRAPPING))
            handleStateBootstrap(endpoint, pieces);
        else if (moveName.equals(VersionedValue.STATUS_NORMAL))
            handleStateNormal(endpoint, pieces);
        else if (moveName.equals(VersionedValue.STATUS_LEAVING))
            handleStateLeaving(endpoint, pieces);
        else if (moveName.equals(VersionedValue.STATUS_LEFT))
            handleStateLeft(endpoint, pieces);
    }
    private void handleStateBootstrap(InetAddress endpoint, String[] pieces)
    {
        assert pieces.length == 2;
        Token token = getPartitioner().getTokenFactory().fromString(pieces[1]);
        if (logger_.isDebugEnabled())
            logger_.debug("Node " + endpoint + " state bootstrapping, token " + token);
        if (tokenMetadata_.isMember(endpoint))
        {
            if (!tokenMetadata_.isLeaving(endpoint))
                logger_.info("Node " + endpoint + " state jump to bootstrap");
            tokenMetadata_.removeEndpoint(endpoint);
        }
        tokenMetadata_.addBootstrapToken(token, endpoint);
        calculatePendingRanges();
    }
    private void handleStateNormal(InetAddress endpoint, String[] pieces)
    {
        assert pieces.length >= 2;
        Token token = getPartitioner().getTokenFactory().fromString(pieces[1]);
        if (logger_.isDebugEnabled())
            logger_.debug("Node " + endpoint + " state normal, token " + token);
        if (tokenMetadata_.isMember(endpoint))
            logger_.info("Node " + endpoint + " state jump to normal");
        InetAddress currentOwner = tokenMetadata_.getEndpoint(token);
        if (currentOwner == null)
        {
            logger_.debug("New node " + endpoint + " at token " + token);
            tokenMetadata_.updateNormalToken(token, endpoint);
            if (!isClientMode)
                SystemTable.updateToken(endpoint, token);
        }
        else if (endpoint.equals(currentOwner))
        {
            tokenMetadata_.updateNormalToken(token, endpoint);
        }
        else if (Gossiper.instance.compareEndpointStartup(endpoint, currentOwner) > 0)
        {
            logger_.info(String.format("Nodes %s and %s have the same token %s.  %s is the new owner",
                                       endpoint, currentOwner, token, endpoint));
            tokenMetadata_.updateNormalToken(token, endpoint);
            if (!isClientMode)
                SystemTable.updateToken(endpoint, token);
        }
        else
        {
            logger_.info(String.format("Nodes %s and %s have the same token %s.  Ignoring %s",
                                       endpoint, currentOwner, token, endpoint));
        }
        if (pieces.length > 2)
        {
            assert pieces.length == 4;
            handleStateRemoving(endpoint, getPartitioner().getTokenFactory().fromString(pieces[3]), pieces[2]);
        }
        calculatePendingRanges();
    }
    private void handleStateLeaving(InetAddress endpoint, String[] pieces)
    {
        assert pieces.length == 2;
        String moveValue = pieces[1];
        Token token = getPartitioner().getTokenFactory().fromString(moveValue);
        if (logger_.isDebugEnabled())
            logger_.debug("Node " + endpoint + " state leaving, token " + token);
        if (!tokenMetadata_.isMember(endpoint))
        {
            logger_.info("Node " + endpoint + " state jump to leaving");
            tokenMetadata_.updateNormalToken(token, endpoint);
        }
        else if (!tokenMetadata_.getToken(endpoint).equals(token))
        {
            logger_.warn("Node " + endpoint + " 'leaving' token mismatch. Long network partition?");
            tokenMetadata_.updateNormalToken(token, endpoint);
        }
        tokenMetadata_.addLeavingEndpoint(endpoint);
        calculatePendingRanges();
    }
    private void handleStateLeft(InetAddress endpoint, String[] pieces)
    {
        assert pieces.length == 2;
        Token token = getPartitioner().getTokenFactory().fromString(pieces[1]);
        if (logger_.isDebugEnabled())
            logger_.debug("Node " + endpoint + " state left, token " + token);
        excise(token, endpoint);
    }
    private void handleStateRemoving(InetAddress endpoint, Token removeToken, String state)
    {
        InetAddress removeEndpoint = tokenMetadata_.getEndpoint(removeToken);
        if (removeEndpoint == null)
            return;
        if (removeEndpoint.equals(FBUtilities.getLocalAddress()))
        {
            logger_.info("Received removeToken gossip about myself. Is this node a replacement for a removed one?");
            return;
        }
        if (VersionedValue.REMOVED_TOKEN.equals(state))
        {
            excise(removeToken, removeEndpoint);
        }
        else if (VersionedValue.REMOVING_TOKEN.equals(state))
        {
            if (logger_.isDebugEnabled())
                logger_.debug("Token " + removeToken + " removed manually (endpoint was " + removeEndpoint + ")");
            tokenMetadata_.addLeavingEndpoint(removeEndpoint);
            calculatePendingRanges();
            restoreReplicaCount(removeEndpoint, endpoint);
        }
    }
    private void excise(Token token, InetAddress endpoint)
    {
        Gossiper.instance.removeEndpoint(endpoint);
        tokenMetadata_.removeEndpoint(endpoint);
        HintedHandOffManager.deleteHintsForEndPoint(endpoint);
        tokenMetadata_.removeBootstrapToken(token);
        calculatePendingRanges();
        if (!isClientMode)
        {
            logger_.info("Removing token " + token + " for " + endpoint);
            SystemTable.removeToken(token);
        }
    }
    private void calculatePendingRanges()
    {
        for (String table : DatabaseDescriptor.getNonSystemTables())
            calculatePendingRanges(Table.open(table).getReplicationStrategy(), table);
    }
    public static void calculatePendingRanges(AbstractReplicationStrategy strategy, String table)
    {
        TokenMetadata tm = StorageService.instance.getTokenMetadata();
        Multimap<Range, InetAddress> pendingRanges = HashMultimap.create();
        Map<Token, InetAddress> bootstrapTokens = tm.getBootstrapTokens();
        Set<InetAddress> leavingEndpoints = tm.getLeavingEndpoints();
        if (bootstrapTokens.isEmpty() && leavingEndpoints.isEmpty())
        {
            if (logger_.isDebugEnabled())
                logger_.debug("No bootstrapping or leaving nodes -> empty pending ranges for {}", table);
            tm.setPendingRanges(table, pendingRanges);
            return;
        }
        Multimap<InetAddress, Range> addressRanges = strategy.getAddressRanges();
        TokenMetadata allLeftMetadata = tm.cloneAfterAllLeft();
        Set<Range> affectedRanges = new HashSet<Range>();
        for (InetAddress endpoint : leavingEndpoints)
            affectedRanges.addAll(addressRanges.get(endpoint));
        for (Range range : affectedRanges)
        {
            Collection<InetAddress> currentEndpoints = strategy.calculateNaturalEndpoints(range.right, tm);
            Collection<InetAddress> newEndpoints = strategy.calculateNaturalEndpoints(range.right, allLeftMetadata);
            newEndpoints.removeAll(currentEndpoints);
            pendingRanges.putAll(range, newEndpoints);
        }
        for (Map.Entry<Token, InetAddress> entry : bootstrapTokens.entrySet())
        {
            InetAddress endpoint = entry.getValue();
            allLeftMetadata.updateNormalToken(entry.getKey(), endpoint);
            for (Range range : strategy.getAddressRanges(allLeftMetadata).get(endpoint))
                pendingRanges.put(range, endpoint);
            allLeftMetadata.removeEndpoint(endpoint);
        }
        tm.setPendingRanges(table, pendingRanges);
        if (logger_.isDebugEnabled())
            logger_.debug("Pending ranges:\n" + (pendingRanges.isEmpty() ? "<empty>" : tm.printPendingRanges()));
    }
    private Multimap<InetAddress, Range> getNewSourceRanges(String table, Set<Range> ranges) 
    {
        InetAddress myAddress = FBUtilities.getLocalAddress();
        Multimap<Range, InetAddress> rangeAddresses = Table.open(table).getReplicationStrategy().getRangeAddresses(tokenMetadata_);
        Multimap<InetAddress, Range> sourceRanges = HashMultimap.create();
        IFailureDetector failureDetector = FailureDetector.instance;
        for (Range range : ranges)
        {
            Collection<InetAddress> possibleRanges = rangeAddresses.get(range);
            IEndpointSnitch snitch = DatabaseDescriptor.getEndpointSnitch();
            List<InetAddress> sources = snitch.getSortedListByProximity(myAddress, possibleRanges);
            assert (!sources.contains(myAddress));
            for (InetAddress source : sources)
            {
                if (failureDetector.isAlive(source))
                {
                    sourceRanges.put(source, range);
                    break;
                }
            } 
        }
        return sourceRanges;
    }
    private void sendReplicationNotification(InetAddress local, InetAddress remote)
    {
        Message msg = new Message(local, StorageService.Verb.REPLICATION_FINISHED, new byte[0]);
        IFailureDetector failureDetector = FailureDetector.instance;
        while (failureDetector.isAlive(remote))
        {
            IAsyncResult iar = MessagingService.instance().sendRR(msg, remote);
            try 
            {
                iar.get(DatabaseDescriptor.getRpcTimeout(), TimeUnit.MILLISECONDS);
                return; 
            }
            catch(TimeoutException e)
            {
            }
        }
    }
    private void restoreReplicaCount(InetAddress endpoint, final InetAddress notifyEndpoint)
    {
        final Multimap<InetAddress, String> fetchSources = HashMultimap.create();
        Multimap<String, Map.Entry<InetAddress, Collection<Range>>> rangesToFetch = HashMultimap.create();
        final InetAddress myAddress = FBUtilities.getLocalAddress();
        for (String table : DatabaseDescriptor.getNonSystemTables())
        {
            Multimap<Range, InetAddress> changedRanges = getChangedRangesForLeaving(table, endpoint); 
            Set<Range> myNewRanges = new HashSet<Range>();
            for (Map.Entry<Range, InetAddress> entry : changedRanges.entries())
            {
                if (entry.getValue().equals(myAddress))
                    myNewRanges.add(entry.getKey());
            }
            Multimap<InetAddress, Range> sourceRanges = getNewSourceRanges(table, myNewRanges);
            for (Map.Entry<InetAddress, Collection<Range>> entry : sourceRanges.asMap().entrySet())
            {
                fetchSources.put(entry.getKey(), table);
                rangesToFetch.put(table, entry);
            }
        }
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
                        synchronized (fetchSources)
                        {
                            fetchSources.remove(source, table);
                            if (fetchSources.isEmpty())
                                sendReplicationNotification(myAddress, notifyEndpoint);
                        }
                    }
                };
                if (logger_.isDebugEnabled())
                    logger_.debug("Requesting from " + source + " ranges " + StringUtils.join(ranges, ", "));
                StreamIn.requestRanges(source, table, ranges, callback, OperationType.RESTORE_REPLICA_COUNT);
            }
        }
    }
    private Multimap<Range, InetAddress> getChangedRangesForLeaving(String table, InetAddress endpoint)
    {
        Collection<Range> ranges = getRangesForEndpoint(table, endpoint);
        if (logger_.isDebugEnabled())
            logger_.debug("Node " + endpoint + " ranges [" + StringUtils.join(ranges, ", ") + "]");
        Map<Range, List<InetAddress>> currentReplicaEndpoints = new HashMap<Range, List<InetAddress>>();
        for (Range range : ranges)
            currentReplicaEndpoints.put(range, Table.open(table).getReplicationStrategy().calculateNaturalEndpoints(range.right, tokenMetadata_));
        TokenMetadata temp = tokenMetadata_.cloneAfterAllLeft();
        if (temp.isMember(endpoint))
            temp.removeEndpoint(endpoint);
        Multimap<Range, InetAddress> changedRanges = HashMultimap.create();
        for (Range range : ranges)
        {
            Collection<InetAddress> newReplicaEndpoints = Table.open(table).getReplicationStrategy().calculateNaturalEndpoints(range.right, temp);
            newReplicaEndpoints.removeAll(currentReplicaEndpoints.get(range));
            if (logger_.isDebugEnabled())
                if (newReplicaEndpoints.isEmpty())
                    logger_.debug("Range " + range + " already in all replicas");
                else
                    logger_.debug("Range " + range + " will be responsibility of " + StringUtils.join(newReplicaEndpoints, ", "));
            changedRanges.putAll(range, newReplicaEndpoints);
        }
        return changedRanges;
    }
    public void onJoin(InetAddress endpoint, EndpointState epState)
    {
        for (Map.Entry<ApplicationState, VersionedValue> entry : epState.getApplicationStateMap().entrySet())
        {
            onChange(endpoint, entry.getKey(), entry.getValue());
        }
    }
    public void onAlive(InetAddress endpoint, EndpointState state)
    {
        if (!isClientMode)
            deliverHints(endpoint);
    }
    public void onRemove(InetAddress endpoint)
    {
        tokenMetadata_.removeEndpoint(endpoint);
        calculatePendingRanges();
    }
    public void onDead(InetAddress endpoint, EndpointState state)
    {
        MessagingService.instance().convict(endpoint);
    }
    public double getLoad()
    {
        double bytes = 0;
        for (String tableName : DatabaseDescriptor.getTables())
        {
            Table table = Table.open(tableName);
            for (ColumnFamilyStore cfs : table.getColumnFamilyStores())
                bytes += cfs.getLiveDiskSpaceUsed();
        }
        return bytes;
    }
    public String getLoadString()
    {
        return FileUtils.stringifyFileSize(getLoad());
    }
    public Map<String, String> getLoadMap()
    {
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<InetAddress,Double> entry : StorageLoadBalancer.instance.getLoadInfo().entrySet())
        {
            map.put(entry.getKey().getHostAddress(), FileUtils.stringifyFileSize(entry.getValue()));
        }
        map.put(FBUtilities.getLocalAddress().getHostAddress(), getLoadString());
        return map;
    }
    public final void deliverHints(InetAddress endpoint)
    {
        HintedHandOffManager.instance.deliverHints(endpoint);
    }
    public final void deliverHints(String host) throws UnknownHostException
    {
        HintedHandOffManager.instance.deliverHints(host);
    }
    public Token getLocalToken()
    {
        Token token = SystemTable.getSavedToken();
        assert token != null; 
        return token;
    }
    public String getToken()
    {
        return getLocalToken().toString();
    }
    public String getReleaseVersion()
    {
        return FBUtilities.getReleaseVersionString();
    }
    public List<String> getLeavingNodes()
    {
        return stringify(tokenMetadata_.getLeavingEndpoints());
    }
    public List<String> getJoiningNodes()
    {
        return stringify(tokenMetadata_.getBootstrapTokens().values());
    }
    public List<String> getLiveNodes()
    {
        return stringify(Gossiper.instance.getLiveMembers());
    }
    public List<String> getUnreachableNodes()
    {
        return stringify(Gossiper.instance.getUnreachableMembers());
    }
    private List<String> stringify(Iterable<InetAddress> endpoints)
    {
        List<String> stringEndpoints = new ArrayList<String>();
        for (InetAddress ep : endpoints)
        {
            stringEndpoints.add(ep.getHostAddress());
        }
        return stringEndpoints;
    }
    public int getCurrentGenerationNumber()
    {
        return Gossiper.instance.getCurrentGenerationNumber(FBUtilities.getLocalAddress());
    }
    public void forceTableCleanup(String tableName, String... columnFamilies) throws IOException, ExecutionException, InterruptedException
    {
        if (tableName.equals("system"))
            throw new RuntimeException("Cleanup of the system table is neither necessary nor wise");
        for (ColumnFamilyStore cfStore : getValidColumnFamilies(tableName, columnFamilies))
        {
            cfStore.forceCleanup();
        }
    }
    public void forceTableCompaction(String tableName, String... columnFamilies) throws IOException, ExecutionException, InterruptedException
    {
        for (ColumnFamilyStore cfStore : getValidColumnFamilies(tableName, columnFamilies))
        {
            cfStore.forceMajorCompaction();
        }
    }
    public void invalidateKeyCaches(String tableName, String... columnFamilies) throws IOException
    {
        for (ColumnFamilyStore cfStore : getValidColumnFamilies(tableName, columnFamilies))
        {
            cfStore.invalidateKeyCache();
        }
    }
    public void invalidateRowCaches(String tableName, String... columnFamilies) throws IOException
    {
        for (ColumnFamilyStore cfStore : getValidColumnFamilies(tableName, columnFamilies))
        {
            cfStore.invalidateRowCache();
        }
    }
    public void takeSnapshot(String tableName, String tag) throws IOException
    {
        Table tableInstance = getValidTable(tableName);
        tableInstance.snapshot(tag);
    }
    private Table getValidTable(String tableName) throws IOException
    {
        if (!DatabaseDescriptor.getTables().contains(tableName))
        {
            throw new IOException("Table " + tableName + "does not exist");
        }
        return Table.open(tableName);
    }
    public void takeAllSnapshot(String tag) throws IOException
    {
        for (Table table : Table.all())
            table.snapshot(tag);
    }
    public void clearSnapshot() throws IOException
    {
        for (Table table : Table.all())
            table.clearSnapshot();
        if (logger_.isDebugEnabled())
            logger_.debug("Cleared out all snapshot directories");
    }
    public Iterable<ColumnFamilyStore> getValidColumnFamilies(String tableName, String... cfNames) throws IOException
    {
        Table table = getValidTable(tableName);
        if (cfNames.length == 0)
            return table.getColumnFamilyStores();
        Set<ColumnFamilyStore> valid = new HashSet<ColumnFamilyStore>();
        for (String cfName : cfNames)
        {
            ColumnFamilyStore cfStore = table.getColumnFamilyStore(cfName);
            if (cfStore == null)
            {
                logger_.warn(String.format("Invalid column family specified: %s. Proceeding with others.", cfName));
                continue;
            }
            valid.add(cfStore);
        }
        return valid;
    }
    public void forceTableFlush(final String tableName, final String... columnFamilies)
                throws IOException, ExecutionException, InterruptedException
    {
        for (ColumnFamilyStore cfStore : getValidColumnFamilies(tableName, columnFamilies))
        {
            logger_.debug("Forcing binary flush on keyspace " + tableName + ", CF " + cfStore.getColumnFamilyName());
            cfStore.forceFlushBinary();
            logger_.debug("Forcing flush on keyspace " + tableName + ", CF " + cfStore.getColumnFamilyName());
            cfStore.forceBlockingFlush();
        }
    }
    public void forceTableRepair(final String tableName, final String... columnFamilies) throws IOException
    {
        String[] families;
        if (columnFamilies.length == 0)
        {
            ArrayList<String> names = new ArrayList<String>();
            for (ColumnFamilyStore cfStore : getValidColumnFamilies(tableName)) {
                names.add(cfStore.getColumnFamilyName());
            }
            families = names.toArray(new String[] {});
        }
        else
        {
            families = columnFamilies;
        }
        AntiEntropyService.RepairSession sess = AntiEntropyService.instance.getRepairSession(tableName, families);
        try
        {
            sess.start();
            sess.join();
        }
        catch (InterruptedException e)
        {
            throw new IOException("Repair session " + sess + " failed.", e);
        }
    }
    InetAddress getPredecessor(InetAddress ep)
    {
        Token token = tokenMetadata_.getToken(ep);
        return tokenMetadata_.getEndpoint(tokenMetadata_.getPredecessor(token));
    }
    public InetAddress getSuccessor(InetAddress ep)
    {
        Token token = tokenMetadata_.getToken(ep);
        return tokenMetadata_.getEndpoint(tokenMetadata_.getSuccessor(token));
    }
    public Range getPrimaryRangeForEndpoint(InetAddress ep)
    {
        return tokenMetadata_.getPrimaryRangeFor(tokenMetadata_.getToken(ep));
    }
    Collection<Range> getRangesForEndpoint(String table, InetAddress ep)
    {
        return Table.open(table).getReplicationStrategy().getAddressRanges().get(ep);
    }
    public List<Range> getAllRanges(List<Token> sortedTokens)
    {
        if (logger_.isDebugEnabled())
            logger_.debug("computing ranges for " + StringUtils.join(sortedTokens, ", "));
        if (sortedTokens.isEmpty()) 
            return Collections.emptyList();
        List<Range> ranges = new ArrayList<Range>();
        int size = sortedTokens.size();
        for (int i = 1; i < size; ++i)
        {
            Range range = new Range(sortedTokens.get(i - 1), sortedTokens.get(i));
            ranges.add(range);
        }
        Range range = new Range(sortedTokens.get(size - 1), sortedTokens.get(0));
        ranges.add(range);
        return ranges;
    }
    public List<InetAddress> getNaturalEndpoints(String table, ByteBuffer key)
    {
        return getNaturalEndpoints(table, partitioner_.getToken(key));
    }
    public List<InetAddress> getNaturalEndpoints(String table, Token token)
    {
        return Table.open(table).getReplicationStrategy().getNaturalEndpoints(token);
    }
    public List<InetAddress> getLiveNaturalEndpoints(String table, ByteBuffer key)
    {
        return getLiveNaturalEndpoints(table, partitioner_.getToken(key));
    }
    public List<InetAddress> getLiveNaturalEndpoints(String table, Token token)
    {
        List<InetAddress> liveEps = new ArrayList<InetAddress>();
        List<InetAddress> endpoints = Table.open(table).getReplicationStrategy().getNaturalEndpoints(token);
        for (InetAddress endpoint : endpoints)
        {
            if (FailureDetector.instance.isAlive(endpoint))
                liveEps.add(endpoint);
        }
        return liveEps;
    }
    public InetAddress findSuitableEndpoint(String table, ByteBuffer key) throws IOException, UnavailableException
    {
        List<InetAddress> endpoints = getNaturalEndpoints(table, key);
        DatabaseDescriptor.getEndpointSnitch().sortByProximity(FBUtilities.getLocalAddress(), endpoints);
        if (logger_.isDebugEnabled())
            logger_.debug("Sorted endpoints are " + StringUtils.join(endpoints, ","));
        for (InetAddress endpoint : endpoints)
        {
            if (FailureDetector.instance.isAlive(endpoint))
                return endpoint;
        }
        throw new UnavailableException(); 
    }
    public void setLog4jLevel(String classQualifier, String rawLevel)
    {
        Level level = Level.toLevel(rawLevel);
        org.apache.log4j.Logger.getLogger(classQualifier).setLevel(level);
        logger_.info("set log level to " + level + " for classes under '" + classQualifier + "' (if the level doesn't look like '" + rawLevel + "' then log4j couldn't parse '" + rawLevel + "')");
    }
    public List<Token> getSplits(String table, String cfName, Range range, int keysPerSplit)
    {
        List<Token> tokens = new ArrayList<Token>();
        tokens.add(range.left);
        List<DecoratedKey> keys = new ArrayList<DecoratedKey>();
        Table t = Table.open(table);
        ColumnFamilyStore cfs = t.getColumnFamilyStore(cfName);
        for (DecoratedKey sample : cfs.allKeySamples())
        {
            if (range.contains(sample.token))
                keys.add(sample);
        }
        FBUtilities.sortSampledKeys(keys, range);
        int splits = keys.size() * DatabaseDescriptor.getIndexInterval() / keysPerSplit;
        if (keys.size() >= splits)
        {
            for (int i = 1; i < splits; i++)
            {
                int index = i * (keys.size() / splits);
                tokens.add(keys.get(index).token);
            }
        }
        tokens.add(range.right);
        return tokens;
    }
    public Token getBootstrapToken()
    {
        Range range = getLocalPrimaryRange();
        List<DecoratedKey> keys = new ArrayList<DecoratedKey>();
        for (ColumnFamilyStore cfs : ColumnFamilyStore.all())
        {
            for (DecoratedKey key : cfs.allKeySamples())
            {
                if (range.contains(key.token))
                    keys.add(key);
            }
        }
        FBUtilities.sortSampledKeys(keys, range);
        if (keys.size() < 3)
            return partitioner_.midpoint(range.left, range.right);
        else
            return keys.get(keys.size() / 2).token;
    }
    private void startLeaving()
    {
        Gossiper.instance.addLocalApplicationState(ApplicationState.STATUS, valueFactory.leaving(getLocalToken()));
        tokenMetadata_.addLeavingEndpoint(FBUtilities.getLocalAddress());
        calculatePendingRanges();
    }
    public void decommission() throws InterruptedException
    {
        if (!tokenMetadata_.isMember(FBUtilities.getLocalAddress()))
            throw new UnsupportedOperationException("local node is not a member of the token ring yet");
        if (tokenMetadata_.cloneAfterAllLeft().sortedTokens().size() < 2)
            throw new UnsupportedOperationException("no other normal nodes in the ring; decommission would be pointless");
        for (String table : DatabaseDescriptor.getNonSystemTables())
        {
            if (tokenMetadata_.getPendingRanges(table, FBUtilities.getLocalAddress()).size() > 0)
                throw new UnsupportedOperationException("data is currently moving to this node; unable to leave the ring");
        }
        if (logger_.isDebugEnabled())
            logger_.debug("DECOMMISSIONING");
        startLeaving();
        setMode("Leaving: sleeping " + RING_DELAY + " ms for pending range setup", true);
        Thread.sleep(RING_DELAY);
        Runnable finishLeaving = new Runnable()
        {
            public void run()
            {
                Gossiper.instance.stop();
                MessagingService.instance().shutdown();
                StageManager.shutdownNow();
                setMode("Decommissioned", true);
            }
        };
        unbootstrap(finishLeaving);
    }
    private void leaveRing()
    {
        SystemTable.setBootstrapped(false);
        tokenMetadata_.removeEndpoint(FBUtilities.getLocalAddress());
        calculatePendingRanges();
        Gossiper.instance.addLocalApplicationState(ApplicationState.STATUS, valueFactory.left(getLocalToken()));
        try
        {
            Thread.sleep(2 * Gossiper.intervalInMillis_);
        }
        catch (InterruptedException e)
        {
            throw new AssertionError(e);
        }
    }
    private void unbootstrap(final Runnable onFinish)
    {
        final CountDownLatch latch = new CountDownLatch(DatabaseDescriptor.getNonSystemTables().size());
        for (final String table : DatabaseDescriptor.getNonSystemTables())
        {
            Multimap<Range, InetAddress> rangesMM = getChangedRangesForLeaving(table, FBUtilities.getLocalAddress());
            if (logger_.isDebugEnabled())
                logger_.debug("Ranges needing transfer are [" + StringUtils.join(rangesMM.keySet(), ",") + "]");
            if (rangesMM.isEmpty())
            {
                latch.countDown();
                continue;
            }
            setMode("Leaving: streaming data to other nodes", true);
            final Set<Map.Entry<Range, InetAddress>> pending = new HashSet<Map.Entry<Range, InetAddress>>(rangesMM.entries());
            for (final Map.Entry<Range, InetAddress> entry : rangesMM.entries())
            {
                final Range range = entry.getKey();
                final InetAddress newEndpoint = entry.getValue();
                final Runnable callback = new Runnable()
                {
                    public void run()
                    {
                        synchronized(pending)
                        {
                            pending.remove(entry);
                            if (pending.isEmpty())
                                latch.countDown();
                        }
                    }
                };
                StageManager.getStage(Stage.STREAM).execute(new Runnable()
                {
                    public void run()
                    {
                        StreamOut.transferRanges(newEndpoint, table, Arrays.asList(range), callback, OperationType.UNBOOTSTRAP);
                    }
                });
            }
        }
        logger_.debug("waiting for stream aks.");
        try
        {
            latch.await();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        logger_.debug("stream acks all received.");
        leaveRing();
        onFinish.run();
    }
    public void move(String newToken) throws IOException, InterruptedException
    {
        move(partitioner_.getTokenFactory().fromString(newToken));
    }
    public void loadBalance() throws IOException, InterruptedException
    {
        move((Token)null);
    }
    private void move(final Token token) throws IOException, InterruptedException
    {
        for (String table : DatabaseDescriptor.getTables())
        {
            if (tokenMetadata_.getPendingRanges(table, FBUtilities.getLocalAddress()).size() > 0)
                throw new UnsupportedOperationException("data is currently moving to this node; unable to leave the ring");
        }
        if (token != null && tokenMetadata_.sortedTokens().contains(token))
            throw new IOException("target token " + token + " is already owned by another node");
        if (logger_.isDebugEnabled())
            logger_.debug("Leaving: old token was " + getLocalToken());
        startLeaving();
        setMode("Leaving: sleeping " + RING_DELAY + " ms for pending range setup", true);
        Thread.sleep(RING_DELAY);
        Runnable finishMoving = new WrappedRunnable()
        {
            public void runMayThrow() throws IOException
            {
                Token bootstrapToken = token;
                if (bootstrapToken == null)
                {
                    StorageLoadBalancer.instance.waitForLoadInfo();
                    bootstrapToken = BootStrapper.getBalancedToken(tokenMetadata_, StorageLoadBalancer.instance.getLoadInfo());
                }
                logger_.info("re-bootstrapping to new token {}", bootstrapToken);
                bootstrap(bootstrapToken);
            }
        };
        unbootstrap(finishMoving);
    }
    public String getRemovalStatus()
    {
        if (removingNode == null) {
            return "No token removals in process.";
        }
        return String.format("Removing token (%s). Waiting for replication confirmation from [%s].",
                             tokenMetadata_.getToken(removingNode),
                             StringUtils.join(replicatingNodes, ","));
    }
    public void forceRemoveCompletion()
    {
        if (!replicatingNodes.isEmpty())
            logger_.warn("Removal not confirmed for for " + StringUtils.join(this.replicatingNodes, ","));
        replicatingNodes.clear();
    }
    public void removeToken(String tokenString)
    {
        InetAddress myAddress = FBUtilities.getLocalAddress();
        Token localToken = tokenMetadata_.getToken(myAddress);
        Token token = partitioner_.getTokenFactory().fromString(tokenString);
        InetAddress endpoint = tokenMetadata_.getEndpoint(token);
        if (endpoint == null)
            throw new UnsupportedOperationException("Token not found.");
        if (endpoint.equals(myAddress))
             throw new UnsupportedOperationException("Cannot remove node's own token");
        if (Gossiper.instance.getLiveMembers().contains(endpoint))
            throw new UnsupportedOperationException("Node " + endpoint + " is alive and owns this token. Use decommission command to remove it from the ring");
        if (tokenMetadata_.isLeaving(endpoint)) 
            throw new UnsupportedOperationException("Node " + endpoint + " is already being removed.");
        if (replicatingNodes != null)
            throw new UnsupportedOperationException("This node is already processing a removal. Wait for it to complete.");
        replicatingNodes = Collections.synchronizedSet(new HashSet<InetAddress>());
        for (String table : DatabaseDescriptor.getNonSystemTables())
        {
            if (Table.open(table).getReplicationStrategy().getReplicationFactor() == 1)
                continue;
            Multimap<Range, InetAddress> changedRanges = getChangedRangesForLeaving(table, endpoint);
            IFailureDetector failureDetector = FailureDetector.instance;
            for (InetAddress ep : changedRanges.values())
            {
                if (failureDetector.isAlive(ep))
                    replicatingNodes.add(ep);
                else
                    logger_.warn("Endpoint " + ep + " is down and will not receive data for re-replication of " + endpoint);
            }
        }
        removingNode = endpoint;
        tokenMetadata_.addLeavingEndpoint(endpoint);
        calculatePendingRanges();
        Gossiper.instance.addLocalApplicationState(ApplicationState.STATUS, valueFactory.removingNonlocal(localToken, token));
        restoreReplicaCount(endpoint, myAddress);
        while (!replicatingNodes.isEmpty())
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                throw new AssertionError(e);
            }
        }
        excise(token, endpoint);
        Gossiper.instance.addLocalApplicationState(ApplicationState.STATUS, valueFactory.removedNonlocal(localToken, token));
        replicatingNodes = null;
        removingNode = null;
    }
    public void confirmReplication(InetAddress node)
    {
        assert replicatingNodes != null;
        replicatingNodes.remove(node);
    }
    public boolean isClientMode()
    {
        return isClientMode;
    }
    public synchronized void requestGC()
    {
        if (hasUnreclaimedSpace())
        {
            logger_.info("requesting GC to free disk space");
            System.gc();
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                throw new AssertionError(e);
            }
        }
    }
    private boolean hasUnreclaimedSpace()
    {
        for (ColumnFamilyStore cfs : ColumnFamilyStore.all())
        {
            if (cfs.hasUnreclaimedSpace())
                return true;
        }
        return false;
    }
    public String getOperationMode()
    {
        return operationMode;
    }
    public String getDrainProgress()
    {
        return String.format("Drained %s/%s ColumnFamilies", remainingCFs, totalCFs);
    }
    public synchronized void drain() throws IOException, InterruptedException, ExecutionException
    {
        ExecutorService mutationStage = StageManager.getStage(Stage.MUTATION);
        if (mutationStage.isTerminated())
        {
            logger_.warn("Cannot drain node (did it already happen?)");
            return;
        }
        setMode("Starting drain process", true);
        Gossiper.instance.stop();
        setMode("Draining: shutting down MessageService", false);
        MessagingService.instance().shutdown();
        setMode("Draining: emptying MessageService pools", false);
        MessagingService.instance().waitFor();
        setMode("Draining: clearing mutation stage", false);
        mutationStage.shutdown();
        mutationStage.awaitTermination(3600, TimeUnit.SECONDS);
        setMode("Draining: flushing column families", false);
        List<ColumnFamilyStore> cfses = new ArrayList<ColumnFamilyStore>();
        for (String tableName : DatabaseDescriptor.getNonSystemTables())
        {
            Table table = Table.open(tableName);
            cfses.addAll(table.getColumnFamilyStores());
        }
        totalCFs = remainingCFs = cfses.size();
        for (ColumnFamilyStore cfs : cfses)
        {
            cfs.forceBlockingFlush();
            remainingCFs--;
        }
        ColumnFamilyStore.postFlushExecutor.shutdown();
        ColumnFamilyStore.postFlushExecutor.awaitTermination(60, TimeUnit.SECONDS);
        DeletionService.waitFor();
        setMode("Node is drained", true);
    }
    public void loadSchemaFromYAML() throws ConfigurationException, IOException
    { 
        final Collection<KSMetaData> tables = DatabaseDescriptor.readTablesFromYaml();
        for (KSMetaData table : tables)
        {
            if (!table.name.matches(Migration.NAME_VALIDATOR_REGEX))
                throw new ConfigurationException("Invalid table name: " + table.name);
            for (CFMetaData cfm : table.cfMetaData().values())
                if (!Migration.isLegalName(cfm.cfName))
                    throw new ConfigurationException("Invalid column family name: " + cfm.cfName);
        }
        Callable<Migration> call = new Callable<Migration>()
        {
            public Migration call() throws Exception
            {
                if (DatabaseDescriptor.getDefsVersion().timestamp() > 0 || Migration.getLastMigrationId() != null)
                    throw new ConfigurationException("Cannot import schema when one already exists");
                Migration migration = null;
                for (KSMetaData table : tables)
                {
                    migration = new AddKeyspace(table); 
                    migration.apply();
                }
                return migration;
            }
        };
        Migration migration;
        try
        {
            migration = StageManager.getStage(Stage.MIGRATION).submit(call).get();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        catch (ExecutionException e)
        {
            if (e.getCause() instanceof ConfigurationException)
                throw (ConfigurationException)e.getCause();
            else if (e.getCause() instanceof IOException)
                throw (IOException)e.getCause();
            else if (e.getCause() instanceof Exception)
                throw new ConfigurationException(e.getCause().getMessage(), (Exception)e.getCause());
            else
                throw new RuntimeException(e);
        }
        assert DatabaseDescriptor.getDefsVersion().timestamp() > 0;
        DefsTable.dumpToStorage(DatabaseDescriptor.getDefsVersion());
        Collection<Future> flushers = new ArrayList<Future>();
        flushers.addAll(Table.open(Table.SYSTEM_TABLE).flush());
        for (Future f : flushers)
        {
            try
            {
                f.get();
            }
            catch (Exception e)
            {
                ConfigurationException ce = new ConfigurationException(e.getMessage());
                ce.initCause(e);
                throw ce;
            }
        }
        if (migration != null)
            migration.announce();
    }
    public String exportSchema() throws IOException
    {
        List<RawKeyspace> keyspaces = new ArrayList<RawKeyspace>();
        for (String ksname : DatabaseDescriptor.getNonSystemTables())
        {
            KSMetaData ksm = DatabaseDescriptor.getTableDefinition(ksname);
            RawKeyspace rks = new RawKeyspace();
            rks.name = ksm.name;
            rks.replica_placement_strategy = ksm.strategyClass.getName();
            rks.replication_factor = ksm.replicationFactor;
            rks.column_families = new RawColumnFamily[ksm.cfMetaData().size()];
            int i = 0;
            for (CFMetaData cfm : ksm.cfMetaData().values())
            {
                RawColumnFamily rcf = new RawColumnFamily();
                rcf.name = cfm.cfName;
                rcf.compare_with = cfm.comparator.getClass().getName();
                rcf.default_validation_class = cfm.getDefaultValidator().getClass().getName();
                rcf.compare_subcolumns_with = cfm.subcolumnComparator == null ? null : cfm.subcolumnComparator.getClass().getName();
                rcf.column_type = cfm.cfType;
                rcf.comment = cfm.getComment();
                rcf.keys_cached = cfm.getKeyCacheSize();
                rcf.read_repair_chance = cfm.getReadRepairChance();
                rcf.replicate_on_write = cfm.getReplicateOnWrite();
                rcf.gc_grace_seconds = cfm.getGcGraceSeconds();
                rcf.rows_cached = cfm.getRowCacheSize();
                rcf.column_metadata = new RawColumnDefinition[cfm.getColumn_metadata().size()];
                int j = 0;
                for (ColumnDefinition cd : cfm.getColumn_metadata().values())
                {
                    RawColumnDefinition rcd = new RawColumnDefinition();
                    rcd.index_name = cd.getIndexName();
                    rcd.index_type = cd.getIndexType();
                    rcd.name = ByteBufferUtil.string(cd.name, Charsets.UTF_8);
                    rcd.validator_class = cd.validator.getClass().getName();
                    rcf.column_metadata[j++] = rcd;
                }
                if (j == 0)
                    rcf.column_metadata = null;
                rks.column_families[i++] = rcf;
            }
            keyspaces.add(rks);
        }
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        SkipNullRepresenter representer = new SkipNullRepresenter();
        representer.addClassTag(RawColumnFamily.class, Tag.MAP);
        representer.addClassTag(Keyspaces.class, Tag.MAP);
        representer.addClassTag(ColumnDefinition.class, Tag.MAP);
        Dumper dumper = new Dumper(representer, options);
        Yaml yaml = new Yaml(dumper);
        Keyspaces ks = new Keyspaces();
        ks.keyspaces = keyspaces;
        return yaml.dump(ks);
    }
    public class Keyspaces
    {
        public List<RawKeyspace> keyspaces;
    }
    IPartitioner setPartitionerUnsafe(IPartitioner newPartitioner)
    {
        IPartitioner oldPartitioner = partitioner_;
        partitioner_ = newPartitioner;
        valueFactory = new VersionedValue.VersionedValueFactory(partitioner_);
        return oldPartitioner;
    }
    TokenMetadata setTokenMetadataUnsafe(TokenMetadata tmd)
    {
        TokenMetadata old = tokenMetadata_;
        tokenMetadata_ = tmd;
        return old;
    }
    public void truncate(String keyspace, String columnFamily) throws UnavailableException, TimeoutException, IOException
    {
        StorageProxy.truncateBlocking(keyspace, columnFamily);
    }
    public void saveCaches() throws ExecutionException, InterruptedException
    {
        List<Future<?>> futures = new ArrayList<Future<?>>();
        logger_.debug("submitting cache saves");
        for (ColumnFamilyStore cfs : ColumnFamilyStore.all())
        {
            futures.add(cfs.submitKeyCacheWrite());
            futures.add(cfs.submitRowCacheWrite());
        }
        FBUtilities.waitOnFutures(futures);
        logger_.debug("cache saves completed");
    }
    public Map<Token, Float> getOwnership()
    {
        List<Token> sortedTokens = new ArrayList<Token>(getTokenToEndpointMap().keySet());
        Collections.sort(sortedTokens);
        return partitioner_.describeOwnership(sortedTokens);
    }
    public List<String> getKeyspaces()
    {
        List<String> tableslist = new ArrayList<String>(DatabaseDescriptor.getTables());
        return Collections.unmodifiableList(tableslist);
    }
    public void updateSnitch(String epSnitchClassName, Boolean dynamic, Integer dynamicUpdateInterval, Integer dynamicResetInterval, Double dynamicBadnessThreshold) throws ConfigurationException
    {
        IEndpointSnitch oldSnitch = DatabaseDescriptor.getEndpointSnitch();
        IEndpointSnitch newSnitch = FBUtilities.construct(epSnitchClassName, "snitch");
        if (dynamic)
        {
            DatabaseDescriptor.setDynamicUpdateInterval(dynamicUpdateInterval);
            DatabaseDescriptor.setDynamicResetInterval(dynamicResetInterval);
            DatabaseDescriptor.setDynamicBadnessThreshold(dynamicBadnessThreshold);
            newSnitch = new DynamicEndpointSnitch(newSnitch);
        }
        DatabaseDescriptor.setEndpointSnitch(newSnitch);
        for (String ks : DatabaseDescriptor.getTables())
        {
            Table.open(ks).getReplicationStrategy().snitch = newSnitch;
        }
        if (oldSnitch instanceof DynamicEndpointSnitch)
            ((DynamicEndpointSnitch)oldSnitch).unregisterMBean();
    }
}
