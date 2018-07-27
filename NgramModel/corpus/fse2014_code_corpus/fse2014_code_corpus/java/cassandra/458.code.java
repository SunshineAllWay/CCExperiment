package org.apache.cassandra.db;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.TreeMap;
import org.apache.cassandra.SchemaLoader;
import org.junit.Test;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.apache.cassandra.db.filter.QueryPath;
import static org.apache.cassandra.Util.column;
import org.apache.cassandra.utils.ByteBufferUtil;
public class ColumnFamilyTest extends SchemaLoader
{
    @Test
    public void testSingleColumn() throws IOException
    {
        ColumnFamily cf;
        cf = ColumnFamily.create("Keyspace1", "Standard1");
        cf.addColumn(column("C", "v", 1));
        DataOutputBuffer bufOut = new DataOutputBuffer();
        ColumnFamily.serializer().serialize(cf, bufOut);
        ByteArrayInputStream bufIn = new ByteArrayInputStream(bufOut.getData(), 0, bufOut.getLength());
        cf = ColumnFamily.serializer().deserialize(new DataInputStream(bufIn));
        assert cf != null;
        assert cf.metadata().cfName.equals("Standard1");
        assert cf.getSortedColumns().size() == 1;
    }
    @Test
    public void testManyColumns() throws IOException
    {
        ColumnFamily cf;
        TreeMap<String, String> map = new TreeMap<String, String>();
        for (int i = 100; i < 1000; ++i)
        {
            map.put(Integer.toString(i), "Avinash Lakshman is a good man: " + i);
        }
        cf = ColumnFamily.create("Keyspace1", "Standard1");
        DataOutputBuffer bufOut = new DataOutputBuffer();
        for (String cName : map.navigableKeySet())
        {
            cf.addColumn(column(cName, map.get(cName), 314));
        }
        ColumnFamily.serializer().serialize(cf, bufOut);
        ByteArrayInputStream bufIn = new ByteArrayInputStream(bufOut.getData(), 0, bufOut.getLength());
        cf = ColumnFamily.serializer().deserialize(new DataInputStream(bufIn));
        for (String cName : map.navigableKeySet())
        {
            ByteBuffer val = cf.getColumn(ByteBuffer.wrap(cName.getBytes())).value();
            assert new String(val.array(),val.position(),val.remaining()).equals(map.get(cName));
        }
        assert cf.getColumnNames().size() == map.size();
    }
    @Test
    public void testGetColumnCount()
    {
        ColumnFamily cf = ColumnFamily.create("Keyspace1", "Standard1");
        cf.addColumn(column("col1", "", 1));
        cf.addColumn(column("col2", "", 2));
        cf.addColumn(column("col1", "", 3));
        assert 2 == cf.getColumnCount();
        assert 2 == cf.getSortedColumns().size();
    }
    @Test
    public void testTimestamp()
    {
        ColumnFamily cf = ColumnFamily.create("Keyspace1", "Standard1");
        cf.addColumn(column("col1", "val1", 2));
        cf.addColumn(column("col1", "val2", 2)); 
        cf.addColumn(column("col1", "val3", 1)); 
        assert ByteBufferUtil.bytes("val2").equals(cf.getColumn(ByteBufferUtil.bytes("col1")).value());
    }
    @Test
    public void testMergeAndAdd()
    {
        ColumnFamily cf_new = ColumnFamily.create("Keyspace1", "Standard1");
        ColumnFamily cf_old = ColumnFamily.create("Keyspace1", "Standard1");
        ColumnFamily cf_result = ColumnFamily.create("Keyspace1", "Standard1");
        ByteBuffer val = ByteBufferUtil.bytes("sample value");
        ByteBuffer val2 = ByteBufferUtil.bytes("x value ");
        cf_new.addColumn(QueryPath.column(ByteBufferUtil.bytes("col1")), val, 3);
        cf_new.addColumn(QueryPath.column(ByteBufferUtil.bytes("col2")), val, 4);
        cf_old.addColumn(QueryPath.column(ByteBufferUtil.bytes("col2")), val2, 1);
        cf_old.addColumn(QueryPath.column(ByteBufferUtil.bytes("col3")), val2, 2);
        cf_result.addAll(cf_new);
        cf_result.addAll(cf_old);
        assert 3 == cf_result.getColumnCount() : "Count is " + cf_new.getColumnCount();
        assert val.equals(cf_result.getColumn(ByteBufferUtil.bytes("col2")).value());
        cf_result.addTombstone(ByteBufferUtil.bytes("col1"), 0, 3);
        assert cf_result.getColumn(ByteBufferUtil.bytes("col1")).isMarkedForDelete();
        cf_result.addColumn(QueryPath.column(ByteBufferUtil.bytes("col1")), val2, 3);
        assert cf_result.getColumn(ByteBufferUtil.bytes("col1")).isMarkedForDelete();
        cf_result.addColumn(QueryPath.column(ByteBufferUtil.bytes("col3")), val, 2);
        assert cf_result.getColumn(ByteBufferUtil.bytes("col3")).value().equals(val2);
        cf_result.addColumn(QueryPath.column(ByteBufferUtil.bytes("col3")), ByteBufferUtil.bytes("z"), 2);
        assert cf_result.getColumn(ByteBufferUtil.bytes("col3")).value().equals(ByteBufferUtil.bytes("z"));
    }
}
