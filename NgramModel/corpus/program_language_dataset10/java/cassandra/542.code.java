package org.apache.cassandra.utils;
import static org.apache.cassandra.utils.MerkleTree.RECOMMENDED_DEPTH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.apache.cassandra.dht.BigIntegerToken;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.RandomPartitioner;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.utils.MerkleTree.Hashable;
import org.apache.cassandra.utils.MerkleTree.RowHash;
import org.apache.cassandra.utils.MerkleTree.TreeRange;
import org.apache.cassandra.utils.MerkleTree.TreeRangeIterator;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.AbstractIterator;
public class MerkleTreeTest
{
    private static final Logger logger = LoggerFactory.getLogger(MerkleTreeTest.class);
    public static byte[] DUMMY = "blah".getBytes();
    public static BigInteger TOKEN_SCALE = new BigInteger("8");
    protected IPartitioner partitioner;
    protected MerkleTree mt;
    @Before
    public void clear()
    {
        TOKEN_SCALE = new BigInteger("8");
        partitioner = new RandomPartitioner();
        mt = new MerkleTree(partitioner, RECOMMENDED_DEPTH, Integer.MAX_VALUE);
    }
    public static void assertHashEquals(final byte[] left, final byte[] right)
    {
        assertHashEquals("", left, right);
    }
    public static void assertHashEquals(String message, final byte[] left, final byte[] right)
    {
        String lstring = left == null ? "null" : FBUtilities.bytesToHex(left);
        String rstring = right == null ? "null" : FBUtilities.bytesToHex(right);
        assertEquals(message, lstring, rstring);
    }
    public static BigIntegerToken tok(int i)
    {
        if (i == -1)
            return new BigIntegerToken(new BigInteger("-1"));
        BigInteger md5_max = new BigInteger("2").pow(127);
        BigInteger bint = md5_max.divide(TOKEN_SCALE).multiply(new BigInteger(""+i));
        return new BigIntegerToken(bint);
    }
    @Test
    public void testSplit()
    {
        mt.split(tok(4));
        mt.split(tok(6));
        mt.split(tok(7));
        assertEquals(4, mt.size());
        assertEquals(new Range(tok(7), tok(-1)), mt.get(tok(-1)));
        assertEquals(new Range(tok(-1), tok(4)), mt.get(tok(3)));
        assertEquals(new Range(tok(-1), tok(4)), mt.get(tok(4)));
        assertEquals(new Range(tok(4), tok(6)), mt.get(tok(6)));
        assertEquals(new Range(tok(6), tok(7)), mt.get(tok(7)));
        assertEquals((byte)1, mt.get(tok(4)).depth);
        assertEquals((byte)2, mt.get(tok(6)).depth);
        assertEquals((byte)3, mt.get(tok(7)).depth);
        assertEquals((byte)3, mt.get(tok(-1)).depth);
        try
        {
            mt.split(tok(-1));
            fail("Shouldn't be able to split outside the initial range.");
        }
        catch (AssertionError e)
        {
        }
    }
    @Test
    public void testSplitLimitDepth()
    {
        mt = new MerkleTree(partitioner, (byte)2, Integer.MAX_VALUE);
        assertTrue(mt.split(tok(4)));
        assertTrue(mt.split(tok(2)));
        assertEquals(3, mt.size());
        assertFalse(mt.split(tok(1)));
        assertEquals(3, mt.size());
        assertEquals(new Range(tok(4), tok(-1)), mt.get(tok(-1)));
        assertEquals(new Range(tok(-1), tok(2)), mt.get(tok(2)));
        assertEquals(new Range(tok(2), tok(4)), mt.get(tok(4)));
    }
    @Test
    public void testSplitLimitSize()
    {
        mt = new MerkleTree(partitioner, RECOMMENDED_DEPTH, 2);
        assertTrue(mt.split(tok(4)));
        assertEquals(2, mt.size());
        assertFalse(mt.split(tok(2)));
        assertEquals(2, mt.size());
        assertEquals(new Range(tok(4), tok(-1)), mt.get(tok(-1)));
        assertEquals(new Range(tok(-1), tok(4)), mt.get(tok(4)));
    }
    @Test
    public void testCompact()
    {
        mt.split(tok(4));
        mt.split(tok(2)); 
        mt.split(tok(6));
        mt.split(tok(1));
        mt.split(tok(3));
        mt.split(tok(5));
        mt.split(tok(7));
        mt.compact(tok(1));
        mt.compact(tok(5));
        assertEquals(6, mt.size());
        assertEquals(new Range(tok(-1), tok(2)), mt.get(tok(2)));
        assertEquals(new Range(tok(2), tok(3)), mt.get(tok(3)));
        assertEquals(new Range(tok(3), tok(4)), mt.get(tok(4)));
        assertEquals(new Range(tok(4), tok(6)), mt.get(tok(5)));
        assertEquals(new Range(tok(6), tok(7)), mt.get(tok(7)));
        assertEquals(new Range(tok(7), tok(-1)), mt.get(tok(-1)));
        for (int i : new int[]{2,6}){ assertEquals((byte)2, mt.get(tok(i)).depth); }
        for (int i : new int[]{3,4,7,-1}){ assertEquals((byte)3, mt.get(tok(i)).depth); }
        mt.compact(tok(3));
        mt.compact(tok(7));
        assertEquals(4, mt.size());
        assertEquals(new Range(tok(-1), tok(2)), mt.get(tok(2)));
        assertEquals(new Range(tok(2), tok(4)), mt.get(tok(4)));
        assertEquals(new Range(tok(4), tok(6)), mt.get(tok(5)));
        assertEquals(new Range(tok(6), tok(-1)), mt.get(tok(-1)));
        for (int i : new int[]{2,4,5,-1}){ assertEquals((byte)2, mt.get(tok(i)).depth); }
        mt.compact(tok(2));
        assertEquals(3, mt.size());
        assertEquals(new Range(tok(-1), tok(4)), mt.get(tok(2)));
        assertEquals(new Range(tok(4), tok(6)), mt.get(tok(6)));
        assertEquals(new Range(tok(6), tok(-1)), mt.get(tok(-1)));
        mt.compact(tok(6));
        assertEquals(2, mt.size());
        assertEquals(new Range(tok(-1), tok(4)), mt.get(tok(2)));
        assertEquals(new Range(tok(4), tok(-1)), mt.get(tok(6)));
        assertEquals((byte)1, mt.get(tok(2)).depth);
        assertEquals((byte)1, mt.get(tok(6)).depth);
        mt.compact(tok(4));
        assertEquals(1, mt.size());
        assertEquals(new Range(tok(-1), tok(-1)), mt.get(tok(-1)));
        assertEquals((byte)0, mt.get(tok(-1)).depth);
    }
    @Test
    public void testCompactHash()
    {
        byte[] val = DUMMY;
        byte[] valXval = hashed(val, 1, 1);
        mt.split(tok(4));
        mt.get(tok(4)).hash(val);
        mt.get(tok(-1)).hash(val);
        mt.compact(tok(4));
        assertHashEquals(valXval, mt.get(tok(-1)).hash());
    }
    @Test
    public void testInvalids()
    {
        Iterator<TreeRange> ranges;
        ranges = mt.invalids(new Range(tok(-1), tok(-1)));
        assertEquals(new Range(tok(-1), tok(-1)), ranges.next());
        assertFalse(ranges.hasNext());
        mt.split(tok(4));
        mt.split(tok(2));
        mt.split(tok(6));
        mt.split(tok(3));
        mt.split(tok(5));
        ranges = mt.invalids(new Range(tok(-1), tok(-1)));
        assertEquals(new Range(tok(-1), tok(2)), ranges.next());
        assertEquals(new Range(tok(2), tok(3)), ranges.next());
        assertEquals(new Range(tok(3), tok(4)), ranges.next());
        assertEquals(new Range(tok(4), tok(5)), ranges.next());
        assertEquals(new Range(tok(5), tok(6)), ranges.next());
        assertEquals(new Range(tok(6), tok(-1)), ranges.next());
        assertFalse(ranges.hasNext());
        mt.get(tok(2)).hash("non-null!".getBytes());
        mt.get(tok(4)).hash("non-null!".getBytes());
        mt.get(tok(5)).hash("non-null!".getBytes());
        mt.get(tok(-1)).hash("non-null!".getBytes());
        ranges = mt.invalids(new Range(tok(-1), tok(-1)));
        assertEquals(new Range(tok(2), tok(3)), ranges.next());
        assertEquals(new Range(tok(5), tok(6)), ranges.next());
        assertFalse(ranges.hasNext());
        ranges = mt.invalids(new Range(tok(-1), tok(6)));
        assertEquals(new Range(tok(2), tok(3)), ranges.next());
        assertEquals(new Range(tok(5), tok(6)), ranges.next());
        assertFalse(ranges.hasNext());
        ranges = mt.invalids(new Range(tok(2), tok(-1)));
        assertEquals(new Range(tok(2), tok(3)), ranges.next());
        assertEquals(new Range(tok(5), tok(6)), ranges.next());
        assertFalse(ranges.hasNext());
    }
    @Test
    public void testHashFull()
    {
        byte[] val = DUMMY;
        Range range = new Range(tok(-1), tok(-1));
        assertNull(mt.hash(range));
        mt.get(tok(-1)).hash(val);
        assertHashEquals(val, mt.hash(range));
    }
    @Test
    public void testHashPartial()
    {
        byte[] val = DUMMY;
        byte[] leftval = hashed(val, 1, 1);
        byte[] partialval = hashed(val, 1);
        Range left = new Range(tok(-1), tok(4));
        Range partial = new Range(tok(2), tok(4));
        Range right = new Range(tok(4), tok(-1));
        Range linvalid = new Range(tok(1), tok(4));
        Range rinvalid = new Range(tok(4), tok(6));
        mt.split(tok(4));
        mt.split(tok(2));
        assertNull(mt.hash(left));
        assertNull(mt.hash(partial));
        assertNull(mt.hash(right));
        assertNull(mt.hash(linvalid));
        assertNull(mt.hash(rinvalid));
        mt.get(tok(2)).hash(val);
        mt.get(tok(4)).hash(val);
        mt.get(tok(-1)).hash(val);
        assertHashEquals(leftval, mt.hash(left));
        assertHashEquals(partialval, mt.hash(partial));
        assertHashEquals(val, mt.hash(right));
        assertNull(mt.hash(linvalid));
        assertNull(mt.hash(rinvalid));
    }
    @Test
    public void testHashInner()
    {
        byte[] val = DUMMY;
        byte[] lchildval = hashed(val, 3, 3, 2);
        byte[] rchildval = hashed(val, 2, 2);
        byte[] fullval = hashed(val, 3, 3, 2, 2, 2);
        Range full = new Range(tok(-1), tok(-1));
        Range lchild = new Range(tok(-1), tok(4));
        Range rchild = new Range(tok(4), tok(-1));
        Range invalid = new Range(tok(1), tok(-1));
        mt.split(tok(4));
        mt.split(tok(2));
        mt.split(tok(6));
        mt.split(tok(1));
        assertNull(mt.hash(full));
        assertNull(mt.hash(lchild));
        assertNull(mt.hash(rchild));
        assertNull(mt.hash(invalid));
        mt.get(tok(1)).hash(val);
        mt.get(tok(2)).hash(val);
        mt.get(tok(4)).hash(val);
        mt.get(tok(6)).hash(val);
        mt.get(tok(-1)).hash(val);
        assertHashEquals(fullval, mt.hash(full));
        assertHashEquals(lchildval, mt.hash(lchild));
        assertHashEquals(rchildval, mt.hash(rchild));
        assertNull(mt.hash(invalid));
    }
    @Test
    public void testHashDegenerate()
    {
        TOKEN_SCALE = new BigInteger("32");
        byte[] val = DUMMY;
        byte[] childfullval = hashed(val, 5, 5, 4);
        byte[] fullval = hashed(val, 5, 5, 4, 3, 2, 1);
        Range childfull = new Range(tok(-1), tok(4));
        Range full = new Range(tok(-1), tok(-1));
        Range invalid = new Range(tok(4), tok(-1));
        mt = new MerkleTree(partitioner, RECOMMENDED_DEPTH, Integer.MAX_VALUE);
        mt.split(tok(16));
        mt.split(tok(8));
        mt.split(tok(4));
        mt.split(tok(2));
        mt.split(tok(1));
        assertNull(mt.hash(full));
        assertNull(mt.hash(childfull));
        assertNull(mt.hash(invalid));
        mt.get(tok(1)).hash(val);
        mt.get(tok(2)).hash(val);
        mt.get(tok(4)).hash(val);
        mt.get(tok(8)).hash(val);
        mt.get(tok(16)).hash(val);
        mt.get(tok(-1)).hash(val);
        assertHashEquals(fullval, mt.hash(full));
        assertHashEquals(childfullval, mt.hash(childfull));
        assertNull(mt.hash(invalid));
    }
    @Test
    public void testHashRandom()
    {
        int max = 1000000;
        TOKEN_SCALE = new BigInteger("" + max);
        mt = new MerkleTree(partitioner, RECOMMENDED_DEPTH, 32);
        Random random = new Random();
        while (true)
        {
            if (!mt.split(tok(random.nextInt(max))))
                break;
        }
        TreeRangeIterator ranges = mt.invalids(new Range(tok(-1), tok(-1)));
        for (TreeRange range : ranges)
            range.addHash(new RowHash(range.right, new byte[0]));
        assert null != mt.hash(new Range(tok(-1), tok(-1))) :
            "Could not hash tree " + mt;
    }
    @Test
    public void testValidateTree()
    {
        TOKEN_SCALE = new BigInteger("16"); 
        Range full = new Range(tok(-1), tok(-1));
        Iterator<TreeRange> ranges;
        MerkleTree mt2 = new MerkleTree(partitioner, RECOMMENDED_DEPTH, Integer.MAX_VALUE);
        mt.split(tok(8));
        mt.split(tok(4));
        mt.split(tok(12));
        mt.split(tok(6));
        mt.split(tok(10));
        ranges = mt.invalids(full);
        ranges.next().addAll(new HIterator(2, 4)); 
        ranges.next().addAll(new HIterator(6)); 
        ranges.next().addAll(new HIterator(8)); 
        ranges.next().addAll(new HIterator( new int[0])); 
        ranges.next().addAll(new HIterator(12)); 
        ranges.next().addAll(new HIterator(14, -1)); 
        mt2.split(tok(8));
        mt2.split(tok(4));
        mt2.split(tok(12));
        mt2.split(tok(2));
        mt2.split(tok(10));
        mt2.split(tok(9));
        mt2.split(tok(11));
        ranges = mt2.invalids(full);
        ranges.next().addAll(new HIterator(2)); 
        ranges.next().addAll(new HIterator(4)); 
        ranges.next().addAll(new HIterator(6, 8)); 
        ranges.next().addAll(new HIterator( new int[0])); 
        ranges.next().addAll(new HIterator( new int[0])); 
        ranges.next().addAll(new HIterator( new int[0])); 
        ranges.next().addAll(new HIterator(12)); 
        ranges.next().addAll(new HIterator(14, -1)); 
        byte[] mthash = mt.hash(full);
        byte[] mt2hash = mt2.hash(full);
        assertHashEquals("Tree hashes did not match: " + mt + " && " + mt2, mthash, mt2hash);
    }
    @Test
    public void testSerialization() throws Exception
    {
        Range full = new Range(tok(-1), tok(-1));
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        mt.maxsize(256);
        mt.init();
        for (TreeRange range : mt.invalids(full))
            range.addAll(new HIterator(range.right));
        byte[] initialhash = mt.hash(full);
        oout.writeObject(mt);
        oout.close();
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        ObjectInputStream oin = new ObjectInputStream(bin);
        MerkleTree restored = (MerkleTree)oin.readObject();
        restored.partitioner(partitioner);
        assertHashEquals(initialhash, restored.hash(full));
    }
    @Test
    public void testDifference()
    {
        Range full = new Range(tok(-1), tok(-1));
        int maxsize = 16;
        mt.maxsize(maxsize);
        MerkleTree mt2 = new MerkleTree(partitioner, RECOMMENDED_DEPTH, maxsize);
        mt.init();
        mt2.init();
        TreeRange leftmost = null;
        TreeRange middle = null;
        TreeRange rightmost = null;
        Iterator<TreeRange> ranges = mt.invalids(full);
        leftmost = ranges.next();
        rightmost = null;
        while (ranges.hasNext())
            rightmost = ranges.next();
        mt.compact(leftmost.right);
        leftmost = mt.get(leftmost.right); 
        mt.split(rightmost.right);
        middle = mt.get(rightmost.left);
        middle.hash("arbitrary!".getBytes());
        byte depth = middle.depth;
        for (TreeRange range : mt.invalids(full))
            range.addAll(new HIterator(range.right));
        for (TreeRange range : mt2.invalids(full))
            range.addAll(new HIterator(range.right));
        List<TreeRange> diffs = MerkleTree.difference(mt, mt2);
        assertEquals(diffs + " contains wrong number of differences:", 2, diffs.size());
        assertTrue(diffs.contains(leftmost));
        assertTrue(diffs.contains(new Range(middle.left, rightmost.right)));
    }
    byte[] hashed(byte[] val, Integer... depths)
    {
        ArrayDeque<Integer> dstack = new ArrayDeque<Integer>();
        ArrayDeque<byte[]> hstack = new ArrayDeque<byte[]>();
        Iterator<Integer> depthiter = Arrays.asList(depths).iterator();
        if (depthiter.hasNext())
        {
            dstack.push(depthiter.next());
            hstack.push(val);
        }
        while (depthiter.hasNext())
        {
            Integer depth = depthiter.next();
            byte[] hash = val;
            while (dstack.peek() == depth)
            {
                hash = Hashable.binaryHash(hstack.pop(), hash);
                depth = dstack.pop()-1;
            }
            dstack.push(depth);
            hstack.push(hash);
        }
        assert hstack.size() == 1;
        return hstack.pop();
    }
    static class HIterator extends AbstractIterator<RowHash>
    {
        private Iterator<Token> tokens;
        public HIterator(int... tokens)
        {
            List<Token> tlist = new LinkedList<Token>();
            for (int token : tokens)
                tlist.add(tok(token));
            this.tokens = tlist.iterator();
        }
        public HIterator(Token... tokens)
        {
            this.tokens = Arrays.asList(tokens).iterator();
        }
        @Override
        public RowHash computeNext()
        {
            if (tokens.hasNext())
                return new RowHash(tokens.next(), DUMMY);
            return endOfData();
        }
    }
}
