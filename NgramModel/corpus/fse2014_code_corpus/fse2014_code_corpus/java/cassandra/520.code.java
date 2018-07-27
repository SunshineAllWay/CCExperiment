package org.apache.cassandra.service;
import java.io.IOException;
import java.nio.ByteBuffer;
import com.google.common.base.Charsets;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.thrift.*;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
public class EmbeddedCassandraServiceTest extends CleanupHelper
{
    private static EmbeddedCassandraService cassandra;
    @BeforeClass
    public static void setup() throws TTransportException, IOException, InterruptedException, ConfigurationException
    {
        cassandra = new EmbeddedCassandraService();
        cassandra.init();
        Thread t = new Thread(cassandra);
        t.setDaemon(true);
        t.start();
    }
    @Test
    public void testEmbeddedCassandraService() throws AuthenticationException, AuthorizationException,
    InvalidRequestException, UnavailableException, TimedOutException, TException, NotFoundException
    {
        Cassandra.Client client = getClient();
        client.set_keyspace("Keyspace1");
        ByteBuffer key_user_id = ByteBufferUtil.bytes("1");
        long timestamp = System.currentTimeMillis();
        ColumnPath cp = new ColumnPath("Standard1");
        ColumnParent par = new ColumnParent("Standard1");
        cp.column = ByteBufferUtil.bytes("name");
        client.insert(key_user_id, par, new Column(ByteBufferUtil.bytes("name"),
                ByteBuffer.wrap( "Ran".getBytes(Charsets.UTF_8)), timestamp), ConsistencyLevel.ONE);
        ColumnOrSuperColumn got = client.get(key_user_id, cp, ConsistencyLevel.ONE);
        assertNotNull("Got a null ColumnOrSuperColumn", got);
        assertEquals("Ran", new String(got.getColumn().getValue(), Charsets.UTF_8));
    }
    private Cassandra.Client getClient() throws TTransportException
    {
        TTransport tr = new TFramedTransport(new TSocket("localhost", DatabaseDescriptor.getRpcPort()));
        TProtocol proto = new TBinaryProtocol(tr);
        Cassandra.Client client = new Cassandra.Client(proto);
        tr.open();
        return client;
    }
}
