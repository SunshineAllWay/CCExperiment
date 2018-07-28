package org.apache.cassandra.utils;
import java.io.Serializable;
import java.util.*;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.PeekingIterator;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
public class MerkleTree implements Serializable
{
    private static final long serialVersionUID = 2L;
    public static final byte RECOMMENDED_DEPTH = Byte.MAX_VALUE - 1;
    public static final int CONSISTENT = 0;
    public static final int FULLY_INCONSISTENT = 1;
    public static final int PARTIALLY_INCONSISTENT = 2;
    public final byte hashdepth;
    private transient IPartitioner partitioner;
    private long maxsize;
    private long size;
    private Hashable root;
    public MerkleTree(IPartitioner partitioner, byte hashdepth, long maxsize)
    {
        assert hashdepth < Byte.MAX_VALUE;
        this.partitioner = partitioner;
        this.hashdepth = hashdepth;
        this.maxsize = maxsize;
        size = 1;
        root = new Leaf(null);
    }
    static byte inc(byte in)
    {
        assert in < Byte.MAX_VALUE;
        return (byte)(in + 1);
    }
    public void init()
    {
        byte sizedepth = (byte)(Math.log10(maxsize) / Math.log10(2));
        byte depth = (byte)Math.min(sizedepth, hashdepth);
        Token mintoken = partitioner.getMinimumToken();
        root = initHelper(mintoken, mintoken, (byte)0, depth);
        size = (long)Math.pow(2, depth);
    }
    private Hashable initHelper(Token left, Token right, byte depth, byte max)
    {
        if (depth == max)
            return new Leaf();
        Token midpoint = partitioner.midpoint(left, right);
        Hashable lchild = initHelper(left, midpoint, inc(depth), max);
        Hashable rchild = initHelper(midpoint, right, inc(depth), max);
        return new Inner(midpoint, lchild, rchild);
    }
    Hashable root()
    {
        return root;
    }
    public IPartitioner partitioner()
    {
        return partitioner;
    }
    public long size()
    {
        return size;
    }
    public long maxsize()
    {
        return maxsize;
    }
    public void maxsize(long maxsize)
    {
        this.maxsize = maxsize;
    }
    public void partitioner(IPartitioner partitioner)
    {
        this.partitioner = partitioner;
    }
    public static List<TreeRange> difference(MerkleTree ltree, MerkleTree rtree)
    {
        List<TreeRange> diff = new ArrayList<TreeRange>();
        Token mintoken = ltree.partitioner.getMinimumToken();
        TreeRange active = new TreeRange(null, mintoken, mintoken, (byte)0, null);
        byte[] lhash = ltree.hash(active);
        byte[] rhash = rtree.hash(active);
        if (lhash != null && rhash != null && !Arrays.equals(lhash, rhash))
        {
            if (FULLY_INCONSISTENT == differenceHelper(ltree, rtree, diff, active))
                diff.add(active);
        }
        else if (lhash == null || rhash == null)
            diff.add(active);
        return diff;
    }
    static int differenceHelper(MerkleTree ltree, MerkleTree rtree, List<TreeRange> diff, TreeRange active)
    {
        Token midpoint = ltree.partitioner().midpoint(active.left, active.right);
        TreeRange left = new TreeRange(null, active.left, midpoint, inc(active.depth), null);
        TreeRange right = new TreeRange(null, midpoint, active.right, inc(active.depth), null);
        byte[] lhash;
        byte[] rhash;
        lhash = ltree.hash(left);
        rhash = rtree.hash(left);
        int ldiff = CONSISTENT;
        boolean lreso = lhash != null && rhash != null;
        if (lreso && !Arrays.equals(lhash, rhash))
            ldiff = differenceHelper(ltree, rtree, diff, left);
        else if (!lreso)
            ldiff = FULLY_INCONSISTENT;
        lhash = ltree.hash(right);
        rhash = rtree.hash(right);
        int rdiff = CONSISTENT;
        boolean rreso = lhash != null && rhash != null;
        if (rreso && !Arrays.equals(lhash, rhash))
            rdiff = differenceHelper(ltree, rtree, diff, right);
        else if (!rreso)
            rdiff = FULLY_INCONSISTENT;
        if (ldiff == FULLY_INCONSISTENT && rdiff == FULLY_INCONSISTENT)
        {
            return FULLY_INCONSISTENT;
        }
        else if (ldiff == FULLY_INCONSISTENT)
        {
            diff.add(left);
            return PARTIALLY_INCONSISTENT;
        }
        else if (rdiff == FULLY_INCONSISTENT)
        {
            diff.add(right);
            return PARTIALLY_INCONSISTENT;
        }
        return PARTIALLY_INCONSISTENT;
    }
    TreeRange get(Token t)
    {
        Token mintoken = partitioner.getMinimumToken();
        return getHelper(root, mintoken, mintoken, (byte)0, t);
    }
    TreeRange getHelper(Hashable hashable, Token pleft, Token pright, byte depth, Token t)
    {
        if (hashable instanceof Leaf)
        {
            return new TreeRange(this, pleft, pright, depth, hashable);
        }
        Inner node = (Inner)hashable;
        if (Range.contains(pleft, node.token, t))
            return getHelper(node.lchild, pleft, node.token, inc(depth), t);
        return getHelper(node.rchild, node.token, pright, inc(depth), t);
    }
    public void invalidate(Token t)
    {
        invalidateHelper(root, partitioner.getMinimumToken(), t);
    }
    private void invalidateHelper(Hashable hashable, Token pleft, Token t)
    {
        hashable.hash(null);
        if (hashable instanceof Leaf)
            return;
        Inner node = (Inner)hashable;
        if (Range.contains(pleft, node.token, t))
            invalidateHelper(node.lchild, pleft, t);
        else
            invalidateHelper(node.rchild, node.token, t);
    }
    public byte[] hash(Range range)
    {
        Token mintoken = partitioner.getMinimumToken();
        try
        {
            return hashHelper(root, new Range(mintoken, mintoken), range);
        }
        catch (StopRecursion e)
        {
            return null;
        }
    }
    private byte[] hashHelper(Hashable hashable, Range active, Range range) throws StopRecursion
    {
        if (hashable instanceof Leaf)
        {
            if (!range.contains(active))
                throw new StopRecursion.BadRange();
            return hashable.hash();
        }
        Inner node = (Inner)hashable;
        Range leftactive = new Range(active.left, node.token);
        Range rightactive = new Range(node.token, active.right);
        if (range.contains(active))
        {
            if (node.hash() != null)
                return node.hash();
            byte[] lhash = hashHelper(node.lchild(), leftactive, range);
            byte[] rhash = hashHelper(node.rchild(), rightactive, range);
            node.hash(lhash, rhash);
            return node.hash();
        } 
        if (leftactive.contains(range))
            return hashHelper(node.lchild, leftactive, range);
        else if (rightactive.contains(range))
            return hashHelper(node.rchild, rightactive, range);
        else
            throw new StopRecursion.BadRange();
    }
    public boolean split(Token t)
    {
        if (!(size < maxsize))
            return false;
        Token mintoken = partitioner.getMinimumToken();
        try
        {
            root = splitHelper(root, mintoken, mintoken, (byte)0, t);
        }
        catch (StopRecursion.TooDeep e)
        {
            return false;
        }
        return true;
    }
    private Hashable splitHelper(Hashable hashable, Token pleft, Token pright, byte depth, Token t) throws StopRecursion.TooDeep
    {
        if (depth >= hashdepth)
            throw new StopRecursion.TooDeep();
        if (hashable instanceof Leaf)
        {
            size++;
            Token midpoint = partitioner.midpoint(pleft, pright);
            return new Inner(midpoint, new Leaf(), new Leaf());
        }
        Inner node = (Inner)hashable;
        if (Range.contains(pleft, node.token, t))
            node.lchild(splitHelper(node.lchild, pleft, node.token, inc(depth), t));
        else
            node.rchild(splitHelper(node.rchild, node.token, pright, inc(depth), t));
        return node;
    }
    public void compact(Token t)
    {
        root = compactHelper(root, t);
    }
    private Hashable compactHelper(Hashable hashable, Token t)
    {
        assert !(hashable instanceof Leaf);
        Inner node = (Inner)hashable;
        int comp = t.compareTo(node.token);
        if (comp == 0)
        {
            assert node.lchild() instanceof Leaf && node.rchild() instanceof Leaf :
                "Can only compact a subrange evenly split by the given token!";
            size--;
            return new Leaf(node.lchild().hash(), node.rchild().hash());
        }
        else if (comp < 0)
            node.lchild(compactHelper(node.lchild(), t));
        else
            node.rchild(compactHelper(node.rchild(), t));
        return node;
    }
    public TreeRangeIterator invalids(Range range)
    {
        return new TreeRangeIterator(this, range);
    }
    @Override
    public String toString()
    {
        StringBuilder buff = new StringBuilder();
        buff.append("#<MerkleTree root=");
        root.toString(buff, 8);
        buff.append(">");
        return buff.toString();
    }
    public static class TreeRange extends Range
    {
        public static final long serialVersionUID = 1L;
        private final MerkleTree tree;
        public final byte depth;
        private final Hashable hashable;
        TreeRange(MerkleTree tree, Token left, Token right, byte depth, Hashable hashable)
        {
            super(left, right);
            this.tree = tree;
            this.depth = depth;
            this.hashable = hashable;
        }
        public void hash(byte[] hash)
        {
            assert tree != null : "Not intended for modification!";
            hashable.hash(hash);
        }
        public byte[] hash()
        {
            return hashable.hash();
        }
        public void addHash(RowHash entry)
        {
            assert tree != null : "Not intended for modification!";
            assert hashable instanceof Leaf;
            hashable.addHash(entry.hash);
        }
        public void addAll(Iterator<RowHash> entries)
        {
            while (entries.hasNext())
                addHash(entries.next());
        }
        @Override
        public String toString()
        {
            StringBuilder buff = new StringBuilder("#<TreeRange ");
            buff.append(super.toString()).append(" depth=").append(depth);
            return buff.append(">").toString();
        }
    }
    public static class TreeRangeIterator extends AbstractIterator<TreeRange> implements Iterable<TreeRange>, PeekingIterator<TreeRange>
    {
        private final ArrayDeque<TreeRange> tovisit;
        private final Range range;
        private final MerkleTree tree;
        TreeRangeIterator(MerkleTree tree, Range range)
        {
            Token mintoken = tree.partitioner().getMinimumToken();
            tovisit = new ArrayDeque<TreeRange>();
            tovisit.add(new TreeRange(tree, mintoken, mintoken, (byte)0, tree.root));
            this.tree = tree;
            this.range = range;
        }
        @Override
        public TreeRange computeNext()
        {
            while (!tovisit.isEmpty())
            {
                TreeRange active = tovisit.pop();
                if (active.hashable.hash() != null)
                    continue;
                if (active.hashable instanceof Leaf)
                    return active;
                Inner node = (Inner)active.hashable;
                TreeRange left = new TreeRange(tree, active.left, node.token, inc(active.depth), node.lchild);
                TreeRange right = new TreeRange(tree, node.token, active.right, inc(active.depth), node.rchild);
                if (right.intersects(range))
                    tovisit.push(right);
                if (left.intersects(range))
                    tovisit.push(left);
            }
            return endOfData();
        }
        public Iterator<TreeRange> iterator()
        {
            return this;
        }
    }
    static class Inner extends Hashable
    {
        public static final long serialVersionUID = 1L;
        public final Token token;
        private Hashable lchild;
        private Hashable rchild;
        public Inner(Token token, Hashable lchild, Hashable rchild)
        {
            super(null);
            this.token = token;
            this.lchild = lchild;
            this.rchild = rchild;
        }
        public Hashable lchild()
        {
            return lchild;
        }
        public Hashable rchild()
        {
            return rchild;
        }
        public void lchild(Hashable child)
        {
            lchild = child;
        }
        public void rchild(Hashable child)
        {
            rchild = child;
        }
        @Override
        public void toString(StringBuilder buff, int maxdepth)
        {
            buff.append("#<").append(getClass().getSimpleName());
            buff.append(" ").append(token);
            buff.append(" hash=").append(Hashable.toString(hash()));
            buff.append(" children=[");
            if (maxdepth < 1)
            {
                buff.append("#");
            }
            else
            {
                if (lchild == null)
                    buff.append("null");
                else
                    lchild.toString(buff, maxdepth-1);
                buff.append(" ");
                if (rchild == null)
                    buff.append("null");
                else
                    rchild.toString(buff, maxdepth-1);
            }
            buff.append("]>");
        }
        @Override
        public String toString()
        {
            StringBuilder buff = new StringBuilder();
            toString(buff, 1);
            return buff.toString();
        }
    }
    static class Leaf extends Hashable
    {
        public static final long serialVersionUID = 1L;
        public Leaf()
        {
            super(null);
        }
        public Leaf(byte[] hash)
        {
            super(hash);
        }
        public Leaf(byte[] lefthash, byte[] righthash)
        {
            super(Hashable.binaryHash(lefthash, righthash));
        }
        @Override
        public void toString(StringBuilder buff, int maxdepth)
        {
            buff.append(toString());
        }
        @Override
        public String toString()
        {
            return "#<Leaf " + Hashable.toString(hash()) + ">";
        }
    }
    public static class RowHash
    {
        public final Token token;
        public final byte[] hash;
        public RowHash(Token token, byte[] hash)
        {
            this.token = token;      
            this.hash  = hash;
        }
        @Override
        public String toString()
        {
            return "#<RowHash " + token + " " + Hashable.toString(hash) + ">";
        }
    }
    static abstract class Hashable implements Serializable
    {
        private static final long serialVersionUID = 1L;
        protected byte[] hash;
        protected Hashable(byte[] hash)
        {
            this.hash = hash;
        }
        public byte[] hash()
        {
            return hash;
        }
        void hash(byte[] hash)
        {
            this.hash = hash;
        }
        void hash(byte[] lefthash, byte[] righthash)
        {
            hash = binaryHash(lefthash, righthash);
        }
        void addHash(byte[] righthash)
        {
            if (hash == null)
                hash = righthash;
            else
                hash = binaryHash(hash, righthash);
        }
        static byte[] binaryHash(final byte[] left, final byte[] right)
        {
            return FBUtilities.xor(left, right);
        }
        public abstract void toString(StringBuilder buff, int maxdepth);
        public static String toString(byte[] hash)
        {
            if (hash == null)
                return "null";
            return "[" + FBUtilities.bytesToHex(hash) + "]";
        }
    }
    static abstract class StopRecursion extends Exception
    {
        static class BadRange extends StopRecursion
        {
            public BadRange(){ super(); }
        }
        static class InvalidHash extends StopRecursion
        {
            public InvalidHash(){ super(); }
        }
        static class TooDeep extends StopRecursion
        {
            public TooDeep(){ super(); }
        }
    }
}
