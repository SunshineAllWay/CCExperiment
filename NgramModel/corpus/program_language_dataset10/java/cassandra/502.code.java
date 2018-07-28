package org.apache.cassandra.io.sstable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.io.util.FileDataInput;
import org.apache.cassandra.io.util.MmappedSegmentedFile;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.Pair;
import org.apache.cassandra.Util;
import static org.junit.Assert.assertEquals;
public class SSTableReaderTest extends CleanupHelper
{
    static Token t(int i)
    {
        return StorageService.getPartitioner().getToken(ByteBuffer.wrap(String.valueOf(i).getBytes()));
    }
    @Test
    public void testGetPositionsForRanges() throws IOException, ExecutionException, InterruptedException
    {
        Table table = Table.open("Keyspace1");
        ColumnFamilyStore store = table.getColumnFamilyStore("Standard2");
        CompactionManager.instance.disableAutoCompaction();
        for (int j = 0; j < 10; j++)
        {
            ByteBuffer key = ByteBuffer.wrap(String.valueOf(j).getBytes());
            RowMutation rm = new RowMutation("Keyspace1", key);
            rm.add(new QueryPath("Standard2", null, ByteBufferUtil.bytes("0")), ByteBufferUtil.EMPTY_BYTE_BUFFER, j);
            rm.apply();
        }
        store.forceBlockingFlush();
        CompactionManager.instance.performMajor(store);
        List<Range> ranges = new ArrayList<Range>();
        ranges.add(new Range(t(0), t(1)));
        ranges.add(new Range(t(2), t(4)));
        ranges.add(new Range(t(6), StorageService.getPartitioner().getMinimumToken()));
        ranges.add(new Range(t(9), t(91)));
        SSTableReader sstable = store.getSSTables().iterator().next();
        long previous = -1;
        for (Pair<Long,Long> section : sstable.getPositionsForRanges(ranges))
        {
            assert previous <= section.left : previous + " ! < " + section.left;
            assert section.left < section.right : section.left + " ! < " + section.right;
            previous = section.right;
        }
    }
    @Test
    public void testSpannedIndexPositions() throws IOException, ExecutionException, InterruptedException
    {
        MmappedSegmentedFile.MAX_SEGMENT_SIZE = 40; 
        Table table = Table.open("Keyspace1");
        ColumnFamilyStore store = table.getColumnFamilyStore("Standard1");
        CompactionManager.instance.disableAutoCompaction();
        for (int j = 0; j < 100; j += 2)
        {
            ByteBuffer key = ByteBuffer.wrap(String.valueOf(j).getBytes());
            RowMutation rm = new RowMutation("Keyspace1", key);
            rm.add(new QueryPath("Standard1", null, ByteBufferUtil.bytes("0")), ByteBufferUtil.EMPTY_BYTE_BUFFER, j);
            rm.apply();
        }
        store.forceBlockingFlush();
        CompactionManager.instance.performMajor(store);
        SSTableReader sstable = store.getSSTables().iterator().next();
        for (int j = 0; j < 100; j += 2)
        {
            DecoratedKey dk = Util.dk(String.valueOf(j));
            FileDataInput file = sstable.getFileDataInput(dk, DatabaseDescriptor.getIndexedReadBufferSizeInKB() * 1024);
            DecoratedKey keyInDisk = SSTableReader.decodeKey(sstable.partitioner,
                                                             sstable.descriptor,
                                                             ByteBufferUtil.readWithShortLength(file));
            assert keyInDisk.equals(dk) : String.format("%s != %s in %s", keyInDisk, dk, file.getPath());
        }
        for (int j = 1; j < 110; j += 2)
        {
            DecoratedKey dk = Util.dk(String.valueOf(j));
            assert sstable.getPosition(dk, SSTableReader.Operator.EQ) == -1;
        }
    }
    @Test
    public void testPersistentStatistics() throws IOException, ExecutionException, InterruptedException
    {
        Table table = Table.open("Keyspace1");
        ColumnFamilyStore store = table.getColumnFamilyStore("Standard1");
        for (int j = 0; j < 100; j += 2)
        {
            ByteBuffer key = ByteBuffer.wrap(String.valueOf(j).getBytes());
            RowMutation rm = new RowMutation("Keyspace1", key);
            rm.add(new QueryPath("Standard1", null, ByteBufferUtil.bytes("0")), ByteBufferUtil.EMPTY_BYTE_BUFFER, j);
            rm.apply();
        }
        store.forceBlockingFlush();
        assert store.getMaxRowSize() != 0;
    }
}
