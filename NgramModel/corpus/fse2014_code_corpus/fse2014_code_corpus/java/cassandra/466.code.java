package org.apache.cassandra.db;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.Set;
import java.util.HashSet;
import org.apache.cassandra.Util;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.utils.ByteBufferUtil;
public class OneCompactionTest extends CleanupHelper
{
    private void testCompaction(String columnFamilyName, int insertsPerTable) throws IOException, ExecutionException, InterruptedException
    {
        CompactionManager.instance.disableAutoCompaction();
        Table table = Table.open("Keyspace1");
        ColumnFamilyStore store = table.getColumnFamilyStore(columnFamilyName);
        Set<DecoratedKey> inserted = new HashSet<DecoratedKey>();
        for (int j = 0; j < insertsPerTable; j++) {
            DecoratedKey key = Util.dk(String.valueOf(j));
            RowMutation rm = new RowMutation("Keyspace1", key.key);
            rm.add(new QueryPath(columnFamilyName, null, ByteBufferUtil.bytes("0")), ByteBufferUtil.EMPTY_BYTE_BUFFER, j);
            rm.apply();
            inserted.add(key);
            store.forceBlockingFlush();
            assertEquals(inserted.size(), Util.getRangeSlice(store).size());
        }
        CompactionManager.instance.performMajor(store);
        assertEquals(1, store.getSSTables().size());
    }
    @Test
    public void testCompaction1() throws IOException, ExecutionException, InterruptedException
    {
        testCompaction("Standard1", 1);
    }
    @Test
    public void testCompaction2() throws IOException, ExecutionException, InterruptedException
    {
        testCompaction("Standard2", 2);
    }
}
