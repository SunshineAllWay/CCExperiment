package org.apache.cassandra.db;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.Util;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.db.columniterator.IdentityQueryFilter;
import org.apache.cassandra.db.filter.IFilter;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.locator.TokenMetadata;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.thrift.IndexClause;
import org.apache.cassandra.thrift.IndexExpression;
import org.apache.cassandra.thrift.IndexOperator;
import org.apache.cassandra.utils.ByteBufferUtil;
public class CleanupTest extends CleanupHelper
{
    public static final int LOOPS = 200;
    public static final String TABLE1 = "Keyspace1";
    public static final String CF1 = "Indexed1";
    public static final String CF2 = "Standard1";
    public static final ByteBuffer COLUMN = ByteBuffer.wrap("birthdate".getBytes());
    public static final ByteBuffer VALUE = ByteBuffer.allocate(8);
    static
    {
        VALUE.putLong(20101229);
        VALUE.flip();
    }
    @Test
    public void testCleanup() throws IOException, ExecutionException, InterruptedException, ConfigurationException
    {
        StorageService.instance.initServer();
        Table table = Table.open(TABLE1);
        ColumnFamilyStore cfs = table.getColumnFamilyStore(CF2);
        List<Row> rows;
        fillCF(cfs, LOOPS);
        rows = cfs.getRangeSlice(null, Util.range("", ""), 1000, new IdentityQueryFilter());
        assertEquals(LOOPS, rows.size());
        CompactionManager.instance.performCleanup(cfs);
        rows = cfs.getRangeSlice(null, Util.range("", ""), 1000, new IdentityQueryFilter());
        assertEquals(LOOPS, rows.size());
    }
    @Test
    public void testCleanupWithIndexes() throws IOException, ExecutionException, InterruptedException
    {
        Table table = Table.open(TABLE1);
        ColumnFamilyStore cfs = table.getColumnFamilyStore(CF1);
        assertEquals(cfs.getIndexedColumns().iterator().next(), COLUMN);
        List<Row> rows;
        fillCF(cfs, LOOPS);
        rows = cfs.getRangeSlice(null, Util.range("", ""), 1000, new IdentityQueryFilter());
        assertEquals(LOOPS, rows.size());
        ColumnFamilyStore cfi = cfs.getIndexedColumnFamilyStore(COLUMN);
        assertTrue(cfi.isIndexBuilt());
        IndexExpression expr = new IndexExpression(COLUMN, IndexOperator.EQ, VALUE);
        IndexClause clause = new IndexClause(Arrays.asList(expr), ByteBufferUtil.EMPTY_BYTE_BUFFER, Integer.MAX_VALUE);
        IFilter filter = new IdentityQueryFilter();
        IPartitioner p = StorageService.getPartitioner();
        Range range = new Range(p.getMinimumToken(), p.getMinimumToken());
        rows = table.getColumnFamilyStore(CF1).scan(clause, range, filter);
        assertEquals(LOOPS, rows.size());
        TokenMetadata tmd = StorageService.instance.getTokenMetadata();
        tmd.clearUnsafe();
        assert StorageService.instance.getLocalRanges(TABLE1).isEmpty();
        CompactionManager.instance.performCleanup(cfs);
        rows = cfs.getRangeSlice(null, Util.range("", ""), 1000, new IdentityQueryFilter());
        assertEquals(0, rows.size());
        assert cfs.getSSTables().isEmpty();
        rows = cfs.scan(clause, range, filter);
        assertEquals(0, rows.size());
    }
    protected void fillCF(ColumnFamilyStore cfs, int rowsPerSSTable) throws ExecutionException, InterruptedException, IOException
    {
        CompactionManager.instance.disableAutoCompaction();
        for (int i = 0; i < rowsPerSSTable; i++)
        {
            String key = String.valueOf(i);
            RowMutation rm;
            rm = new RowMutation(TABLE1, ByteBufferUtil.bytes(key));
            rm.add(new QueryPath(cfs.getColumnFamilyName(), null, COLUMN), VALUE, System.currentTimeMillis());
            rm.applyUnsafe();
        }
        cfs.forceBlockingFlush();
    }
}
