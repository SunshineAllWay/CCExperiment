package org.apache.cassandra.hadoop;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.TokenRange;
import org.apache.cassandra.thrift.TBinaryProtocol;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.*;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
public class ColumnFamilyInputFormat extends InputFormat<ByteBuffer, SortedMap<ByteBuffer, IColumn>>
{
    private static final Logger logger = LoggerFactory.getLogger(ColumnFamilyInputFormat.class);
    private String keyspace;
    private String cfName;
    private static void validateConfiguration(Configuration conf)
    {
        if (ConfigHelper.getInputKeyspace(conf) == null || ConfigHelper.getInputColumnFamily(conf) == null)
        {
            throw new UnsupportedOperationException("you must set the keyspace and columnfamily with setColumnFamily()");
        }
        if (ConfigHelper.getInputSlicePredicate(conf) == null)
        {
            throw new UnsupportedOperationException("you must set the predicate with setPredicate");
        }
    }
    public List<InputSplit> getSplits(JobContext context) throws IOException
    {
        Configuration conf = context.getConfiguration();
        validateConfiguration(conf);
        List<TokenRange> masterRangeNodes = getRangeMap(conf);
        keyspace = ConfigHelper.getInputKeyspace(context.getConfiguration());
        cfName = ConfigHelper.getInputColumnFamily(context.getConfiguration());
        ExecutorService executor = Executors.newCachedThreadPool();
        List<InputSplit> splits = new ArrayList<InputSplit>();
        try
        {
            List<Future<List<InputSplit>>> splitfutures = new ArrayList<Future<List<InputSplit>>>();
            for (TokenRange range : masterRangeNodes)
            {
                    splitfutures.add(executor.submit(new SplitCallable(range, conf)));
            }
            for (Future<List<InputSplit>> futureInputSplits : splitfutures)
            {
                try
                {
                    splits.addAll(futureInputSplits.get());
                }
                catch (Exception e)
                {
                    throw new IOException("Could not get input splits", e);
                }
            }
        }
        finally
        {
            executor.shutdownNow();
        }
        assert splits.size() > 0;
        Collections.shuffle(splits, new Random(System.nanoTime()));
        return splits;
    }
    class SplitCallable implements Callable<List<InputSplit>>
    {
        private final TokenRange range;
        private final Configuration conf;
        public SplitCallable(TokenRange tr, Configuration conf)
        {
            this.range = tr;
            this.conf = conf;
        }
        public List<InputSplit> call() throws Exception
        {
            ArrayList<InputSplit> splits = new ArrayList<InputSplit>();
            List<String> tokens = getSubSplits(keyspace, cfName, range, conf);
            String[] endpoints = range.endpoints.toArray(new String[range.endpoints.size()]);
            for (int i = 0; i < endpoints.length; i++)
            {
                endpoints[i] = InetAddress.getByName(endpoints[i]).getHostName();
            }
            for (int i = 1; i < tokens.size(); i++)
            {
                ColumnFamilySplit split = new ColumnFamilySplit(tokens.get(i - 1), tokens.get(i), endpoints);
                logger.debug("adding " + split);
                splits.add(split);
            }
            return splits;
        }
    }
    private List<String> getSubSplits(String keyspace, String cfName, TokenRange range, Configuration conf) throws IOException
    {
        int splitsize = ConfigHelper.getInputSplitSize(conf);
        for (String host : range.endpoints)
        {
            try
            {
                Cassandra.Client client = createConnection(host, ConfigHelper.getRpcPort(conf), true);
                client.set_keyspace(keyspace);
                return client.describe_splits(cfName, range.start_token, range.end_token, splitsize);
            }
            catch (IOException e)
            {
                logger.debug("failed connect to endpoint " + host, e);
            }
            catch (TException e)
            {
                throw new RuntimeException(e);
            }
            catch (InvalidRequestException e)
            {
                throw new RuntimeException(e);
            }
        }
        throw new IOException("failed connecting to all endpoints " + StringUtils.join(range.endpoints, ","));
    }
    private static Cassandra.Client createConnection(String host, Integer port, boolean framed) throws IOException
    {
        TSocket socket = new TSocket(host, port);
        TTransport trans = framed ? new TFramedTransport(socket) : socket;
        try
        {
            trans.open();
        }
        catch (TTransportException e)
        {
            throw new IOException("unable to connect to server", e);
        }
        return new Cassandra.Client(new TBinaryProtocol(trans));
    }
    private List<TokenRange> getRangeMap(Configuration conf) throws IOException
    {
        Cassandra.Client client = createConnection(ConfigHelper.getInitialAddress(conf), ConfigHelper.getRpcPort(conf), true);
        List<TokenRange> map;
        try
        {
            map = client.describe_ring(ConfigHelper.getInputKeyspace(conf));
        }
        catch (TException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvalidRequestException e)
        {
            throw new RuntimeException(e);
        }
        return map;
    }
    public RecordReader<ByteBuffer, SortedMap<ByteBuffer, IColumn>> createRecordReader(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException
    {
        return new ColumnFamilyRecordReader();
    }
}
