package org.apache.cassandra.db;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import static junit.framework.Assert.assertNull;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.filter.QueryPath;
import static org.apache.cassandra.Util.getBytes;
import org.apache.cassandra.Util;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.utils.ByteBufferUtil;
public class RemoveSubColumnTest extends CleanupHelper
{
    @Test
    public void testRemoveSubColumn() throws IOException, ExecutionException, InterruptedException
    {
        Table table = Table.open("Keyspace1");
        ColumnFamilyStore store = table.getColumnFamilyStore("Super1");
        RowMutation rm;
        DecoratedKey dk = Util.dk("key1");
        rm = new RowMutation("Keyspace1", dk.key);
        Util.addMutation(rm, "Super1", "SC1", 1, "asdf", 0);
        rm.apply();
        store.forceBlockingFlush();
        rm = new RowMutation("Keyspace1", dk.key);
        rm.delete(new QueryPath("Super1", ByteBufferUtil.bytes("SC1"), getBytes(1)), 1);
        rm.apply();
        ColumnFamily retrieved = store.getColumnFamily(QueryFilter.getIdentityFilter(dk, new QueryPath("Super1", ByteBufferUtil.bytes("SC1"))));
        assert retrieved.getColumn(ByteBufferUtil.bytes("SC1")).getSubColumn(getBytes(1)).isMarkedForDelete();
        assertNull(Util.cloneAndRemoveDeleted(retrieved, Integer.MAX_VALUE));
    }
}
