package org.apache.cassandra.db;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import org.apache.cassandra.Util;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.filter.QueryPath;
import static org.apache.cassandra.Util.addMutation;
import static org.apache.cassandra.Util.getBytes;
import org.apache.cassandra.CleanupHelper;
import static junit.framework.Assert.assertNotNull;
import org.apache.cassandra.utils.ByteBufferUtil;
public class RemoveSuperColumnTest extends CleanupHelper
{
    @Test
    public void testRemoveSuperColumn() throws IOException, ExecutionException, InterruptedException
    {
        ColumnFamilyStore store = Table.open("Keyspace1").getColumnFamilyStore("Super1");
        RowMutation rm;
        DecoratedKey dk = Util.dk("key1");
        rm = new RowMutation("Keyspace1", dk.key);
        addMutation(rm, "Super1", "SC1", 1, "val1", 0);
        rm.apply();
        store.forceBlockingFlush();
        rm = new RowMutation("Keyspace1", dk.key);
        rm.delete(new QueryPath("Super1", ByteBufferUtil.bytes("SC1")), 1);
        rm.apply();
        validateRemoveTwoSources(dk);
        store.forceBlockingFlush();
        validateRemoveTwoSources(dk);
        CompactionManager.instance.performMajor(store);
        assertEquals(1, store.getSSTables().size());
        validateRemoveCompacted(dk);
    }
    @Test
    public void testRemoveDeletedSubColumn() throws IOException, ExecutionException, InterruptedException
    {
        ColumnFamilyStore store = Table.open("Keyspace1").getColumnFamilyStore("Super3");
        RowMutation rm;
        DecoratedKey dk = Util.dk("key1");
        rm = new RowMutation("Keyspace1", dk.key);
        addMutation(rm, "Super3", "SC1", 1, "val1", 0);
        addMutation(rm, "Super3", "SC1", 2, "val1", 0);
        rm.apply();
        store.forceBlockingFlush();
        rm = new RowMutation("Keyspace1", dk.key);
        rm.delete(new QueryPath("Super3", ByteBufferUtil.bytes("SC1"), Util.getBytes(1)), 1);
        rm.apply();
        validateRemoveSubColumn(dk);
        store.forceBlockingFlush();
        validateRemoveSubColumn(dk);
    }
    private void validateRemoveSubColumn(DecoratedKey dk) throws IOException
    {
        ColumnFamilyStore store = Table.open("Keyspace1").getColumnFamilyStore("Super3");
        ColumnFamily cf = store.getColumnFamily(QueryFilter.getNamesFilter(dk, new QueryPath("Super3", ByteBufferUtil.bytes("SC1")), Util.getBytes(1)));
        assertNull(Util.cloneAndRemoveDeleted(cf, Integer.MAX_VALUE));
        cf = store.getColumnFamily(QueryFilter.getNamesFilter(dk, new QueryPath("Super3", ByteBufferUtil.bytes("SC1")), Util.getBytes(2)));
        assertNotNull(Util.cloneAndRemoveDeleted(cf, Integer.MAX_VALUE));
    }
    private void validateRemoveTwoSources(DecoratedKey dk) throws IOException
    {
        ColumnFamilyStore store = Table.open("Keyspace1").getColumnFamilyStore("Super1");
        ColumnFamily cf = store.getColumnFamily(QueryFilter.getNamesFilter(dk, new QueryPath("Super1"), ByteBufferUtil.bytes("SC1")));
        assert cf.getSortedColumns().iterator().next().getMarkedForDeleteAt() == 1 : cf;
        assert cf.getSortedColumns().iterator().next().getSubColumns().size() == 0 : cf;
        assertNull(Util.cloneAndRemoveDeleted(cf, Integer.MAX_VALUE));
        cf = store.getColumnFamily(QueryFilter.getNamesFilter(dk, new QueryPath("Super1"), ByteBufferUtil.bytes("SC1")));
        assertNull(Util.cloneAndRemoveDeleted(cf, Integer.MAX_VALUE));
        cf = store.getColumnFamily(QueryFilter.getIdentityFilter(dk, new QueryPath("Super1")));
        assertNull(Util.cloneAndRemoveDeleted(cf, Integer.MAX_VALUE));
        assertNull(Util.cloneAndRemoveDeleted(store.getColumnFamily(QueryFilter.getIdentityFilter(dk, new QueryPath("Super1"))), Integer.MAX_VALUE));
    }
    private void validateRemoveCompacted(DecoratedKey dk) throws IOException
    {
        ColumnFamilyStore store = Table.open("Keyspace1").getColumnFamilyStore("Super1");
        ColumnFamily resolved = store.getColumnFamily(QueryFilter.getNamesFilter(dk, new QueryPath("Super1"), ByteBufferUtil.bytes("SC1")));
        assert resolved.getSortedColumns().iterator().next().getMarkedForDeleteAt() == 1;
        Collection<IColumn> subColumns = resolved.getSortedColumns().iterator().next().getSubColumns();
        assert subColumns.size() == 0;
    }
    @Test
    public void testRemoveSuperColumnWithNewData() throws IOException, ExecutionException, InterruptedException
    {
        ColumnFamilyStore store = Table.open("Keyspace1").getColumnFamilyStore("Super2");
        RowMutation rm;
        DecoratedKey dk = Util.dk("key1");
        rm = new RowMutation("Keyspace1", dk.key);
        addMutation(rm, "Super2", "SC1", 1, "val1", 0);
        rm.apply();
        store.forceBlockingFlush();
        rm = new RowMutation("Keyspace1", dk.key);
        rm.delete(new QueryPath("Super2", ByteBufferUtil.bytes("SC1")), 1);
        rm.apply();
        rm = new RowMutation("Keyspace1", dk.key);
        addMutation(rm, "Super2", "SC1", 2, "val2", 2);
        rm.apply();
        validateRemoveWithNewData(dk);
        store.forceBlockingFlush();
        validateRemoveWithNewData(dk);
        CompactionManager.instance.performMajor(store);
        assertEquals(1, store.getSSTables().size());
        validateRemoveWithNewData(dk);
    }
    private void validateRemoveWithNewData(DecoratedKey dk) throws IOException
    {
        ColumnFamilyStore store = Table.open("Keyspace1").getColumnFamilyStore("Super2");
        ColumnFamily cf = store.getColumnFamily(QueryFilter.getNamesFilter(dk, new QueryPath("Super2", ByteBufferUtil.bytes("SC1")), getBytes(2)));
        Collection<IColumn> subColumns = cf.getSortedColumns().iterator().next().getSubColumns();
        assert subColumns.size() == 1;
        assert subColumns.iterator().next().timestamp() == 2;
    }
    @Test
    public void testRemoveSuperColumnResurrection() throws IOException, ExecutionException, InterruptedException
    {
        ColumnFamilyStore store = Table.open("Keyspace1").getColumnFamilyStore("Super2");
        RowMutation rm;
        DecoratedKey key = Util.dk("keyC");
        rm = new RowMutation("Keyspace1", key.key);
        addMutation(rm, "Super2", "SC1", 1, "val1", 0);
        rm.apply();
        rm = new RowMutation("Keyspace1", key.key);
        rm.delete(new QueryPath("Super2", ByteBufferUtil.bytes("SC1")), 1);
        rm.apply();
        assertNull(Util.cloneAndRemoveDeleted(store.getColumnFamily(QueryFilter.getNamesFilter(key, new QueryPath("Super2"), ByteBufferUtil.bytes("SC1"))), Integer.MAX_VALUE));
        rm = new RowMutation("Keyspace1", key.key);
        addMutation(rm, "Super2", "SC1", 1, "val2", 2);
        rm.apply();
        ColumnFamily cf = store.getColumnFamily(QueryFilter.getNamesFilter(key, new QueryPath("Super2"), ByteBufferUtil.bytes("SC1")));
        cf = Util.cloneAndRemoveDeleted(cf, Integer.MAX_VALUE);
        Collection<IColumn> subColumns = cf.getSortedColumns().iterator().next().getSubColumns();
        assert subColumns.size() == 1;
        assert subColumns.iterator().next().timestamp() == 2;
    }
}
