package org.apache.cassandra.db;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.io.sstable.SSTableReader;
import org.apache.cassandra.Util;
import static junit.framework.Assert.assertEquals;
import static org.apache.cassandra.db.TableTest.assertColumns;
import org.apache.cassandra.utils.ByteBufferUtil;
public class CompactionsPurgeTest extends CleanupHelper
{
    public static final String TABLE1 = "Keyspace1";
    public static final String TABLE2 = "Keyspace2";
    @Test
    public void testMajorCompactionPurge() throws IOException, ExecutionException, InterruptedException
    {
        CompactionManager.instance.disableAutoCompaction();
        Table table = Table.open(TABLE1);
        String cfName = "Standard1";
        ColumnFamilyStore cfs = table.getColumnFamilyStore(cfName);
        DecoratedKey key = Util.dk("key1");
        RowMutation rm;
        rm = new RowMutation(TABLE1, key.key);
        for (int i = 0; i < 10; i++)
        {
            rm.add(new QueryPath(cfName, null, ByteBuffer.wrap(String.valueOf(i).getBytes())), ByteBufferUtil.EMPTY_BYTE_BUFFER, 0);
        }
        rm.apply();
        cfs.forceBlockingFlush();
        for (int i = 0; i < 10; i++)
        {
            rm = new RowMutation(TABLE1, key.key);
            rm.delete(new QueryPath(cfName, null, ByteBuffer.wrap(String.valueOf(i).getBytes())), 1);
            rm.apply();
        }
        cfs.forceBlockingFlush();
        rm = new RowMutation(TABLE1, key.key);
        rm.add(new QueryPath(cfName, null, ByteBuffer.wrap(String.valueOf(5).getBytes())), ByteBufferUtil.EMPTY_BYTE_BUFFER, 2);
        rm.apply();
        cfs.forceBlockingFlush();
        CompactionManager.instance.submitMajor(cfs, 0, Integer.MAX_VALUE).get();
        cfs.invalidateCachedRow(key);
        ColumnFamily cf = cfs.getColumnFamily(QueryFilter.getIdentityFilter(key, new QueryPath(cfName)));
        assertColumns(cf, "5");
        assert cf.getColumn(ByteBuffer.wrap(String.valueOf(5).getBytes())) != null;
    }
    @Test
    public void testMinorCompactionPurge() throws IOException, ExecutionException, InterruptedException
    {
        CompactionManager.instance.disableAutoCompaction();
        Table table = Table.open(TABLE2);
        String cfName = "Standard1";
        ColumnFamilyStore cfs = table.getColumnFamilyStore(cfName);
        RowMutation rm;
        for (int k = 1; k <= 2; ++k) {
            DecoratedKey key = Util.dk("key" + k);
            rm = new RowMutation(TABLE2, key.key);
            for (int i = 0; i < 10; i++)
            {
                rm.add(new QueryPath(cfName, null, ByteBuffer.wrap(String.valueOf(i).getBytes())), ByteBufferUtil.EMPTY_BYTE_BUFFER, 0);
            }
            rm.apply();
            cfs.forceBlockingFlush();
            for (int i = 0; i < 10; i++)
            {
                rm = new RowMutation(TABLE2, key.key);
                rm.delete(new QueryPath(cfName, null, ByteBuffer.wrap(String.valueOf(i).getBytes())), 1);
                rm.apply();
            }
            cfs.forceBlockingFlush();
        }
        DecoratedKey key1 = Util.dk("key1");
        DecoratedKey key2 = Util.dk("key2");
        cfs.forceBlockingFlush();
        Collection<SSTableReader> sstablesIncomplete = cfs.getSSTables();
        rm = new RowMutation(TABLE2, key1.key);
        rm.add(new QueryPath(cfName, null, ByteBuffer.wrap(String.valueOf(5).getBytes())), ByteBufferUtil.EMPTY_BYTE_BUFFER, 2);
        rm.apply();
        cfs.forceBlockingFlush();
        CompactionManager.instance.doCompaction(cfs, sstablesIncomplete, Integer.MAX_VALUE);
        ColumnFamily cf = cfs.getColumnFamily(QueryFilter.getIdentityFilter(key1, new QueryPath(cfName)));
        assert cf.getColumnCount() == 10;
        cf = cfs.getColumnFamily(QueryFilter.getIdentityFilter(key2, new QueryPath(cfName)));
        assert cf == null;
    }
    @Test
    public void testCompactionPurgeOneFile() throws IOException, ExecutionException, InterruptedException
    {
        CompactionManager.instance.disableAutoCompaction();
        Table table = Table.open(TABLE1);
        String cfName = "Standard2";
        ColumnFamilyStore store = table.getColumnFamilyStore(cfName);
        DecoratedKey key = Util.dk("key1");
        RowMutation rm;
        rm = new RowMutation(TABLE1, key.key);
        for (int i = 0; i < 5; i++)
        {
            rm.add(new QueryPath(cfName, null, ByteBuffer.wrap(String.valueOf(i).getBytes())), ByteBufferUtil.EMPTY_BYTE_BUFFER, 0);
        }
        rm.apply();
        for (int i = 0; i < 5; i++)
        {
            rm = new RowMutation(TABLE1, key.key);
            rm.delete(new QueryPath(cfName, null, ByteBuffer.wrap(String.valueOf(i).getBytes())), 1);
            rm.apply();
        }
        store.forceBlockingFlush();
        assert store.getSSTables().size() == 1 : store.getSSTables(); 
        CompactionManager.instance.submitMajor(store, 0, Integer.MAX_VALUE).get();
        assert store.getSSTables().isEmpty();
        ColumnFamily cf = table.getColumnFamilyStore(cfName).getColumnFamily(QueryFilter.getIdentityFilter(key, new QueryPath(cfName)));
        assert cf == null : cf;
    }
    @Test
    public void testKeyCache50() throws IOException, ExecutionException, InterruptedException
    {
        testKeyCache("Standard3", 64);
    }
    @Test
    public void testKeyCache100() throws IOException, ExecutionException, InterruptedException
    {
        testKeyCache("Standard4", 128);
    }
    public void testKeyCache(String cfname, int expectedCacheSize) throws IOException, ExecutionException, InterruptedException
    {
        CompactionManager.instance.disableAutoCompaction();
        Table table = Table.open(TABLE1);
        String cfName = cfname;
        ColumnFamilyStore store = table.getColumnFamilyStore(cfName);
        int keyCacheSize = store.getKeyCacheCapacity();
        assert keyCacheSize == 1 : keyCacheSize;
        DecoratedKey key1 = Util.dk("key1");
        DecoratedKey key2 = Util.dk("key2");
        RowMutation rm;
        rm = new RowMutation(TABLE1, key1.key);
        rm.add(new QueryPath(cfName, null, ByteBufferUtil.bytes("1")), ByteBufferUtil.EMPTY_BYTE_BUFFER, 0);
        rm.apply();
        rm = new RowMutation(TABLE1, key2.key);
        rm.add(new QueryPath(cfName, null, ByteBufferUtil.bytes("2")), ByteBufferUtil.EMPTY_BYTE_BUFFER, 0);
        rm.apply();
        rm = new RowMutation(TABLE1, key1.key);
        rm.delete(new QueryPath(cfName, null, ByteBufferUtil.bytes("1")), 1);
        rm.apply();
        rm = new RowMutation(TABLE1, key2.key);
        rm.delete(new QueryPath(cfName, null, ByteBufferUtil.bytes("2")), 1);
        rm.apply();
        store.forceBlockingFlush();
        keyCacheSize = store.getKeyCacheCapacity();
        assert keyCacheSize == expectedCacheSize : keyCacheSize;
        CompactionManager.instance.submitMajor(store, 0, Integer.MAX_VALUE).get();
        keyCacheSize = store.getKeyCacheCapacity();
        assert keyCacheSize == 1 : keyCacheSize;
    }
}
