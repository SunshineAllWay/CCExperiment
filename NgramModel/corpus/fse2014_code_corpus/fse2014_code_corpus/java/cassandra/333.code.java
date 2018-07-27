package org.apache.cassandra.net;
import java.io.IOError;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.concurrent.DebuggableThreadPoolExecutor;
import org.apache.cassandra.concurrent.StageManager;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.EncryptionOptions;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.apache.cassandra.locator.ILatencyPublisher;
import org.apache.cassandra.locator.ILatencySubscriber;
import org.apache.cassandra.net.io.SerializerType;
import org.apache.cassandra.net.sink.SinkManager;
import org.apache.cassandra.service.GCInspector;
import org.apache.cassandra.security.SSLFactory;
import org.apache.cassandra.security.streaming.SSLFileStreamTask;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.streaming.FileStreamTask;
import org.apache.cassandra.streaming.StreamHeader;
import org.apache.cassandra.utils.ExpiringMap;
import org.apache.cassandra.utils.GuidGenerator;
import org.apache.cassandra.utils.SimpleCondition;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.cliffc.high_scale_lib.NonBlockingHashSet;
public final class MessagingService implements MessagingServiceMBean, ILatencyPublisher
{
    private static final int version_ = 1;
    private SerializerType serializerType_ = SerializerType.BINARY;
    private static final int PROTOCOL_MAGIC = 0xCA552DFA;
    private final ExpiringMap<String, IMessageCallback> callbacks;
    private final ConcurrentMap<String, Collection<InetAddress>> targets = new NonBlockingHashMap<String, Collection<InetAddress>>();
    private final Map<StorageService.Verb, IVerbHandler> verbHandlers_;
    private final ExecutorService streamExecutor_;
    private final NonBlockingHashMap<InetAddress, OutboundTcpConnectionPool> connectionManagers_ = new NonBlockingHashMap<InetAddress, OutboundTcpConnectionPool>();
    private static final Logger logger_ = LoggerFactory.getLogger(MessagingService.class);
    private static final int LOG_DROPPED_INTERVAL_IN_MS = 5000;
    private SocketThread socketThread;
    private final SimpleCondition listenGate;
    private final Map<StorageService.Verb, AtomicInteger> droppedMessages = new EnumMap<StorageService.Verb, AtomicInteger>(StorageService.Verb.class);
    private final List<ILatencySubscriber> subscribers = new ArrayList<ILatencySubscriber>();
    {
        for (StorageService.Verb verb : StorageService.Verb.values())
            droppedMessages.put(verb, new AtomicInteger());
    }
    private static class MSHandle
    {
        public static final MessagingService instance = new MessagingService();
    }
    public static MessagingService instance()
    {
        return MSHandle.instance;
    }
    private MessagingService()
    {
        listenGate = new SimpleCondition();
        verbHandlers_ = new EnumMap<StorageService.Verb, IVerbHandler>(StorageService.Verb.class);
        streamExecutor_ = new DebuggableThreadPoolExecutor("Streaming", DatabaseDescriptor.getCompactionThreadPriority());
        Runnable logDropped = new Runnable()
        {
            public void run()
            {
                logDroppedMessages();
            }
        };
        StorageService.scheduledTasks.scheduleWithFixedDelay(logDropped, LOG_DROPPED_INTERVAL_IN_MS, LOG_DROPPED_INTERVAL_IN_MS, TimeUnit.MILLISECONDS);
        Function<String, ?> timeoutReporter = new Function<String, Object>()
        {
            public Object apply(String messageId)
            {
                Collection<InetAddress> addresses = targets.remove(messageId);
                if (addresses == null)
                    return null;
                for (InetAddress address : addresses)
                {
                    for (ILatencySubscriber subscriber : subscribers)
                        subscriber.receiveTiming(address, (double) DatabaseDescriptor.getRpcTimeout());
                }
                return null;
            }
        };
        callbacks = new ExpiringMap<String, IMessageCallback>((long) (1.1 * DatabaseDescriptor.getRpcTimeout()), timeoutReporter);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try
        {
            mbs.registerMBean(this, new ObjectName("org.apache.cassandra.net:type=MessagingService"));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    public static byte[] hash(String type, byte data[])
    {
        byte result[];
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance(type);
            result = messageDigest.digest(data);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return result;
    }
    public void convict(InetAddress ep)
    {
        logger_.debug("Resetting pool for " + ep);
        getConnectionPool(ep).reset();
    }
    public void listen(InetAddress localEp) throws IOException, ConfigurationException
    {
        socketThread = new SocketThread(getServerSocket(localEp), "ACCEPT-" + localEp);
        socketThread.start();
        listenGate.signalAll();
    }
    private ServerSocket getServerSocket(InetAddress localEp) throws IOException, ConfigurationException
    {
        final ServerSocket ss;
        if (DatabaseDescriptor.getEncryptionOptions().internode_encryption == EncryptionOptions.InternodeEncryption.all)
        {
            ss = SSLFactory.getServerSocket(DatabaseDescriptor.getEncryptionOptions(), localEp, DatabaseDescriptor.getStoragePort());
            logger_.info("Starting Encrypted Messaging Service on port {}", DatabaseDescriptor.getStoragePort());
        }
        else
        {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            ss = serverChannel.socket();
            ss.setReuseAddress(true);
            InetSocketAddress address = new InetSocketAddress(localEp, DatabaseDescriptor.getStoragePort());
            try
            {
                ss.bind(address);
            }
            catch (BindException e)
            {
                if (e.getMessage().contains("in use"))
                    throw new ConfigurationException(address + " is in use by another process.  Change listen_address:storage_port in cassandra.yaml to values that do not conflict with other services");
                else if (e.getMessage().contains("Cannot assign requested address"))
                    throw new ConfigurationException("Unable to bind to address " + address + ". Set listen_address in cassandra.yaml to an interface you can bind to, e.g., your private IP address on EC2");
                else
                    throw e;
            }
            logger_.info("Starting Messaging Service on port {}", DatabaseDescriptor.getStoragePort());
        }
        return ss;
    }
    public void waitUntilListening()
    {
        try
        {
            listenGate.await();
        }
        catch (InterruptedException ie)
        {
            logger_.debug("await interrupted");
        }
    }
    public OutboundTcpConnectionPool getConnectionPool(InetAddress to)
    {
        OutboundTcpConnectionPool cp = connectionManagers_.get(to);
        if (cp == null)
        {
            connectionManagers_.putIfAbsent(to, new OutboundTcpConnectionPool(to));
            cp = connectionManagers_.get(to);
        }
        return cp;
    }
    public OutboundTcpConnection getConnection(InetAddress to, Message msg)
    {
        return getConnectionPool(to).getConnection(msg);
    }
    public void registerVerbHandlers(StorageService.Verb verb, IVerbHandler verbHandler)
    {
    	assert !verbHandlers_.containsKey(verb);
    	verbHandlers_.put(verb, verbHandler);
    }
    public IVerbHandler getVerbHandler(StorageService.Verb type)
    {
        return verbHandlers_.get(type);
    }
    public String sendRR(Message message, Collection<InetAddress> to, IAsyncCallback cb)
    {
        String messageId = message.getMessageId();
        addCallback(cb, messageId);
        for (InetAddress endpoint : to)
        {
            putTarget(messageId, endpoint);
            sendOneWay(message, endpoint);
        }
        return messageId;
    }
    private void putTarget(String messageId, InetAddress endpoint)
    {
        Collection<InetAddress> addresses = targets.get(messageId);
        if (addresses == null)
        {
            addresses = new NonBlockingHashSet<InetAddress>();
            Collection<InetAddress> oldAddresses = targets.putIfAbsent(messageId, addresses);
            if (oldAddresses != null)
                addresses = oldAddresses;
        }
        addresses.add(endpoint);
    }
    private void removeTarget(String messageId, InetAddress from)
    {
        Collection<InetAddress> addresses = targets.get(messageId);
        if (addresses != null)
            addresses.remove(from);
    }
    public void addCallback(IAsyncCallback cb, String messageId)
    {
        callbacks.put(messageId, cb);
    }
    public String sendRR(Message message, InetAddress to, IAsyncCallback cb)
    {        
        String messageId = message.getMessageId();
        addCallback(cb, messageId);
        putTarget(messageId, to);
        sendOneWay(message, to);
        return messageId;
    }
    public String sendRR(Message[] messages, List<InetAddress> to, IAsyncCallback cb)
    {
        if (messages.length != to.size())
            throw new IllegalArgumentException("Number of messages and the number of endpoints need to be same.");
        String groupId = GuidGenerator.guid();
        addCallback(cb, groupId);
        for ( int i = 0; i < messages.length; ++i )
        {
            messages[i].setMessageId(groupId);
            putTarget(groupId, to.get(i));
            sendOneWay(messages[i], to.get(i));
        }
        return groupId;
    } 
    public void sendOneWay(Message message, InetAddress to)
    {
        if ( message.getFrom().equals(to) )
        {
            receive(message);
            return;
        }
        Message processedMessage = SinkManager.processClientMessage(message, to);
        if (processedMessage == null)
        {
            return;
        }
        OutboundTcpConnection connection = getConnection(to, message);
        byte[] data;
        try
        {
            DataOutputBuffer buffer = new DataOutputBuffer();
            Message.serializer().serialize(message, buffer);
            data = buffer.getData();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        assert data.length > 0;
        ByteBuffer buffer = packIt(data , false);
        connection.write(buffer);
    }
    public IAsyncResult sendRR(Message message, InetAddress to)
    {
        IAsyncResult iar = new AsyncResult();
        callbacks.put(message.getMessageId(), iar);
        putTarget(message.getMessageId(), to);
        sendOneWay(message, to);
        return iar;
    }
    public void stream(StreamHeader header, InetAddress to)
    {
        if (DatabaseDescriptor.getEncryptionOptions().internode_encryption == EncryptionOptions.InternodeEncryption.all)
            streamExecutor_.execute(new SSLFileStreamTask(header, to));
        else
            streamExecutor_.execute(new FileStreamTask(header, to));
    }
    public void register(ILatencySubscriber subcriber)
    {
        subscribers.add(subcriber);
    }
    public void waitFor() throws InterruptedException
    {
        while (!streamExecutor_.isTerminated())
            streamExecutor_.awaitTermination(5, TimeUnit.SECONDS);
    }
    public void shutdown()
    {
        logger_.info("Shutting down MessageService...");
        try
        {
            socketThread.close();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
        streamExecutor_.shutdownNow();
        callbacks.shutdown();
        logger_.info("Shutdown complete (no further commands will be processed)");
    }
    public void receive(Message message)
    {
        message = SinkManager.processServerMessage(message);
        if (message == null)
            return;
        Runnable runnable = new MessageDeliveryTask(message);
        ExecutorService stage = StageManager.getStage(message.getMessageType());
        assert stage != null : "No stage for message type " + message.getMessageType();
        stage.execute(runnable);
    }
    public IMessageCallback getRegisteredCallback(String messageId)
    {
        return callbacks.get(messageId);
    }
    public IMessageCallback removeRegisteredCallback(String messageId)
    {
        targets.remove(messageId);
        return callbacks.remove(messageId);
    }
    public long getRegisteredCallbackAge(String messageId)
    {
        return callbacks.getAge(messageId);
    }
    public void responseReceivedFrom(String messageId, InetAddress from)
    {
        removeTarget(messageId, from);
    }
    public static void validateMagic(int magic) throws IOException
    {
        if (magic != PROTOCOL_MAGIC)
            throw new IOException("invalid protocol header");
    }
    public static int getBits(int x, int p, int n)
    {
        return x >>> (p + 1) - n & ~(-1 << n);
    }
    public ByteBuffer packIt(byte[] bytes, boolean compress)
    {
        int header = 0;
        header |= serializerType_.ordinal();
        if (compress)
            header |= 4;
        header |= (version_ << 8);
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + 4 + bytes.length);
        buffer.putInt(PROTOCOL_MAGIC);
        buffer.putInt(header);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return buffer;
    }
    public ByteBuffer constructStreamHeader(StreamHeader streamHeader, boolean compress)
    {
        int header = 0;
        header |= serializerType_.ordinal();
        if ( compress )
            header |= 4;
        header |= 8;
        header |= (version_ << 8);
        byte[] bytes;
        try
        {
            DataOutputBuffer buffer = new DataOutputBuffer();
            StreamHeader.serializer().serialize(streamHeader, buffer);
            bytes = buffer.getData();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        assert bytes.length > 0;
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + 4 + bytes.length);
        buffer.putInt(PROTOCOL_MAGIC);
        buffer.putInt(header);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return buffer;
    }
    public int incrementDroppedMessages(StorageService.Verb verb)
    {
        return droppedMessages.get(verb).incrementAndGet();
    }
    private void logDroppedMessages()
    {
        boolean logTpstats = false;
        for (Map.Entry<StorageService.Verb, AtomicInteger> entry : droppedMessages.entrySet())
        {
            AtomicInteger dropped = entry.getValue();
            if (dropped.get() > 0)
            {
                logTpstats = true;
                logger_.warn("Dropped {} {} messages in the last {}ms",
                             new Object[] {dropped, entry.getKey(), LOG_DROPPED_INTERVAL_IN_MS});
            }
            dropped.set(0);
        }
        if (logTpstats)
            GCInspector.instance.logStats();
    }
    private static class SocketThread extends Thread
    {
        private final ServerSocket server;
        SocketThread(ServerSocket server, String name)
        {
            super(name);
            this.server = server;
        }
        public void run()
        {
            while (true)
            {
                try
                {
                    Socket socket = server.accept();
                    new IncomingTcpConnection(socket).start();
                }
                catch (AsynchronousCloseException e)
                {
                    logger_.info("MessagingService shutting down server thread.");
                    break;
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        void close() throws IOException
        {
            server.close();
        }
    }
    public Map<String, Integer> getCommandPendingTasks()
    {
        Map<String, Integer> pendingTasks = new HashMap<String, Integer>();
        for (Map.Entry<InetAddress, OutboundTcpConnectionPool> entry : connectionManagers_.entrySet())
            pendingTasks.put(entry.getKey().getHostAddress(), entry.getValue().cmdCon.getPendingMessages());
        return pendingTasks;
    }
    public Map<String, Long> getCommandCompletedTasks()
    {
        Map<String, Long> completedTasks = new HashMap<String, Long>();
        for (Map.Entry<InetAddress, OutboundTcpConnectionPool> entry : connectionManagers_.entrySet())
            completedTasks.put(entry.getKey().getHostAddress(), entry.getValue().cmdCon.getCompletedMesssages());
        return completedTasks;
    }
    public Map<String, Integer> getResponsePendingTasks()
    {
        Map<String, Integer> pendingTasks = new HashMap<String, Integer>();
        for (Map.Entry<InetAddress, OutboundTcpConnectionPool> entry : connectionManagers_.entrySet())
            pendingTasks.put(entry.getKey().getHostAddress(), entry.getValue().ackCon.getPendingMessages());
        return pendingTasks;
    }
    public Map<String, Long> getResponseCompletedTasks()
    {
        Map<String, Long> completedTasks = new HashMap<String, Long>();
        for (Map.Entry<InetAddress, OutboundTcpConnectionPool> entry : connectionManagers_.entrySet())
            completedTasks.put(entry.getKey().getHostAddress(), entry.getValue().ackCon.getCompletedMesssages());
        return completedTasks;
    }
}
