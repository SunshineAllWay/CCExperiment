package org.apache.cassandra;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import org.apache.thrift.TException;
import org.apache.cassandra.thrift.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
public abstract class TestBase
{
    protected static CassandraServiceController controller =
        CassandraServiceController.getInstance();
    protected static void addKeyspace(String name, int rf) throws Exception
    {
        List<CfDef> cfDefList = new LinkedList<CfDef>();
        CfDef standard1 = new CfDef(name, "Standard1");
        standard1.setComparator_type("BytesType");
        standard1.setKey_cache_size(10000);
        standard1.setRow_cache_size(1000);
        standard1.setRow_cache_save_period_in_seconds(0);
        standard1.setKey_cache_save_period_in_seconds(3600);
        standard1.setMemtable_flush_after_mins(59);
        standard1.setMemtable_throughput_in_mb(255);
        standard1.setMemtable_operations_in_millions(0.29);
        cfDefList.add(standard1);
        List<InetAddress> hosts = controller.getHosts();
        Cassandra.Client client = controller.createClient(hosts.get(0));
        client.system_add_keyspace(
            new KsDef(
                name,
                "org.apache.cassandra.locator.SimpleStrategy",
                rf,
                cfDefList));
        for (InetAddress host : hosts)
        {
            try
            {
                client = controller.createClient(host);
                poll:
                while (true)
                {
                    List<KsDef> ksDefList = client.describe_keyspaces();
                    for (KsDef ks : ksDefList)
                    {
                        if (ks.name.equals(name))
                            break poll;
                    }
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        break poll;
                    }
                }
            }
            catch (TException te)
            {
                continue;
            }
        }
    }
    @BeforeClass
    public static void setUp() throws Exception
    {
        controller.ensureClusterRunning();
    }
    protected static String createTemporaryKey()
    {
        return String.format("test.key.%d", System.currentTimeMillis());
    }
}
