import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.thrift.ColumnPath;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.utils.ByteBufferUtil;
public class ClientOnlyExample
{
    private static void testWriting() throws Exception
    {
        StorageService.instance.initClient();
        try
        {
            Thread.sleep(10000L);
        }
        catch (Exception ex)
        {
            throw new AssertionError(ex);
        }
        for (int i = 0; i < 100; i++)
        {
            RowMutation change = new RowMutation("Keyspace1", ByteBuffer.wrap(("key" + i).getBytes()));
            ColumnPath cp = new ColumnPath("Standard1").setColumn(("colb").getBytes());
            change.add(new QueryPath(cp), ByteBuffer.wrap(("value" + i).getBytes()), 0);
            StorageProxy.mutate(Arrays.asList(change), ConsistencyLevel.ONE);
            System.out.println("wrote key" + i);
        }
        System.out.println("Done writing.");
        StorageService.instance.stopClient();
    }
    private static void testReading() throws Exception
    {
        StorageService.instance.initClient();
        try
        {
            Thread.sleep(10000L);
        }
        catch (Exception ex)
        {
            throw new AssertionError(ex);
        }
        Collection<ByteBuffer> cols = new ArrayList<ByteBuffer>()
        {{
            add(ByteBufferUtil.bytes("colb"));
        }};
        for (int i = 0; i < 100; i++)
        {
            List<ReadCommand> commands = new ArrayList<ReadCommand>();
            SliceByNamesReadCommand readCommand = new SliceByNamesReadCommand("Keyspace1", ByteBuffer.wrap(("key" + i).getBytes()),
                                                                              new QueryPath("Standard1", null, null), cols);
            readCommand.setDigestQuery(false);
            commands.add(readCommand);
            List<Row> rows = StorageProxy.read(commands, ConsistencyLevel.ONE);
            assert rows.size() == 1;
            Row row = rows.get(0);
            ColumnFamily cf = row.cf;
            if (cf != null)
            {
                for (IColumn col : cf.getSortedColumns())
                {
                    System.out.println(ByteBufferUtil.string(col.name()) + ", " + ByteBufferUtil.string(col.value()));
                }
            }
            else
                System.err.println("This output indicates that nothing was read.");
        }
        StorageService.instance.stopClient();
    }
    public static void main(String args[]) throws Exception
    {
        if (args.length == 0)
            System.out.println("run with \"read\" or \"write\".");
        else if ("read".equalsIgnoreCase(args[0]))
        {
            testReading();
        }
        else if ("write".equalsIgnoreCase(args[0]))
        {
            testWriting();
        }
    }
}
