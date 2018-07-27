package org.apache.cassandra.hadoop;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.auth.SimpleAuthenticator;
import org.apache.cassandra.hadoop.avro.Mutation;
import org.apache.cassandra.thrift.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.*;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
public class ColumnFamilyOutputFormat extends OutputFormat<ByteBuffer,List<Mutation>>
    implements org.apache.hadoop.mapred.OutputFormat<ByteBuffer,List<Mutation>>
{
    private static final Logger logger = LoggerFactory.getLogger(ColumnFamilyOutputFormat.class);
    public static final String BATCH_THRESHOLD = "mapreduce.output.columnfamilyoutputformat.batch.threshold";
    public static final String QUEUE_SIZE = "mapreduce.output.columnfamilyoutputformat.queue.size";
    @Override
    public void checkOutputSpecs(JobContext context)
    {
        checkOutputSpecs(context.getConfiguration());
    }
    private void checkOutputSpecs(Configuration conf)
    {
        if (ConfigHelper.getOutputKeyspace(conf) == null || ConfigHelper.getOutputColumnFamily(conf) == null)
        {
            throw new UnsupportedOperationException("you must set the keyspace and columnfamily with setColumnFamily()");
        }
    }
    @Override
    public OutputCommitter getOutputCommitter(TaskAttemptContext context) throws IOException, InterruptedException
    {
        return new NullOutputCommitter();
    }
    @Deprecated
    public void checkOutputSpecs(org.apache.hadoop.fs.FileSystem filesystem, org.apache.hadoop.mapred.JobConf job) throws IOException
    {
        checkOutputSpecs(job);
    }
    @Deprecated @Override
    public ColumnFamilyRecordWriter getRecordWriter(org.apache.hadoop.fs.FileSystem filesystem, org.apache.hadoop.mapred.JobConf job, String name, org.apache.hadoop.util.Progressable progress) throws IOException
    {
        return new ColumnFamilyRecordWriter(job);
    }
    @Override
    public ColumnFamilyRecordWriter getRecordWriter(final TaskAttemptContext context) throws IOException, InterruptedException
    {
        return new ColumnFamilyRecordWriter(context);
    }
    public static Cassandra.Client createAuthenticatedClient(TSocket socket, Configuration conf)
    throws InvalidRequestException, TException, AuthenticationException, AuthorizationException
    {
        TBinaryProtocol binaryProtocol = new TBinaryProtocol(new TFramedTransport(socket));
        Cassandra.Client client = new Cassandra.Client(binaryProtocol);
        socket.open();
        client.set_keyspace(ConfigHelper.getOutputKeyspace(conf));
        if (ConfigHelper.getOutputKeyspaceUserName(conf) != null)
        {
            Map<String, String> creds = new HashMap<String, String>();
            creds.put(SimpleAuthenticator.USERNAME_KEY, ConfigHelper.getOutputKeyspaceUserName(conf));
            creds.put(SimpleAuthenticator.PASSWORD_KEY, ConfigHelper.getOutputKeyspacePassword(conf));
            AuthenticationRequest authRequest = new AuthenticationRequest(creds);
            client.login(authRequest);
        }
        return client;
    }
    public static class NullOutputCommitter extends OutputCommitter
    {
        public void abortTask(TaskAttemptContext taskContext) { }
        public void cleanupJob(JobContext jobContext) { }
        public void commitTask(TaskAttemptContext taskContext) { }
        public boolean needsTaskCommit(TaskAttemptContext taskContext)
        {
            return false;
        }
        public void setupJob(JobContext jobContext) { }
        public void setupTask(TaskAttemptContext taskContext) { }
    }
}
