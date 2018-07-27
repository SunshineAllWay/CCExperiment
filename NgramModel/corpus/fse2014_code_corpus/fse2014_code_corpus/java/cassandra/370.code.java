package org.apache.cassandra.service;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.concurrent.Stage;
import org.apache.cassandra.concurrent.StageManager;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.dht.Bounds;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.gms.Gossiper;
import org.apache.cassandra.locator.AbstractReplicationStrategy;
import org.apache.cassandra.locator.TokenMetadata;
import org.apache.cassandra.net.IAsyncCallback;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.utils.*;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.IndexClause;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.UnavailableException;
import static com.google.common.base.Charsets.UTF_8;
public class StorageProxy implements StorageProxyMBean
{
    private static final Logger logger = LoggerFactory.getLogger(StorageProxy.class);
    private static ScheduledExecutorService repairExecutor = new ScheduledThreadPoolExecutor(1); 
    private static final ThreadLocal<Random> random = new ThreadLocal<Random>()
    {
        @Override
        protected Random initialValue()
        {
            return new Random();
        }
    };
    private static final LatencyTracker readStats = new LatencyTracker();
    private static final LatencyTracker rangeStats = new LatencyTracker();
    private static final LatencyTracker writeStats = new LatencyTracker();
    private static final LatencyTracker counterWriteStats = new LatencyTracker();
    private static boolean hintedHandoffEnabled = DatabaseDescriptor.hintedHandoffEnabled();
    private static int maxHintWindow = DatabaseDescriptor.getMaxHintWindow();
    private static final String UNREACHABLE = "UNREACHABLE";
    private static final WritePerformer standardWritePerformer;
    private static final WritePerformer counterWritePerformer;
    private StorageProxy() {}
    static
    {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try
        {
            mbs.registerMBean(new StorageProxy(), new ObjectName("org.apache.cassandra.db:type=StorageProxy"));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        standardWritePerformer = new WritePerformer()
        {
            public void apply(IMutation mutation, Multimap<InetAddress, InetAddress> hintedEndpoints, IWriteResponseHandler responseHandler, String localDataCenter) throws IOException
            {
                assert mutation instanceof RowMutation;
                sendToHintedEndpoints((RowMutation) mutation, hintedEndpoints, responseHandler, localDataCenter, true);
            }
        };
        counterWritePerformer = new WritePerformer()
        {
            public void apply(IMutation mutation, Multimap<InetAddress, InetAddress> hintedEndpoints, IWriteResponseHandler responseHandler, String localDataCenter) throws IOException
            {
                applyCounterMutation(mutation, hintedEndpoints, responseHandler, localDataCenter);
            }
        };
    }
    public static void mutate(List<RowMutation> mutations, ConsistencyLevel consistency_level) throws UnavailableException, TimeoutException
    {
        write(mutations, consistency_level, standardWritePerformer, true);
    }
    public static void write(List<? extends IMutation> mutations, ConsistencyLevel consistency_level, WritePerformer performer, boolean updateStats) throws UnavailableException, TimeoutException
    {
        final String localDataCenter = DatabaseDescriptor.getEndpointSnitch().getDatacenter(FBUtilities.getLocalAddress());
        long startTime = System.nanoTime();
        List<IWriteResponseHandler> responseHandlers = new ArrayList<IWriteResponseHandler>();
        IMutation mostRecentMutation = null;
        try
        {
            for (IMutation mutation : mutations)
            {
                mostRecentMutation = mutation;
                String table = mutation.getTable();
                AbstractReplicationStrategy rs = Table.open(table).getReplicationStrategy();
                Collection<InetAddress> writeEndpoints = getWriteEndpoints(table, mutation.key());
                Multimap<InetAddress, InetAddress> hintedEndpoints = rs.getHintedEndpoints(writeEndpoints);
                final IWriteResponseHandler responseHandler = rs.getWriteResponseHandler(writeEndpoints, hintedEndpoints, consistency_level);
                responseHandler.assureSufficientLiveNodes();
                responseHandlers.add(responseHandler);
                performer.apply(mutation, hintedEndpoints, responseHandler, localDataCenter);
            }
            for (IWriteResponseHandler responseHandler : responseHandlers)
            {
                responseHandler.get();
            }
        }
        catch (IOException e)
        {
            assert mostRecentMutation != null;
            throw new RuntimeException("error writing key " + ByteBufferUtil.bytesToHex(mostRecentMutation.key()), e);
        }
        finally
        {
            if (updateStats)
                writeStats.addNano(System.nanoTime() - startTime);
        }
    }
    private static Collection<InetAddress> getWriteEndpoints(String table, ByteBuffer key)
    {
        StorageService ss = StorageService.instance;
        List<InetAddress> naturalEndpoints = ss.getNaturalEndpoints(table, key);
        return ss.getTokenMetadata().getWriteEndpoints(StorageService.getPartitioner().getToken(key), table, naturalEndpoints);
    }
    private static void sendToHintedEndpoints(RowMutation rm, Multimap<InetAddress, InetAddress> hintedEndpoints, IWriteResponseHandler responseHandler, String localDataCenter, boolean insertLocalMessages)
    throws IOException
    {
        Map<String, Multimap<Message, InetAddress>> dcMessages = new HashMap<String, Multimap<Message, InetAddress>>(hintedEndpoints.size());
        Message unhintedMessage = null;
        for (Map.Entry<InetAddress, Collection<InetAddress>> entry : hintedEndpoints.asMap().entrySet())
        {
            InetAddress destination = entry.getKey();
            Collection<InetAddress> targets = entry.getValue();
            String dc = DatabaseDescriptor.getEndpointSnitch().getDatacenter(destination);
            if (targets.size() == 1 && targets.iterator().next().equals(destination))
            {
                if (destination.equals(FBUtilities.getLocalAddress()))
                {
                    if (insertLocalMessages)
                        insertLocalMessage(rm, responseHandler);
                }
                else
                {
                    if (unhintedMessage == null)
                    {
                        unhintedMessage = rm.makeRowMutationMessage();
                        MessagingService.instance().addCallback(responseHandler, unhintedMessage.getMessageId());
                    }
                    if (logger.isDebugEnabled())
                        logger.debug("insert writing key " + ByteBufferUtil.bytesToHex(rm.key()) + " to " + unhintedMessage.getMessageId() + "@" + destination);
                    Multimap<Message, InetAddress> messages = dcMessages.get(dc);
                    if (messages == null)
                    {
                        messages = HashMultimap.create();
                        dcMessages.put(dc, messages);
                    }
                    messages.put(unhintedMessage, destination);
                }
            }
            else
            {
                Message hintedMessage = rm.makeRowMutationMessage();
                for (InetAddress target : targets)
                {
                    if (!target.equals(destination))
                    {
                        addHintHeader(hintedMessage, target);
                        if (logger.isDebugEnabled())
                            logger.debug("insert writing key " + ByteBufferUtil.bytesToHex(rm.key()) + " to " + hintedMessage.getMessageId() + "@" + destination + " for " + target);
                    }
                }
                responseHandler.addHintCallback(hintedMessage, destination);
                Multimap<Message, InetAddress> messages = dcMessages.get(dc);
                if (messages == null)
                {
                    messages = HashMultimap.create();
                    dcMessages.put(dc, messages);
                }
                messages.put(hintedMessage, destination);
            }
        }
        sendMessages(localDataCenter, dcMessages);
    }
    private static void sendMessages(String localDataCenter, Map<String, Multimap<Message, InetAddress>> dcMessages)
    throws IOException
    {
        for (Map.Entry<String, Multimap<Message, InetAddress>> entry: dcMessages.entrySet())
        {
            String dataCenter = entry.getKey();
            Map<Message, Collection<InetAddress>> messagesForDataCenter = entry.getValue().asMap();
            for (Map.Entry<Message, Collection<InetAddress>> messages: messagesForDataCenter.entrySet())
            {
                Message message = messages.getKey();
                Iterator<InetAddress> iter = messages.getValue().iterator();
                assert iter.hasNext();
                InetAddress target = iter.next();
                while (iter.hasNext())
                {
                    InetAddress destination = iter.next();
                    if (dataCenter.equals(localDataCenter))
                    {
                        assert message.getHeader(RowMutation.FORWARD_HEADER) == null;
                        MessagingService.instance().sendOneWay(message, destination);
                    }
                    else
                    {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(bos);
                        byte[] previousHints = message.getHeader(RowMutation.FORWARD_HEADER);
                        if (previousHints != null)
                            dos.write(previousHints);
                        dos.write(destination.getAddress());
                        message.setHeader(RowMutation.FORWARD_HEADER, bos.toByteArray());
                    }
                }
                MessagingService.instance().sendOneWay(message, target);
            }
        }
    }
    private static void addHintHeader(Message message, InetAddress target) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        byte[] previousHints = message.getHeader(RowMutation.HINT);
        if (previousHints != null)
        {
            dos.write(previousHints);
        }
        ByteBufferUtil.writeWithShortLength(ByteBuffer.wrap(target.getHostAddress().getBytes(UTF_8)), dos);
        message.setHeader(RowMutation.HINT, bos.toByteArray());
    }
    private static void insertLocalMessage(final RowMutation rm, final IWriteResponseHandler responseHandler)
    {
        if (logger.isDebugEnabled())
            logger.debug("insert writing local " + rm.toString(true));
        Runnable runnable = new WrappedRunnable()
        {
            public void runMayThrow() throws IOException
            {
                rm.deepCopy().apply();
                responseHandler.response(null);
            }
        };
        StageManager.getStage(Stage.MUTATION).execute(runnable);
    }
    public static void mutateCounters(List<CounterMutation> mutations) throws UnavailableException, TimeoutException
    {
        long startTime = System.nanoTime();
        ArrayList<IWriteResponseHandler> responseHandlers = new ArrayList<IWriteResponseHandler>();
        CounterMutation mostRecentMutation = null;
        StorageService ss = StorageService.instance;
        try
        {
            for (CounterMutation cm : mutations)
            {
                mostRecentMutation = cm;
                InetAddress endpoint = ss.findSuitableEndpoint(cm.getTable(), cm.key());
                if (endpoint.equals(FBUtilities.getLocalAddress()))
                {
                    applyCounterMutationOnLeader(cm);
                }
                else
                {
                    String table = cm.getTable();
                    AbstractReplicationStrategy rs = Table.open(table).getReplicationStrategy();
                    Collection<InetAddress> writeEndpoints = getWriteEndpoints(table, cm.key());
                    Multimap<InetAddress, InetAddress> hintedEndpoints = rs.getHintedEndpoints(writeEndpoints);
                    rs.getWriteResponseHandler(writeEndpoints, hintedEndpoints, cm.consistency()).assureSufficientLiveNodes();
                    IWriteResponseHandler responseHandler = WriteResponseHandler.create(endpoint);
                    responseHandlers.add(responseHandler);
                    Message msg = cm.makeMutationMessage();
                    MessagingService.instance().addCallback(responseHandler, msg.getMessageId());
                    if (logger.isDebugEnabled())
                        logger.debug("forwarding counter update of key " + ByteBufferUtil.bytesToHex(cm.key()) + " to " + msg.getMessageId() + "@" + endpoint);
                    MessagingService.instance().sendOneWay(msg, endpoint);
                }
            }
            for (IWriteResponseHandler responseHandler : responseHandlers)
            {
                responseHandler.get();
            }
        }
        catch (IOException e)
        {
            if (mostRecentMutation == null)
                throw new RuntimeException("no mutations were seen but found an error during write anyway", e);
            else
                throw new RuntimeException("error writing key " + ByteBufferUtil.bytesToHex(mostRecentMutation.key()), e);
        }
        finally
        {
            counterWriteStats.addNano(System.nanoTime() - startTime);
        }
    }
    public static void applyCounterMutationOnLeader(CounterMutation cm) throws UnavailableException, TimeoutException, IOException
    {
        write(Collections.singletonList(cm), cm.consistency(), counterWritePerformer, false);
    }
    private static void applyCounterMutation(final IMutation mutation, final Multimap<InetAddress, InetAddress> hintedEndpoints, final IWriteResponseHandler responseHandler, final String localDataCenter)
    {
        if (logger.isDebugEnabled())
            logger.debug("insert writing local & replicate " + mutation.toString(true));
        Runnable runnable = new WrappedRunnable()
        {
            public void runMayThrow() throws IOException
            {
                assert mutation instanceof CounterMutation;
                final CounterMutation cm = (CounterMutation) mutation;
                cm.apply();
                responseHandler.response(null);
                if (cm.shouldReplicateOnWrite())
                {
                    StageManager.getStage(Stage.REPLICATE_ON_WRITE).execute(new WrappedRunnable()
                            {
                                public void runMayThrow() throws IOException
                    {
                        sendToHintedEndpoints(cm.makeReplicationMutation(), hintedEndpoints, responseHandler, localDataCenter, false);
                    }
                    });
                }
            }
        };
        StageManager.getStage(Stage.MUTATION).execute(runnable);
    }
    public static List<Row> read(List<ReadCommand> commands, ConsistencyLevel consistency_level)
            throws IOException, UnavailableException, TimeoutException, InvalidRequestException
    {
        if (StorageService.instance.isBootstrapMode())
            throw new UnavailableException();
        long startTime = System.nanoTime();
        List<Row> rows;
        try
        {
            rows = fetchRows(commands, consistency_level);
        }
        finally
        {
            readStats.addNano(System.nanoTime() - startTime);
        }
        return rows;
    }
    private static List<Row> fetchRows(List<ReadCommand> commands, ConsistencyLevel consistency_level) throws IOException, UnavailableException, TimeoutException
    {
        List<ReadCallback<Row>> readCallbacks = new ArrayList<ReadCallback<Row>>();
        List<List<InetAddress>> commandEndpoints = new ArrayList<List<InetAddress>>();
        List<Row> rows = new ArrayList<Row>();
        Set<ReadCommand> repairs = new HashSet<ReadCommand>();
        for (ReadCommand command: commands)
        {
            assert !command.isDigestQuery();
            ReadCommand readMessageDigestOnly = command.copy();
            readMessageDigestOnly.setDigestQuery(true);
            Message message = command.makeReadMessage();
            Message messageDigestOnly = readMessageDigestOnly.makeReadMessage();
            InetAddress dataPoint = StorageService.instance.findSuitableEndpoint(command.table, command.key);
            List<InetAddress> endpoints = StorageService.instance.getLiveNaturalEndpoints(command.table, command.key);
            ReadResponseResolver resolver = new ReadResponseResolver(command.table, command.key);
            ReadCallback<Row> handler = getReadCallback(resolver, command.table, consistency_level);
            handler.assureSufficientLiveNodes(endpoints);
            if (randomlyReadRepair(command))
            {
                if (endpoints.size() > handler.blockfor)
                    repairs.add(command);
            }
            else
            {
                endpoints = endpoints.subList(0, handler.blockfor);
            }
            Message[] messages = new Message[endpoints.size()];
            for (int i = 0; i < messages.length; i++)
            {
                InetAddress endpoint = endpoints.get(i);
                Message m = endpoint.equals(dataPoint) ? message : messageDigestOnly;
                messages[i] = m;
                if (logger.isDebugEnabled())
                    logger.debug("reading " + (m == message ? "data" : "digest") + " for " + command + " from " + m.getMessageId() + "@" + endpoint);
            }
            MessagingService.instance().sendRR(messages, endpoints, handler);
            readCallbacks.add(handler);
            commandEndpoints.add(endpoints);
        }
        List<RepairCallback<Row>> repairResponseHandlers = null;
        for (int i = 0; i < commands.size(); i++)
        {
            ReadCallback<Row> readCallback = readCallbacks.get(i);
            Row row;
            ReadCommand command = commands.get(i);
            List<InetAddress> endpoints = commandEndpoints.get(i);
            try
            {
                long startTime2 = System.currentTimeMillis();
                row = readCallback.get(); 
                if (row != null)
                    rows.add(row);
                if (logger.isDebugEnabled())
                    logger.debug("Read: " + (System.currentTimeMillis() - startTime2) + " ms.");
                if (repairs.contains(command))
                    repairExecutor.schedule(new RepairRunner(readCallback.resolver, command, endpoints), DatabaseDescriptor.getRpcTimeout(), TimeUnit.MILLISECONDS);
            }
            catch (DigestMismatchException ex)
            {
                if (logger.isDebugEnabled())
                    logger.debug("Digest mismatch:", ex);
                RepairCallback<Row> handler = repair(command, endpoints);
                if (repairResponseHandlers == null)
                    repairResponseHandlers = new ArrayList<RepairCallback<Row>>();
                repairResponseHandlers.add(handler);
            }
        }
        if (repairResponseHandlers != null)
        {
            for (RepairCallback<Row> handler : repairResponseHandlers)
            {
                try
                {
                    Row row = handler.get();
                    if (row != null)
                        rows.add(row);
                }
                catch (DigestMismatchException e)
                {
                    throw new AssertionError(e); 
                }
            }
        }
        return rows;
    }
    static <T> ReadCallback<T> getReadCallback(IResponseResolver<T> resolver, String table, ConsistencyLevel consistencyLevel)
    {
        if (consistencyLevel.equals(ConsistencyLevel.LOCAL_QUORUM) || consistencyLevel.equals(ConsistencyLevel.EACH_QUORUM))
        {
            return new DatacenterReadCallback(resolver, consistencyLevel, table);
        }
        return new ReadCallback(resolver, consistencyLevel, table);
    }
    private static RepairCallback<Row> repair(ReadCommand command, List<InetAddress> endpoints)
    throws IOException
    {
        ReadResponseResolver resolver = new ReadResponseResolver(command.table, command.key);
        RepairCallback<Row> handler = new RepairCallback<Row>(resolver, endpoints);
        Message messageRepair = command.makeReadMessage();
        MessagingService.instance().sendRR(messageRepair, endpoints, handler);
        return handler;
    }
    public static List<Row> getRangeSlice(RangeSliceCommand command, ConsistencyLevel consistency_level)
    throws IOException, UnavailableException, TimeoutException
    {
        if (logger.isDebugEnabled())
            logger.debug(command.toString());
        long startTime = System.nanoTime();
        List<Row> rows;
        try
        {
            rows = new ArrayList<Row>(command.max_keys);
            List<AbstractBounds> ranges = getRestrictedRanges(command.range);
            for (AbstractBounds range : ranges)
            {
                List<InetAddress> liveEndpoints = StorageService.instance.getLiveNaturalEndpoints(command.keyspace, range.right);
                if (consistency_level == ConsistencyLevel.ONE && liveEndpoints.contains(FBUtilities.getLocalAddress())) 
                {
                    if (logger.isDebugEnabled())
                        logger.debug("local range slice");
                    ColumnFamilyStore cfs = Table.open(command.keyspace).getColumnFamilyStore(command.column_family);
                    try 
                    {
                        rows.addAll(cfs.getRangeSlice(command.super_column,
                                                    range,
                                                    command.max_keys,
                                                    QueryFilter.getFilter(command.predicate, cfs.getComparator())));
                    } 
                    catch (ExecutionException e) 
                    {
                        throw new RuntimeException(e.getCause());
                    } 
                    catch (InterruptedException e) 
                    {
                        throw new AssertionError(e);
                    }           
                }
                else 
                {
                    DatabaseDescriptor.getEndpointSnitch().sortByProximity(FBUtilities.getLocalAddress(), liveEndpoints);
                    RangeSliceCommand c2 = new RangeSliceCommand(command.keyspace, command.column_family, command.super_column, command.predicate, range, command.max_keys);
                    Message message = c2.getMessage();
                    RangeSliceResponseResolver resolver = new RangeSliceResponseResolver(command.keyspace, liveEndpoints);
                    AbstractReplicationStrategy rs = Table.open(command.keyspace).getReplicationStrategy();
                    ReadCallback<List<Row>> handler = getReadCallback(resolver, command.keyspace, consistency_level);
                    for (InetAddress endpoint : liveEndpoints) 
                    {
                        MessagingService.instance().sendRR(message, endpoint, handler);
                        if (logger.isDebugEnabled())
                            logger.debug("reading " + c2 + " from " + message.getMessageId() + "@" + endpoint);
                    }
                    try 
                    {
                        if (logger.isDebugEnabled()) 
                        {
                            for (Row row : handler.get()) 
                            {
                                logger.debug("range slices read " + row.key);
                            }
                        }
                        rows.addAll(handler.get());
                    } 
                    catch (DigestMismatchException e) 
                    {
                        throw new AssertionError(e); 
                    }
                }
                if (rows.size() >= command.max_keys)
                    break;
            }
        }
        finally
        {
            rangeStats.addNano(System.nanoTime() - startTime);
        }
        return rows.size() > command.max_keys ? rows.subList(0, command.max_keys) : rows;
    }
    public static Map<String, List<String>> describeSchemaVersions()
    {
        final String myVersion = DatabaseDescriptor.getDefsVersion().toString();
        final Map<InetAddress, UUID> versions = new ConcurrentHashMap<InetAddress, UUID>();
        final Set<InetAddress> liveHosts = Gossiper.instance.getLiveMembers();
        final Message msg = new Message(FBUtilities.getLocalAddress(), StorageService.Verb.SCHEMA_CHECK, ArrayUtils.EMPTY_BYTE_ARRAY);
        final CountDownLatch latch = new CountDownLatch(liveHosts.size());
        MessagingService.instance().sendRR(msg, liveHosts, new IAsyncCallback()
        {
            public void response(Message msg)
            {
                logger.debug("Received schema check response from " + msg.getFrom().getHostAddress());
                UUID theirVersion = UUID.fromString(new String(msg.getMessageBody()));
                versions.put(msg.getFrom(), theirVersion);
                latch.countDown();
            }
        });
        try
        {
            latch.await(DatabaseDescriptor.getRpcTimeout(), TimeUnit.MILLISECONDS);
        } 
        catch (InterruptedException ex) 
        {
            throw new AssertionError("This latch shouldn't have been interrupted.");
        }
        logger.debug("My version is " + myVersion);
        Map<String, List<String>> results = new HashMap<String, List<String>>();
        Iterable<InetAddress> allHosts = Iterables.concat(Gossiper.instance.getLiveMembers(), Gossiper.instance.getUnreachableMembers());
        for (InetAddress host : allHosts)
        {
            UUID version = versions.get(host);
            String stringVersion = version == null ? UNREACHABLE : version.toString();
            List<String> hosts = results.get(stringVersion);
            if (hosts == null)
            {
                hosts = new ArrayList<String>();
                results.put(stringVersion, hosts);
            }
            hosts.add(host.getHostAddress());
        }
        if (results.get(UNREACHABLE) != null)
            logger.debug("Hosts not in agreement. Didn't get a response from everybody: " + StringUtils.join(results.get(UNREACHABLE), ","));
        for (Map.Entry<String, List<String>> entry : results.entrySet())
        {
            if (entry.getKey().equals(UNREACHABLE) || entry.getKey().equals(myVersion))
                continue;
            for (String host : entry.getValue())
                logger.debug("%s disagrees (%s)", host, entry.getKey());
        }
        if (results.size() == 1)
            logger.debug("Schemas are in agreement.");
        return results;
    }
    static List<AbstractBounds> getRestrictedRanges(final AbstractBounds queryRange)
    {
        if (queryRange instanceof Bounds && queryRange.left.equals(queryRange.right) && !queryRange.left.equals(StorageService.getPartitioner().getMinimumToken()))
        {
            if (logger.isDebugEnabled())
                logger.debug("restricted single token match for query " + queryRange);
            return Collections.singletonList(queryRange);
        }
        TokenMetadata tokenMetadata = StorageService.instance.getTokenMetadata();
        List<AbstractBounds> ranges = new ArrayList<AbstractBounds>();
        Iterator<Token> ringIter = TokenMetadata.ringIterator(tokenMetadata.sortedTokens(), queryRange.left, true);
        AbstractBounds remainder = queryRange;
        while (ringIter.hasNext())
        {
            Token token = ringIter.next();
            if (remainder == null || !(remainder.left.equals(token) || remainder.contains(token)))
                break;
            Pair<AbstractBounds,AbstractBounds> splits = remainder.split(token);
            if (splits.left != null)
                ranges.add(splits.left);
            remainder = splits.right;
        }
        if (remainder != null)
            ranges.add(remainder);
        if (logger.isDebugEnabled())
            logger.debug("restricted ranges for query " + queryRange + " are " + ranges);
        return ranges;
    }
    private static boolean randomlyReadRepair(ReadCommand command)
    {
        CFMetaData cfmd = DatabaseDescriptor.getTableMetaData(command.table).get(command.getColumnFamilyName());
        return cfmd.getReadRepairChance() > random.get().nextDouble();
    }
    public long getReadOperations()
    {
        return readStats.getOpCount();
    }
    public long getTotalReadLatencyMicros()
    {
        return readStats.getTotalLatencyMicros();
    }
    public double getRecentReadLatencyMicros()
    {
        return readStats.getRecentLatencyMicros();
    }
    public long[] getTotalReadLatencyHistogramMicros()
    {
        return readStats.getTotalLatencyHistogramMicros();
    }
    public long[] getRecentReadLatencyHistogramMicros()
    {
        return readStats.getRecentLatencyHistogramMicros();
    }
    public long getRangeOperations()
    {
        return rangeStats.getOpCount();
    }
    public long getTotalRangeLatencyMicros()
    {
        return rangeStats.getTotalLatencyMicros();
    }
    public double getRecentRangeLatencyMicros()
    {
        return rangeStats.getRecentLatencyMicros();
    }
    public long[] getTotalRangeLatencyHistogramMicros()
    {
        return rangeStats.getTotalLatencyHistogramMicros();
    }
    public long[] getRecentRangeLatencyHistogramMicros()
    {
        return rangeStats.getRecentLatencyHistogramMicros();
    }
    public long getWriteOperations()
    {
        return writeStats.getOpCount();
    }
    public long getTotalWriteLatencyMicros()
    {
        return writeStats.getTotalLatencyMicros();
    }
    public double getRecentWriteLatencyMicros()
    {
        return writeStats.getRecentLatencyMicros();
    }
    public long[] getTotalWriteLatencyHistogramMicros()
    {
        return writeStats.getTotalLatencyHistogramMicros();
    }
    public long[] getRecentWriteLatencyHistogramMicros()
    {
        return writeStats.getRecentLatencyHistogramMicros();
    }
    public long getCounterWriteOperations()
    {
        return counterWriteStats.getOpCount();
    }
    public long getTotalCounterWriteLatencyMicros()
    {
        return counterWriteStats.getTotalLatencyMicros();
    }
    public double getRecentCounterWriteLatencyMicros()
    {
        return counterWriteStats.getRecentLatencyMicros();
    }
    public long[] getTotalCounterWriteLatencyHistogramMicros()
    {
        return counterWriteStats.getTotalLatencyHistogramMicros();
    }
    public long[] getRecentCounterWriteLatencyHistogramMicros()
    {
        return counterWriteStats.getRecentLatencyHistogramMicros();
    }
    public static List<Row> scan(String keyspace, String column_family, IndexClause index_clause, SlicePredicate column_predicate, ConsistencyLevel consistency_level)
    throws IOException, TimeoutException, UnavailableException
    {
        IPartitioner p = StorageService.getPartitioner();
        Token leftToken = index_clause.start_key == null ? p.getMinimumToken() : p.getToken(index_clause.start_key);
        List<AbstractBounds> ranges = getRestrictedRanges(new Bounds(leftToken, p.getMinimumToken()));
        logger.debug("scan ranges are " + StringUtils.join(ranges, ","));
        List<Row> rows = new ArrayList<Row>(index_clause.count);
        for (AbstractBounds range : ranges)
        {
            List<InetAddress> liveEndpoints = StorageService.instance.getLiveNaturalEndpoints(keyspace, range.right);
            DatabaseDescriptor.getEndpointSnitch().sortByProximity(FBUtilities.getLocalAddress(), liveEndpoints);
            RangeSliceResponseResolver resolver = new RangeSliceResponseResolver(keyspace, liveEndpoints);
            ReadCallback<List<Row>> handler = getReadCallback(resolver, keyspace, consistency_level);
            if(handler.blockfor > liveEndpoints.size())
                throw new UnavailableException();
            IndexScanCommand command = new IndexScanCommand(keyspace, column_family, index_clause, column_predicate, range);
            Message message = command.getMessage();
            for (InetAddress endpoint : liveEndpoints)
            {
                MessagingService.instance().sendRR(message, endpoint, handler);
                if (logger.isDebugEnabled())
                    logger.debug("reading " + command + " from " + message.getMessageId() + "@" + endpoint);
            }
            List<Row> theseRows;
            try
            {
                theseRows = handler.get();
            }
            catch (DigestMismatchException e)
            {
                throw new RuntimeException(e);
            }
            rows.addAll(theseRows);
            if (logger.isDebugEnabled())
            {
                for (Row row : theseRows)
                    logger.debug("read " + row);
            }
            if (rows.size() >= index_clause.count)
                return rows.subList(0, index_clause.count);
        }
        return rows;
    }
    public boolean getHintedHandoffEnabled()
    {
        return hintedHandoffEnabled;
    }
    public void setHintedHandoffEnabled(boolean b)
    {
        hintedHandoffEnabled = b;
    }
    public static boolean isHintedHandoffEnabled()
    {
        return hintedHandoffEnabled;
    }
    public int getMaxHintWindow()
    {
        return maxHintWindow;
    }
    public void setMaxHintWindow(int ms)
    {
        maxHintWindow = ms;
    }
    public static boolean shouldHint(InetAddress ep)
    {
        return Gossiper.instance.getEndpointDowntime(ep) <= maxHintWindow;
    }
    public static void truncateBlocking(String keyspace, String cfname) throws UnavailableException, TimeoutException, IOException
    {
        logger.debug("Starting a blocking truncate operation on keyspace {}, CF ", keyspace, cfname);
        if (isAnyHostDown())
        {
            logger.info("Cannot perform truncate, some hosts are down");
            throw new UnavailableException();
        }
        Set<InetAddress> allEndpoints = Gossiper.instance.getLiveMembers();
        int blockFor = allEndpoints.size();
        final TruncateResponseHandler responseHandler = new TruncateResponseHandler(blockFor);
        logger.debug("Starting to send truncate messages to hosts {}", allEndpoints);
        Truncation truncation = new Truncation(keyspace, cfname);
        Message message = truncation.makeTruncationMessage();
        MessagingService.instance().sendRR(message, allEndpoints, responseHandler);
        logger.debug("Sent all truncate messages, now waiting for {} responses", blockFor);
        responseHandler.get();
        logger.debug("truncate done");
    }
    private static boolean isAnyHostDown()
    {
        return !Gossiper.instance.getUnreachableMembers().isEmpty();
    }
    private static class RepairRunner extends WrappedRunnable
    {
        private final IResponseResolver<Row> resolver;
        private final ReadCommand command;
        private final List<InetAddress> endpoints;
        public RepairRunner(IResponseResolver<Row> resolver, ReadCommand command, List<InetAddress> endpoints)
        {
            this.resolver = resolver;
            this.command = command;
            this.endpoints = endpoints;
        }
        protected void runMayThrow() throws IOException
        {
            try
            {
                resolver.resolve();
            }
            catch (DigestMismatchException e)
            {
                if (logger.isDebugEnabled())
                    logger.debug("Digest mismatch:", e);
                final RepairCallback<Row> callback = repair(command, endpoints);
                Runnable runnable = new WrappedRunnable()
                {
                    public void runMayThrow() throws DigestMismatchException, IOException, TimeoutException
                    {
                        callback.get();
                    }
                };
                repairExecutor.schedule(runnable, DatabaseDescriptor.getRpcTimeout(), TimeUnit.MILLISECONDS);
            }
        }
    }
    private interface WritePerformer
    {
        public void apply(IMutation mutation, Multimap<InetAddress, InetAddress> hintedEndpoints, IWriteResponseHandler responseHandler, String localDataCenter) throws IOException;
    }
}
