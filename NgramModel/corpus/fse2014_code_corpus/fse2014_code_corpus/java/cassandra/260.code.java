package org.apache.cassandra.hadoop;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.cassandra.client.RingCache;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.thrift.*;
import org.apache.cassandra.utils.Pair;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TSocket;
import static org.apache.cassandra.io.SerDeUtils.copy;
final class ColumnFamilyRecordWriter extends RecordWriter<ByteBuffer,List<org.apache.cassandra.hadoop.avro.Mutation>>
implements org.apache.hadoop.mapred.RecordWriter<ByteBuffer,List<org.apache.cassandra.hadoop.avro.Mutation>>
{
    private final Configuration conf;
    private final RingCache ringCache;
    private final int queueSize;
    private final Map<Range,RangeClient> clients;
    private final long batchThreshold;
    ColumnFamilyRecordWriter(TaskAttemptContext context) throws IOException
    {
        this(context.getConfiguration());
    }
    ColumnFamilyRecordWriter(Configuration conf) throws IOException
    {
        this.conf = conf;
        this.ringCache = new RingCache(ConfigHelper.getOutputKeyspace(conf),
                                       ConfigHelper.getPartitioner(conf),
                                       ConfigHelper.getInitialAddress(conf),
                                       ConfigHelper.getRpcPort(conf));
        this.queueSize = conf.getInt(ColumnFamilyOutputFormat.QUEUE_SIZE, 32 * Runtime.getRuntime().availableProcessors());
        this.clients = new HashMap<Range,RangeClient>();
        batchThreshold = conf.getLong(ColumnFamilyOutputFormat.BATCH_THRESHOLD, 32);
    }
    @Override
    public void write(ByteBuffer keybuff, List<org.apache.cassandra.hadoop.avro.Mutation> value) throws IOException
    {
        Range range = ringCache.getRange(keybuff);
        RangeClient client = clients.get(range);
        if (client == null)
        {
            client = new RangeClient(ringCache.getEndpoint(range));
            client.start();
            clients.put(range, client);
        }
        for (org.apache.cassandra.hadoop.avro.Mutation amut : value)
            client.put(new Pair<ByteBuffer,Mutation>(keybuff, avroToThrift(amut)));
    }
    private Mutation avroToThrift(org.apache.cassandra.hadoop.avro.Mutation amut)
    {
        Mutation mutation = new Mutation();
        org.apache.cassandra.hadoop.avro.ColumnOrSuperColumn acosc = amut.column_or_supercolumn;
        if (acosc != null)
        {
            ColumnOrSuperColumn cosc = new ColumnOrSuperColumn();
            mutation.setColumn_or_supercolumn(cosc);
            if (acosc.column != null)
                cosc.setColumn(avroToThrift(acosc.column));
            else
            {
                ByteBuffer scolname = acosc.super_column.name;
                List<Column> scolcols = new ArrayList<Column>(acosc.super_column.columns.size());
                for (org.apache.cassandra.hadoop.avro.Column acol : acosc.super_column.columns)
                    scolcols.add(avroToThrift(acol));
                cosc.setSuper_column(new SuperColumn(scolname, scolcols));
            }
        }
        else
        {
            Deletion deletion = new Deletion(amut.deletion.timestamp);
            mutation.setDeletion(deletion);
            org.apache.cassandra.hadoop.avro.SlicePredicate apred = amut.deletion.predicate;
            if (amut.deletion.super_column != null)
                deletion.setSuper_column(copy(amut.deletion.super_column));
            else if (apred.column_names != null)
            {
                List<ByteBuffer> names = new ArrayList<ByteBuffer>(apred.column_names.size());
                for (ByteBuffer name : apred.column_names)
                    names.add(name);
                deletion.setPredicate(new SlicePredicate().setColumn_names(names));
            }
            else
            {
                deletion.setPredicate(new SlicePredicate().setSlice_range(avroToThrift(apred.slice_range)));
            }
        }
        return mutation;
    }
    private SliceRange avroToThrift(org.apache.cassandra.hadoop.avro.SliceRange asr)
    {
        return new SliceRange(asr.start, asr.finish, asr.reversed, asr.count);
    }
    private Column avroToThrift(org.apache.cassandra.hadoop.avro.Column acol)
    {
        return new Column(acol.name, acol.value, acol.timestamp);
    }
    @Override
    public void close(TaskAttemptContext context) throws IOException, InterruptedException
    {
        close((org.apache.hadoop.mapred.Reporter)null);
    }
    @Deprecated
    public void close(org.apache.hadoop.mapred.Reporter reporter) throws IOException
    {
        for (RangeClient client : clients.values())
            client.stopNicely();
        try
        {
            for (RangeClient client : clients.values())
            {
                client.join();
                client.close();
            }
        }
        catch (InterruptedException e)
        {
            throw new AssertionError(e);
        }
    }
    public class RangeClient extends Thread
    {
        private final List<InetAddress> endpoints;
        private final String columnFamily = ConfigHelper.getOutputColumnFamily(conf);
        private final BlockingQueue<Pair<ByteBuffer, Mutation>> queue = new ArrayBlockingQueue<Pair<ByteBuffer,Mutation>>(queueSize);
        private volatile boolean run = true;
        private volatile IOException lastException;
        private Cassandra.Client thriftClient;
        private TSocket thriftSocket;
        public RangeClient(List<InetAddress> endpoints)
        {
            super("client-" + endpoints);
            this.endpoints = endpoints;
         }
        public void put(Pair<ByteBuffer,Mutation> value) throws IOException
        {
            while (true)
            {
                if (lastException != null)
                    throw lastException;
                try
                {
                    if (queue.offer(value, 100, TimeUnit.MILLISECONDS))
                        break;
                }
                catch (InterruptedException e)
                {
                    throw new AssertionError(e);
                }
            }
        }
        public void stopNicely() throws IOException
        {
            if (lastException != null)
                throw lastException;
            run = false;
            interrupt();
        }
        public void close()
        {
            if (thriftSocket != null)
            {
                thriftSocket.close();
                thriftSocket = null;
                thriftClient = null;
            }
        }
        public void run()
        {
            outer:
            while (run || !queue.isEmpty())
            {
                Pair<ByteBuffer, Mutation> mutation;
                try
                {
                    mutation = queue.take();
                }
                catch (InterruptedException e)
                {
                    continue;
                }
                Map<ByteBuffer, Map<String, List<Mutation>>> batch = new HashMap<ByteBuffer, Map<String, List<Mutation>>>();
                while (batch.size() < batchThreshold)
                {
                    Map<String, List<Mutation>> subBatch = batch.get(mutation.left);
                    if (subBatch == null)
                    {
                        subBatch = Collections.singletonMap(columnFamily, (List<Mutation>) new ArrayList<Mutation>());
                        batch.put(mutation.left, subBatch);
                    }
                    subBatch.get(columnFamily).add(mutation.right);
                    if ((mutation = queue.poll()) == null)
                        break;
                }
                Iterator<InetAddress> iter = endpoints.iterator();
                while (true)
                {
                    try
                    {
                        thriftClient.batch_mutate(batch, ConsistencyLevel.ONE);
                        break;
                    }
                    catch (Exception e)
                    {
                        close();
                        if (!iter.hasNext())
                        {
                            lastException = new IOException(e);
                            break outer;
                        }
                    }
                    try
                    {
                        InetAddress address = iter.next();
                        thriftSocket = new TSocket(address.getHostName(), ConfigHelper.getRpcPort(conf));
                        thriftClient = ColumnFamilyOutputFormat.createAuthenticatedClient(thriftSocket, conf);
                    }
                    catch (Exception e)
                    {
                        close();
                        if ((!(e instanceof TException)) || !iter.hasNext())
                        {
                            lastException = new IOException(e);
                            break outer;
                        }
                    }
                }
            }
        }
        @Override
        public String toString()
        {
            return "#<Client for " + endpoints.toString() + ">";
        }
    }
}
