package org.apache.cassandra.client;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ColumnPath;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.cassandra.utils.ByteBufferUtil;
public class TestRingCache
{
    private RingCache ringCache;
    private Cassandra.Client thriftClient;
    public TestRingCache(String keyspace) throws IOException
    {
        String seed = DatabaseDescriptor.getSeeds().iterator().next().getHostAddress();
    	ringCache = new RingCache(keyspace, DatabaseDescriptor.getPartitioner(), seed, DatabaseDescriptor.getRpcPort());
    }
    private void setup(String server, int port) throws Exception
    {
        TSocket socket = new TSocket(server, port);
        System.out.println(" connected to " + server + ":" + port + ".");
        TBinaryProtocol binaryProtocol = new TBinaryProtocol(new TFramedTransport(socket));
        Cassandra.Client cassandraClient = new Cassandra.Client(binaryProtocol);
        socket.open();
        thriftClient = cassandraClient;
    }
    public static void main(String[] args) throws Throwable
    {
        int minRow;
        int maxRow;
        String rowPrefix, keyspace = "Keyspace1";
        if (args.length > 0)
        {
            keyspace = args[0];
            rowPrefix = args[1];
            minRow = Integer.parseInt(args[2]);
            maxRow = minRow + 1;
        }
        else
        {
            minRow = 1;
            maxRow = 10;
            rowPrefix = "row";
        }
        TestRingCache tester = new TestRingCache(keyspace);
        for (int nRows = minRow; nRows < maxRow; nRows++)
        {
            ByteBuffer row = ByteBuffer.wrap((rowPrefix + nRows).getBytes());
            ColumnPath col = new ColumnPath("Standard1").setSuper_column((ByteBuffer)null).setColumn("col1".getBytes());
            ColumnParent parent = new ColumnParent("Standard1").setSuper_column((ByteBuffer)null);
            Collection<InetAddress> endpoints = tester.ringCache.getEndpoint(row);
            InetAddress firstEndpoint = endpoints.iterator().next();
            System.out.printf("hosts with key %s : %s; choose %s%n",
                              new String(row.array()), StringUtils.join(endpoints, ","), firstEndpoint);
            tester.setup(firstEndpoint.getHostAddress(), DatabaseDescriptor.getRpcPort());
            tester.thriftClient.set_keyspace(keyspace);
            tester.thriftClient.insert(row, parent, new Column(ByteBufferUtil.bytes("col1"), ByteBufferUtil.bytes("val1"), 1), ConsistencyLevel.ONE);
            Column column = tester.thriftClient.get(row, col, ConsistencyLevel.ONE).column;
            System.out.println("read row " + new String(row.array()) + " " + new String(column.name.array()) + ":" + new String(column.value.array()) + ":" + column.timestamp);
        }
        System.exit(1);
    }
}
