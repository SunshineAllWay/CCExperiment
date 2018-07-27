package org.apache.cassandra.io.sstable;
import static org.junit.Assert.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.filter.IFilter;
import org.apache.cassandra.db.columniterator.IdentityQueryFilter;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.streaming.OperationType;
import org.apache.cassandra.thrift.IndexClause;
import org.apache.cassandra.thrift.IndexExpression;
import org.apache.cassandra.thrift.IndexOperator;
import org.junit.Test;
import org.apache.cassandra.utils.ByteBufferUtil;
public class SSTableWriterTest extends CleanupHelper {
    @Test
    public void testRecoverAndOpen() throws IOException, ExecutionException, InterruptedException
    {
        RowMutation rm;
        rm = new RowMutation("Keyspace1", ByteBufferUtil.bytes("k1"));
        rm.add(new QueryPath("Indexed1", null, ByteBufferUtil.bytes("birthdate")), ByteBufferUtil.bytes(1L), 0);
        rm.apply();
        ColumnFamily cf = ColumnFamily.create("Keyspace1", "Indexed1");        
        cf.addColumn(new Column(ByteBufferUtil.bytes("birthdate"), ByteBufferUtil.bytes(1L), 0));
        cf.addColumn(new Column(ByteBufferUtil.bytes("anydate"), ByteBufferUtil.bytes(1L), 0));
        Map<ByteBuffer, ByteBuffer> entries = new HashMap<ByteBuffer, ByteBuffer>();
        DataOutputBuffer buffer = new DataOutputBuffer();
        ColumnFamily.serializer().serializeWithIndexes(cf, buffer);
        entries.put(ByteBufferUtil.bytes("k2"), ByteBuffer.wrap(Arrays.copyOf(buffer.getData(), buffer.getLength())));        
        cf.clear();
        cf.addColumn(new Column(ByteBufferUtil.bytes("anydate"), ByteBufferUtil.bytes(1L), 0));
        buffer = new DataOutputBuffer();
        ColumnFamily.serializer().serializeWithIndexes(cf, buffer);               
        entries.put(ByteBufferUtil.bytes("k3"), ByteBuffer.wrap(Arrays.copyOf(buffer.getData(), buffer.getLength())));
        SSTableReader orig = SSTableUtils.prepare().cf("Indexed1").writeRaw(entries);        
        FileUtils.deleteWithConfirm(orig.descriptor.filenameFor(Component.PRIMARY_INDEX));
        FileUtils.deleteWithConfirm(orig.descriptor.filenameFor(Component.FILTER));
        SSTableReader sstr = CompactionManager.instance.submitSSTableBuild(orig.descriptor, OperationType.AES).get();
        assert sstr != null;
        ColumnFamilyStore cfs = Table.open("Keyspace1").getColumnFamilyStore("Indexed1");
        cfs.addSSTable(sstr);
        cfs.buildSecondaryIndexes(cfs.getSSTables(), cfs.getIndexedColumns());
        IndexExpression expr = new IndexExpression(ByteBufferUtil.bytes("birthdate"), IndexOperator.EQ, ByteBufferUtil.bytes(1L));
        IndexClause clause = new IndexClause(Arrays.asList(expr), ByteBufferUtil.EMPTY_BYTE_BUFFER, 100);
        IFilter filter = new IdentityQueryFilter();
        IPartitioner p = StorageService.getPartitioner();
        Range range = new Range(p.getMinimumToken(), p.getMinimumToken());
        List<Row> rows = cfs.scan(clause, range, filter);
        assertEquals("IndexExpression should return two rows on recoverAndOpen", 2, rows.size());
        assertTrue("First result should be 'k1'",ByteBufferUtil.bytes("k1").equals(rows.get(0).key.key));
    }
}
