package org.apache.cassandra;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;
import org.apache.cassandra.thrift.*;
import org.apache.cassandra.tools.NodeProbe;
import org.apache.cassandra.utils.WrappedRunnable;
import org.apache.cassandra.CassandraServiceController.Failure;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
public class MovementTest extends TestBase
{
    private static final String STANDARD_CF = "Standard1";
    private static final ColumnParent STANDARD = new ColumnParent(STANDARD_CF);
    private static Map<ByteBuffer,List<ColumnOrSuperColumn>> insertBatch(Cassandra.Client client) throws Exception
    {
        final int N = 1000;
        Column col1 = new Column(
            ByteBuffer.wrap("c1".getBytes()),
            ByteBuffer.wrap("v1".getBytes()),
            0
            );
        Column col2 = new Column(
            ByteBuffer.wrap("c2".getBytes()),
            ByteBuffer.wrap("v2".getBytes()),
            0
            );
        Map<ByteBuffer,List<ColumnOrSuperColumn>> rows = new HashMap<ByteBuffer, List<ColumnOrSuperColumn>>();
        Map<ByteBuffer,Map<String,List<Mutation>>> batch = new HashMap<ByteBuffer,Map<String,List<Mutation>>>();
        for (int i = 0; i < N; i++)
        {
            String rawKey = String.format("test.key.%d", i);
            ByteBuffer key = ByteBuffer.wrap(rawKey.getBytes());
            Mutation m1 = (new Mutation()).setColumn_or_supercolumn((new ColumnOrSuperColumn()).setColumn(col1));
            Mutation m2 = (new Mutation()).setColumn_or_supercolumn((new ColumnOrSuperColumn()).setColumn(col2));
            rows.put(key, Arrays.asList(m1.getColumn_or_supercolumn(),
                                        m2.getColumn_or_supercolumn()));
            Map<String,List<Mutation>> rowmap = new HashMap<String,List<Mutation>>();
            rowmap.put(STANDARD_CF, Arrays.asList(m1, m2));
            batch.put(key, rowmap);
        }
        client.batch_mutate(batch, ConsistencyLevel.ONE);
        return rows;
    }
    private static void verifyBatch(Cassandra.Client client, Map<ByteBuffer,List<ColumnOrSuperColumn>> batch) throws Exception
    {
        for (Map.Entry<ByteBuffer,List<ColumnOrSuperColumn>> entry : batch.entrySet())
        {
            SlicePredicate sp = new SlicePredicate();
            sp.setSlice_range(
                new SliceRange(
                    ByteBuffer.wrap(new byte[0]),
                    ByteBuffer.wrap(new byte[0]),
                    false,
                    1000
                    )
                );
            assertEquals(client.get_slice(entry.getKey(), STANDARD, sp, ConsistencyLevel.ONE),
                         entry.getValue());
        }
    }
    @Test
    public void testLoadbalance() throws Exception
    {
        final String keyspace = "TestLoadbalance";
        addKeyspace(keyspace, 1);
        List<InetAddress> hosts = controller.getHosts();
        Cassandra.Client client = controller.createClient(hosts.get(0));
        client.set_keyspace(keyspace);
        Map<ByteBuffer,List<ColumnOrSuperColumn>> rows = insertBatch(client);
        Thread.sleep(100);
        controller.nodetool("loadbalance", hosts.get(0));
        for (InetAddress host : hosts)
            controller.nodetool("cleanup", host);
        verifyBatch(client, rows);
    }
}
