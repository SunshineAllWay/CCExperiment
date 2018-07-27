package org.apache.cassandra.dht;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.*;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.commons.lang.ObjectUtils;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
public class Range extends AbstractBounds implements Comparable<Range>, Serializable
{
    public static final long serialVersionUID = 1L;
    public Range(Token left, Token right)
    {
        this(left, right, StorageService.getPartitioner());
    }
    public Range(Token left, Token right, IPartitioner partitioner)
    {
        super(left, right, partitioner);
    }
    public static boolean contains(Token left, Token right, Token bi)
    {
        if (isWrapAround(left, right))
        {
            if (bi.compareTo(left) > 0)
                return true;
            else
                return right.compareTo(bi) >= 0;
        }
        else
        {
            return ( compare(bi,left) > 0 && compare(right,bi) >= 0);
        }
    }
    public boolean contains(Range that)
    {
        if (this.left.equals(this.right))
        {
            return true;
        }
        boolean thiswraps = isWrapAround(left, right);
        boolean thatwraps = isWrapAround(that.left, that.right);
        if (thiswraps == thatwraps)
        {
            return compare(left,that.left) <= 0 && compare(that.right,right) <= 0;
        }
        else if (thiswraps)
        {
            return compare(left,that.left) <= 0 || compare(that.right,right) <= 0;
        }
        else
        {
            return false;
        }
    }
    public boolean contains(Token bi)
    {
        return contains(left, right, bi);
    }
    public boolean intersects(Range that)
    {
        return intersectionWith(that).size() > 0;
    }
    public static Set<Range> rangeSet(Range ... ranges)
    {
        return Collections.unmodifiableSet(new HashSet<Range>(Arrays.asList(ranges)));
    }
    public Set<Range> intersectionWith(Range that)
    {
        if (that.contains(this))
            return rangeSet(this);
        if (this.contains(that))
            return rangeSet(that);
        boolean thiswraps = isWrapAround(left, right);
        boolean thatwraps = isWrapAround(that.left, that.right);
        if (!thiswraps && !thatwraps)
        {
            if (!(left.compareTo(that.right) < 0 && that.left.compareTo(right) < 0))
                return Collections.emptySet();
            return rangeSet(new Range((Token)ObjectUtils.max(this.left, that.left),
                                      (Token)ObjectUtils.min(this.right, that.right)));
        }
        if (thiswraps && thatwraps)
        {
            assert !this.left.equals(that.left);
            return this.left.compareTo(that.left) < 0
                   ? intersectionBothWrapping(this, that)
                   : intersectionBothWrapping(that, this);
        }
        if (thiswraps && !thatwraps)
            return intersectionOneWrapping(this, that);
        assert (!thiswraps && thatwraps);
        return intersectionOneWrapping(that, this);
    }
    private static Set<Range> intersectionBothWrapping(Range first, Range that)
    {
        Set<Range> intersection = new HashSet<Range>(2);
        if (that.right.compareTo(first.left) > 0)
            intersection.add(new Range(first.left, that.right));
        intersection.add(new Range(that.left, first.right));
        return Collections.unmodifiableSet(intersection);
    }
    private static Set<Range> intersectionOneWrapping(Range wrapping, Range other)
    {
        Set<Range> intersection = new HashSet<Range>(2);
        if (other.contains(wrapping.right))
            intersection.add(new Range(other.left, wrapping.right));
        if (other.contains(wrapping.left) && wrapping.left.compareTo(other.right) < 0)
            intersection.add(new Range(wrapping.left, other.right));
        return Collections.unmodifiableSet(intersection);
    }
    public AbstractBounds createFrom(Token token)
    {
        if (token.equals(left))
            return null;
        return new Range(left, token);
    }
    public List<AbstractBounds> unwrap()
    {
        if (!isWrapAround() || right.equals(partitioner.getMinimumToken()))
            return (List)Arrays.asList(this);
        List<AbstractBounds> unwrapped = new ArrayList<AbstractBounds>(2);
        unwrapped.add(new Range(left, partitioner.getMinimumToken()));
        unwrapped.add(new Range(partitioner.getMinimumToken(), right));
        return unwrapped;
    }
    public static boolean isWrapAround(Token left, Token right)
    {
       return compare(left,right) >= 0;           
    }
    public static int compare(Token left, Token right)
    {
        ByteBuffer l,r;
        if (left.token instanceof byte[])
        {
            l  = ByteBuffer.wrap((byte[]) left.token);
        }
        else if (left.token instanceof ByteBuffer)
        {
            l  = (ByteBuffer) left.token;
        }
        else
        {
            return left.compareTo(right);
        }
        if (right.token instanceof byte[])
        {
            r  = ByteBuffer.wrap((byte[]) right.token);
        }
        else
        {
            r  = (ByteBuffer) right.token;
        }
        return ByteBufferUtil.compareUnsigned(l, r);
     }
    public int compareTo(Range rhs)
    {
        if ( isWrapAround(left, right) )
            return -1;
        if ( isWrapAround(rhs.left, rhs.right) )
            return 1;
        return compare(right,rhs.right);
    }
    public static boolean isTokenInRanges(Token token, Iterable<Range> ranges)
    {
        assert ranges != null;
        for (Range range : ranges)
        {
            if (range.contains(token))
            {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Range))
            return false;
        Range rhs = (Range)o;
        return compare(left,rhs.left) == 0 && compare(right,rhs.right) == 0;
    }
    public String toString()
    {
        return "(" + left + "," + right + "]";
    }
    public boolean isWrapAround()
    {
        return isWrapAround(left, right);
    }
}
