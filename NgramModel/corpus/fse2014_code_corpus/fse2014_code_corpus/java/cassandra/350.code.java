package org.apache.cassandra.service;
import java.io.*;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.concurrent.Stage;
import org.apache.cassandra.concurrent.StageManager;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.CompactionManager;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.io.AbstractCompactedRow;
import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.io.sstable.SSTableReader;
import org.apache.cassandra.net.CompactEndpointSerializationHelper;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.streaming.OperationType;
import org.apache.cassandra.streaming.StreamIn;
import org.apache.cassandra.streaming.StreamOut;
import org.apache.cassandra.streaming.StreamOutSession;
import org.apache.cassandra.utils.*;
public class AntiEntropyService
{
    private static final Logger logger = LoggerFactory.getLogger(AntiEntropyService.class);
    public static final AntiEntropyService instance = new AntiEntropyService();
    public final static long REQUEST_TIMEOUT = 48*60*60*1000;
    private final ExpiringMap<String, Map<TreeRequest, TreePair>> requests;
    private final ConcurrentMap<String, RepairSession.Callback> sessions;
    protected AntiEntropyService()
    {
        requests = new ExpiringMap<String, Map<TreeRequest, TreePair>>(REQUEST_TIMEOUT);
        sessions = new ConcurrentHashMap<String, RepairSession.Callback>();
    }
    public RepairSession getRepairSession(String tablename, String... cfnames)
    {
        return new RepairSession(tablename, cfnames);
    }
    void completedRequest(TreeRequest request)
    {
        sessions.get(request.sessionid).completed(request);
    }
    private Map<TreeRequest, TreePair> rendezvousPairs(String sessionid)
    {
        Map<TreeRequest, TreePair> ctrees = requests.get(sessionid);
        if (ctrees == null)
        {
            ctrees = new HashMap<TreeRequest, TreePair>();
            requests.put(sessionid, ctrees);
        }
        return ctrees;
    }
    static Set<InetAddress> getNeighbors(String table)
    {
        StorageService ss = StorageService.instance;
        Set<InetAddress> neighbors = new HashSet<InetAddress>();
        Map<Range, List<InetAddress>> replicaSets = ss.getRangeToAddressMap(table);
        for (Range range : ss.getLocalRanges(table))
        {
            neighbors.addAll(replicaSets.get(range));
        }
        neighbors.remove(FBUtilities.getLocalAddress());
        return neighbors;
    }
    private void rendezvous(TreeRequest request, MerkleTree tree)
    {
        InetAddress LOCAL = FBUtilities.getLocalAddress();
        Map<TreeRequest, TreePair> ctrees = rendezvousPairs(request.sessionid);
        List<Differencer> differencers = new ArrayList<Differencer>();
        if (LOCAL.equals(request.endpoint))
        {
            for (InetAddress neighbor : getNeighbors(request.cf.left))
            {
                TreeRequest remotereq = new TreeRequest(request.sessionid, neighbor, request.cf);
                TreePair waiting = ctrees.remove(remotereq);
                if (waiting != null && waiting.right != null)
                {
                    differencers.add(new Differencer(remotereq, tree, waiting.right));
                    continue;
                }
                ctrees.put(remotereq, new TreePair(tree, null));
                logger.debug("Stored local tree for " + request + " to wait for " + remotereq);
            }
        }
        else
        {
            TreePair waiting = ctrees.remove(request);
            if (waiting != null && waiting.left != null)
            {
                differencers.add(new Differencer(request, waiting.left, tree));
            }
            else
            {
                ctrees.put(request, new TreePair(null, tree));
                logger.debug("Stored remote tree for " + request + " to wait for local tree.");
            }
        }
        for (Differencer differencer : differencers)
        {
            logger.info("Queueing comparison " + differencer);
            StageManager.getStage(Stage.ANTI_ENTROPY).execute(differencer);
        }
    }
    TreeRequest request(String sessionid, InetAddress remote, String ksname, String cfname)
    {
        TreeRequest request = new TreeRequest(sessionid, remote, new CFPair(ksname, cfname));
        MessagingService.instance().sendOneWay(TreeRequestVerbHandler.makeVerb(request), remote);
        return request;
    }
    void respond(Validator validator, InetAddress local)
    {
        MessagingService ms = MessagingService.instance();
        try
        {
            Message message = TreeResponseVerbHandler.makeVerb(local, validator);
            logger.info("Sending AEService tree for " + validator.request);
            ms.sendOneWay(message, validator.request.endpoint);
        }
        catch (Exception e)
        {
            logger.error("Could not send valid tree for request " + validator.request, e);
        }
    }
    public static class Validator implements Callable<Object>
    {
        public final TreeRequest request;
        public final MerkleTree tree;
        private transient List<MerkleTree.RowHash> minrows;
        private transient Token mintoken;
        private transient long validated;
        private transient MerkleTree.TreeRange range;
        private transient MerkleTree.TreeRangeIterator ranges;
        public final static MerkleTree.RowHash EMPTY_ROW = new MerkleTree.RowHash(null, new byte[0]);
        Validator(TreeRequest request)
        {
            this(request,
                 new MerkleTree(DatabaseDescriptor.getPartitioner(), MerkleTree.RECOMMENDED_DEPTH, (int)Math.pow(2, 15)));
        }
        Validator(TreeRequest request, MerkleTree tree)
        {
            this.request = request;
            this.tree = tree;
            minrows = new ArrayList<MerkleTree.RowHash>();
            mintoken = null;
            validated = 0;
            range = null;
            ranges = null;
        }
        public void prepare(ColumnFamilyStore cfs)
        {
            List<DecoratedKey> keys = new ArrayList<DecoratedKey>();
            for (DecoratedKey sample : cfs.allKeySamples())
                keys.add(sample);
            if (keys.isEmpty())
            {
                tree.init();
            }
            else
            {
                int numkeys = keys.size();
                Random random = new Random();
                while (true)
                {
                    DecoratedKey dk = keys.get(random.nextInt(numkeys));
                    if (!tree.split(dk.token))
                        break;
                }
            }
            logger.debug("Prepared AEService tree of size " + tree.size() + " for " + request);
            mintoken = tree.partitioner().getMinimumToken();
            ranges = tree.invalids(new Range(mintoken, mintoken));
        }
        public void add(AbstractCompactedRow row)
        {
            if (mintoken != null)
            {
                assert ranges != null : "Validator was not prepared()";
                if (row.key.token.compareTo(mintoken) == 0)
                {
                    minrows.add(rowHash(row));
                    return;
                }
                mintoken = null;
            }
            if (range == null)
                range = ranges.next();
            while (!range.contains(row.key.token))
            {
                range.addHash(EMPTY_ROW);
                range = ranges.next();
            }
            range.addHash(rowHash(row));
        }
        private MerkleTree.RowHash rowHash(AbstractCompactedRow row)
        {
            validated++;
            MessageDigest digest = null;
            try
            {
                digest = MessageDigest.getInstance("SHA-256");
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new AssertionError(e);
            }
            row.update(digest);
            return new MerkleTree.RowHash(row.key.token, digest.digest());
        }
        public void complete()
        {
            assert ranges != null : "Validator was not prepared()";
            if (range != null)
                range.addHash(EMPTY_ROW);
            while (ranges.hasNext())
            {
                range = ranges.next();
                range.addHash(EMPTY_ROW);
            }
            if (!minrows.isEmpty())
                for (MerkleTree.RowHash minrow : minrows)
                    range.addHash(minrow);
            StageManager.getStage(Stage.ANTI_ENTROPY).submit(this);
            logger.debug("Validated " + validated + " rows into AEService tree for " + request);
        }
        public Object call() throws Exception
        {
            AntiEntropyService.instance.respond(this, FBUtilities.getLocalAddress());
            return AntiEntropyService.class;
        }
    }
    public static class Differencer implements Runnable
    {
        public final TreeRequest request;
        public final MerkleTree ltree;
        public final MerkleTree rtree;
        public final List<Range> differences;
        public Differencer(TreeRequest request, MerkleTree ltree, MerkleTree rtree)
        {
            this.request = request;
            this.ltree = ltree;
            this.rtree = rtree;
            differences = new ArrayList<Range>();
        }
        public void run()
        {
            InetAddress local = FBUtilities.getLocalAddress();
            StorageService ss = StorageService.instance;
            if (ltree.partitioner() == null)
                ltree.partitioner(StorageService.getPartitioner());
            if (rtree.partitioner() == null)
                rtree.partitioner(StorageService.getPartitioner());
            Set<Range> interesting = new HashSet(ss.getRangesForEndpoint(request.cf.left, local));
            interesting.retainAll(ss.getRangesForEndpoint(request.cf.left, request.endpoint));
            for (MerkleTree.TreeRange diff : MerkleTree.difference(ltree, rtree))
                for (Range localrange: interesting)
                    differences.addAll(diff.intersectionWith(localrange));
            String format = "Endpoints " + local + " and " + request.endpoint + " %s for " + request.cf;
            if (differences.isEmpty())
            {
                logger.info(String.format(format, "are consistent"));
                AntiEntropyService.instance.completedRequest(request);
                return;
            }
            logger.info(String.format(format, "have " + differences.size() + " range(s) out of sync"));
            try
            {
                performStreamingRepair();
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        void performStreamingRepair() throws IOException
        {
            logger.info("Performing streaming repair of " + differences.size() + " ranges for " + request);
            ColumnFamilyStore cfstore = Table.open(request.cf.left).getColumnFamilyStore(request.cf.right);
            try
            {
                List<Range> ranges = new ArrayList<Range>(differences);
                Collection<SSTableReader> sstables = cfstore.getSSTables();
                Callback callback = new Callback();
                StreamOutSession outsession = StreamOutSession.create(request.cf.left, request.endpoint, callback);
                StreamOut.transferSSTables(outsession, sstables, ranges, OperationType.AES);
                StreamIn.requestRanges(request.endpoint, request.cf.left, ranges, callback, OperationType.AES);
            }
            catch(Exception e)
            {
                throw new IOException("Streaming repair failed.", e);
            }
        }
        public String toString()
        {
            return "#<Differencer " + request + ">";
        }
        class Callback extends WrappedRunnable
        {
            private final AtomicInteger outstanding = new AtomicInteger(2);
            protected void runMayThrow() throws Exception
            {
                if (outstanding.decrementAndGet() > 0)
                    return;
                logger.info("Finished streaming repair for " + request);
                AntiEntropyService.instance.completedRequest(request);
            }
        }
    }
    public static class TreeRequestVerbHandler implements IVerbHandler, ICompactSerializer<TreeRequest>
    {
        public static final TreeRequestVerbHandler SERIALIZER = new TreeRequestVerbHandler();
        static Message makeVerb(TreeRequest request)
        {
            try
            {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                SERIALIZER.serialize(request, dos);
                return new Message(FBUtilities.getLocalAddress(), StorageService.Verb.TREE_REQUEST, bos.toByteArray());
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        public void serialize(TreeRequest request, DataOutputStream dos) throws IOException
        {
            dos.writeUTF(request.sessionid);
            CompactEndpointSerializationHelper.serialize(request.endpoint, dos);
            dos.writeUTF(request.cf.left);
            dos.writeUTF(request.cf.right);
        }
        public TreeRequest deserialize(DataInputStream dis) throws IOException
        {
            return new TreeRequest(dis.readUTF(),
                                   CompactEndpointSerializationHelper.deserialize(dis),
                                   new CFPair(dis.readUTF(), dis.readUTF()));
        }
        public void doVerb(Message message)
        { 
            byte[] bytes = message.getMessageBody();
            DataInputStream buffer = new DataInputStream(new ByteArrayInputStream(bytes));
            try
            {
                TreeRequest remotereq = this.deserialize(buffer);
                TreeRequest request = new TreeRequest(remotereq.sessionid, message.getFrom(), remotereq.cf);
                ColumnFamilyStore store = Table.open(request.cf.left).getColumnFamilyStore(request.cf.right);
                Validator validator = new Validator(request);
                logger.debug("Queueing validation compaction for " + request);
                CompactionManager.instance.submitValidation(store, validator);
            }
            catch (IOException e)
            {
                throw new IOError(e);            
            }
        }
    }
    public static class TreeResponseVerbHandler implements IVerbHandler, ICompactSerializer<Validator>
    {
        public static final TreeResponseVerbHandler SERIALIZER = new TreeResponseVerbHandler();
        static Message makeVerb(InetAddress local, Validator validator)
        {
            try
            {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                SERIALIZER.serialize(validator, dos);
                return new Message(local, StorageService.Verb.TREE_RESPONSE, bos.toByteArray());
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        public void serialize(Validator v, DataOutputStream dos) throws IOException
        {
            TreeRequestVerbHandler.SERIALIZER.serialize(v.request, dos);
            ObjectOutputStream oos = new ObjectOutputStream(dos);
            oos.writeObject(v.tree);
            oos.flush();
        }
        public Validator deserialize(DataInputStream dis) throws IOException
        {
            final TreeRequest request = TreeRequestVerbHandler.SERIALIZER.deserialize(dis);
            ObjectInputStream ois = new ObjectInputStream(dis);
            try
            {
                return new Validator(request, (MerkleTree)ois.readObject());
            }
            catch(Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        public void doVerb(Message message)
        { 
            byte[] bytes = message.getMessageBody();
            DataInputStream buffer = new DataInputStream(new ByteArrayInputStream(bytes));
            try
            {
                Validator response = this.deserialize(buffer);
                TreeRequest request = new TreeRequest(response.request.sessionid, message.getFrom(), response.request.cf);
                AntiEntropyService.instance.rendezvous(request, response.tree);
            }
            catch (IOException e)
            {
                throw new IOError(e);
            }
        }
    }
    static class CFPair extends Pair<String,String>
    {
        public CFPair(String table, String cf)
        {
            super(table, cf);
            assert table != null && cf != null;
        }
    }
    static class TreePair extends Pair<MerkleTree,MerkleTree>
    {
        public TreePair(MerkleTree local, MerkleTree remote)
        {
            super(local, remote);
        }
    }
    public static class TreeRequest
    {
        public final String sessionid;
        public final InetAddress endpoint;
        public final CFPair cf;
        public TreeRequest(String sessionid, InetAddress endpoint, CFPair cf)
        {
            this.sessionid = sessionid;
            this.endpoint = endpoint;
            this.cf = cf;
        }
        @Override
        public final int hashCode()
        {
            return Objects.hashCode(sessionid, endpoint, cf);
        }
        @Override
        public final boolean equals(Object o)
        {
            if(!(o instanceof TreeRequest))
                return false;
            TreeRequest that = (TreeRequest)o;
            return Objects.equal(sessionid, that.sessionid) && Objects.equal(endpoint, that.endpoint) && Objects.equal(cf, that.cf);
        }
        @Override
        public String toString()
        {
            return "#<TreeRequest " + sessionid + ", " + endpoint + ", " + cf + ">";
        }
    }
    class RepairSession extends Thread
    {
        private final String tablename;
        private final String[] cfnames;
        private final SimpleCondition requestsMade;
        private final ConcurrentHashMap<TreeRequest,Object> requests;
        public RepairSession(String tablename, String... cfnames)
        {
            super("manual-repair-" + UUID.randomUUID());
            this.tablename = tablename;
            this.cfnames = cfnames;
            this.requestsMade = new SimpleCondition();
            this.requests = new ConcurrentHashMap<TreeRequest,Object>();
        }
        public void blockUntilRunning() throws InterruptedException
        {
            requestsMade.await();
        }
        @Override
        public void run()
        {
            Set<InetAddress> endpoints = AntiEntropyService.getNeighbors(tablename);
            if (endpoints.isEmpty())
            {
                logger.info("No neighbors to repair with: " + getName() + " completed.");
                return;
            }
            Callback callback = new Callback();
            AntiEntropyService.this.sessions.put(getName(), callback);
            try
            {
                for (String cfname : cfnames)
                {
                    for (InetAddress endpoint : endpoints)
                        requests.put(AntiEntropyService.this.request(getName(), endpoint, tablename, cfname), this);
                    AntiEntropyService.this.request(getName(), FBUtilities.getLocalAddress(), tablename, cfname);
                }
                logger.info("Waiting for repair requests: " + requests.keySet());
                requestsMade.signalAll();
                callback.completed.await();
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException("Interrupted while waiting for repair: repair will continue in the background.");
            }
        }
        class Callback
        {
            public final SimpleCondition completed = new SimpleCondition();
            public void completed(TreeRequest request)
            {
                try
                {
                    blockUntilRunning();
                }
                catch (InterruptedException e)
                {
                    throw new AssertionError(e);
                }
                requests.remove(request);
                logger.info("{} completed successfully: {} outstanding.", request, requests.size());
                if (!requests.isEmpty())
                    return;
                logger.info("Session " + getName() + " completed successfully.");
                AntiEntropyService.this.sessions.remove(getName());
                completed.signalAll();
            }
        }
    }
}
