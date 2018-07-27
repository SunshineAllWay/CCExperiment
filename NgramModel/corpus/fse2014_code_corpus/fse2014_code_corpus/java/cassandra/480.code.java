package org.apache.cassandra.db;
import java.nio.ByteBuffer;
import org.junit.Test;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.apache.cassandra.Util.getBytes;
import static org.apache.cassandra.Util.concatByteArrays;
import org.apache.cassandra.db.context.CounterContext;
import org.apache.cassandra.db.marshal.LongType;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
public class SuperColumnTest
{   
    private static final CounterContext cc = new CounterContext();
    @Test
    public void testMissingSubcolumn() {
    	SuperColumn sc = new SuperColumn(ByteBufferUtil.bytes("sc1"), LongType.instance);
    	sc.addColumn(new Column(getBytes(1), ByteBufferUtil.bytes("value"), 1));
    	assertNotNull(sc.getSubColumn(getBytes(1)));
    	assertNull(sc.getSubColumn(getBytes(2)));
    }
    @Test
    public void testAddColumnIncrementCounter()
    {
        byte[] context;
    	SuperColumn sc = new SuperColumn(ByteBufferUtil.bytes("sc1"), LongType.instance);
        context = concatByteArrays(
            FBUtilities.toByteArray(1), FBUtilities.toByteArray(7L), FBUtilities.toByteArray(0L),
            FBUtilities.toByteArray(2), FBUtilities.toByteArray(5L), FBUtilities.toByteArray(7L),
            FBUtilities.toByteArray(4), FBUtilities.toByteArray(2L), FBUtilities.toByteArray(9L),
            FBUtilities.getLocalAddress().getAddress(), FBUtilities.toByteArray(3L), FBUtilities.toByteArray(3L)
            );
    	sc.addColumn(new CounterColumn(getBytes(1), ByteBuffer.wrap(cc.total(context)), 3L, context, 0L));
        context = concatByteArrays(
            FBUtilities.toByteArray(2), FBUtilities.toByteArray(3L), FBUtilities.toByteArray(4L),
            FBUtilities.toByteArray(4), FBUtilities.toByteArray(4L), FBUtilities.toByteArray(1L),
            FBUtilities.toByteArray(8), FBUtilities.toByteArray(9L), FBUtilities.toByteArray(0L),
            FBUtilities.getLocalAddress().getAddress(), FBUtilities.toByteArray(9L), FBUtilities.toByteArray(5L)
            );
    	sc.addColumn(new CounterColumn(getBytes(1), ByteBuffer.wrap(cc.total(context)), 10L, context, 0L));
        context = concatByteArrays(
            FBUtilities.toByteArray(2), FBUtilities.toByteArray(1L), FBUtilities.toByteArray(0L),
            FBUtilities.toByteArray(3), FBUtilities.toByteArray(6L), FBUtilities.toByteArray(0L),
            FBUtilities.toByteArray(7), FBUtilities.toByteArray(3L), FBUtilities.toByteArray(0L)
            );
    	sc.addColumn(new CounterColumn(getBytes(2), ByteBuffer.wrap(cc.total(context)), 9L, context, 0L));
    	assertNotNull(sc.getSubColumn(getBytes(1)));
    	assertNull(sc.getSubColumn(getBytes(3)));
    	byte[] c1 = concatByteArrays(
                FBUtilities.toByteArray(1), FBUtilities.toByteArray(7L), FBUtilities.toByteArray(0L),
                FBUtilities.toByteArray(2), FBUtilities.toByteArray(5L), FBUtilities.toByteArray(7L),
                FBUtilities.toByteArray(4), FBUtilities.toByteArray(4L), FBUtilities.toByteArray(1L),
                FBUtilities.toByteArray(8), FBUtilities.toByteArray(9L), FBUtilities.toByteArray(0L),
                FBUtilities.getLocalAddress().getAddress(), FBUtilities.toByteArray(12L), FBUtilities.toByteArray(8L)
                );
        assert 0 == FBUtilities.compareByteSubArrays(
            ((CounterColumn)sc.getSubColumn(getBytes(1))).partitionedCounter(),
            0,
            c1,
            0,
            c1.length);
        byte[] c2 = concatByteArrays(
                FBUtilities.toByteArray(2), FBUtilities.toByteArray(1L), FBUtilities.toByteArray(0L),
                FBUtilities.toByteArray(3), FBUtilities.toByteArray(6L), FBUtilities.toByteArray(0L),
                FBUtilities.toByteArray(7), FBUtilities.toByteArray(3L), FBUtilities.toByteArray(0L)
                );
        assert 0 == FBUtilities.compareByteSubArrays(
            ((CounterColumn)sc.getSubColumn(getBytes(2))).partitionedCounter(),
            0,
            c2,
            0,
            c2.length);
    	assertNotNull(sc.getSubColumn(getBytes(1)));
    	assertNotNull(sc.getSubColumn(getBytes(2)));
    	assertNull(sc.getSubColumn(getBytes(3)));
    }
}
