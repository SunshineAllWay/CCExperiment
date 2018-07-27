package org.apache.cassandra.db;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import static junit.framework.Assert.assertNull;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.Util;
import org.apache.cassandra.utils.ByteBufferUtil;
public class RemoveColumnFamilyWithFlush2Test extends CleanupHelper
{
    @Test
    public void testRemoveColumnFamilyWithFlush2() throws IOException, ExecutionException, InterruptedException
    {
        Table table = Table.open("Keyspace1");
        ColumnFamilyStore store = table.getColumnFamilyStore("Standard1");
        RowMutation rm;
        DecoratedKey dk = Util.dk("key1");
        rm = new RowMutation("Keyspace1", dk.key);
        rm.add(new QueryPath("Standard1", null, ByteBufferUtil.bytes("Column1")), ByteBufferUtil.bytes("asdf"), 0);
        rm.apply();
        rm = new RowMutation("Keyspace1", dk.key);
        rm.delete(new QueryPath("Standard1"), 1);
        rm.apply();
        store.forceBlockingFlush();
        ColumnFamily retrieved = store.getColumnFamily(QueryFilter.getIdentityFilter(dk, new QueryPath("Standard1", null, ByteBufferUtil.bytes("Column1"))));
        assert retrieved.isMarkedForDelete();
        assertNull(retrieved.getColumn(ByteBufferUtil.bytes("Column1")));
        assertNull(Util.cloneAndRemoveDeleted(retrieved, Integer.MAX_VALUE));
    }
}
