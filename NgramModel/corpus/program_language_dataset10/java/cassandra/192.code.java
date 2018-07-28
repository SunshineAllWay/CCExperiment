package org.apache.cassandra.db.context;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.cassandra.db.DBConstants;
import org.apache.cassandra.utils.FBUtilities;
public class CounterContext implements IContext
{
    private static final int idLength;
    private static final byte[] localId;
    private static final int clockLength = DBConstants.longSize_;
    private static final int countLength = DBConstants.longSize_;
    private static final int stepLength; 
    private static class LazyHolder
    {
        private static final CounterContext counterContext = new CounterContext();
    }
    static
    {
        localId  = FBUtilities.getLocalAddress().getAddress();
        idLength   = localId.length;
        stepLength = idLength + clockLength + countLength;
    }
    public static CounterContext instance()
    {
        return LazyHolder.counterContext;
    }
    public byte[] create()
    {
        return new byte[0];
    }
    protected static void writeElement(byte[] context, byte[] id, long clock, long count)
    {
        writeElementAtStepOffset(context, 0, id, clock, count);
    }
    protected static void writeElementAtStepOffset(byte[] context, int stepOffset, byte[] id, long clock, long count)
    {
        int offset = stepOffset * stepLength;
        System.arraycopy(id, 0, context, offset, idLength);
        FBUtilities.copyIntoBytes(context, offset + idLength, clock);
        FBUtilities.copyIntoBytes(context, offset + idLength + clockLength, count);
    }
    public byte[] insertElementAtStepOffset(byte[] context, int stepOffset, byte[] id, long clock, long count)
    {
        int offset = stepOffset * stepLength;
        byte[] newContext = new byte[context.length + stepLength];
        System.arraycopy(context, 0, newContext, 0, offset);
        writeElementAtStepOffset(newContext, stepOffset, id, clock, count);
        System.arraycopy(context, offset, newContext, offset + stepLength, context.length - offset);
        return newContext;
    }
    public byte[] update(byte[] context, InetAddress node, long delta)
    {
        byte[] nodeId = node.getAddress();
        int idCount = context.length / stepLength;
        for (int stepOffset = 0; stepOffset < idCount; ++stepOffset)
        {
            int offset = stepOffset * stepLength;
            int cmp = FBUtilities.compareByteSubArrays(nodeId, 0, context, offset, idLength);
            if (cmp == 0)
            {
                long clock = FBUtilities.byteArrayToLong(context, offset + idLength);
                long count = FBUtilities.byteArrayToLong(context, offset + idLength + clockLength);
                writeElementAtStepOffset(context, stepOffset, nodeId, clock + 1L, count + delta);
                return context;
            }
            if (cmp < 0)
            {
                return insertElementAtStepOffset(context, stepOffset, nodeId, 1L, delta);
            }
        }
        return insertElementAtStepOffset(context, idCount, nodeId, 1L, delta);
    }
    public ContextRelationship diff(byte[] left, byte[] right)
    {
        ContextRelationship relationship = ContextRelationship.EQUAL;
        int leftIndex  = 0;
        int rightIndex = 0;
        while (leftIndex < left.length && rightIndex < right.length)
        {
            int compareId = FBUtilities.compareByteSubArrays(left, leftIndex, right, rightIndex, idLength);
            if (compareId == 0)
            {
                long leftClock  = FBUtilities.byteArrayToLong(left,  leftIndex + idLength);
                long rightClock = FBUtilities.byteArrayToLong(right, rightIndex + idLength);
                leftIndex  += stepLength;
                rightIndex += stepLength;
                if (leftClock == rightClock)
                {
                    continue;
                }
                else if (leftClock > rightClock)
                {
                    if (relationship == ContextRelationship.EQUAL)
                    {
                        relationship = ContextRelationship.GREATER_THAN;
                    }
                    else if (relationship == ContextRelationship.GREATER_THAN)
                    {
                        continue;
                    }
                    else
                    {
                        return ContextRelationship.DISJOINT;
                    }
                }
                else
                {
                    if (relationship == ContextRelationship.EQUAL)
                    {
                        relationship = ContextRelationship.LESS_THAN;
                    }
                    else if (relationship == ContextRelationship.GREATER_THAN)
                    {
                        return ContextRelationship.DISJOINT;
                    }
                    else
                    {
                        continue;
                    }
                }
            }
            else if (compareId > 0)
            {
                rightIndex += stepLength;
                if (relationship == ContextRelationship.EQUAL)
                {
                    relationship = ContextRelationship.LESS_THAN;
                }
                else if (relationship == ContextRelationship.GREATER_THAN)
                {
                    return ContextRelationship.DISJOINT;
                }
                else
                {
                    continue;
                }
            }
            else
            {
                leftIndex += stepLength;
                if (relationship == ContextRelationship.EQUAL)
                {
                    relationship = ContextRelationship.GREATER_THAN;
                }
                else if (relationship == ContextRelationship.GREATER_THAN)
                {
                    continue;
                }
                else
                {
                    return ContextRelationship.DISJOINT;
                }
            }
        }
        if (leftIndex < left.length)
        {
            if (relationship == ContextRelationship.EQUAL)
            {
                return ContextRelationship.GREATER_THAN;
            }
            else if (relationship == ContextRelationship.LESS_THAN)
            {
                return ContextRelationship.DISJOINT;
            }
        }
        else if (rightIndex < right.length)
        {
            if (relationship == ContextRelationship.EQUAL)
            {
                return ContextRelationship.LESS_THAN;
            }
            else if (relationship == ContextRelationship.GREATER_THAN)
            {
                return ContextRelationship.DISJOINT;
            }
        }
        return relationship;
    }
    private class CounterNode
    {
        public final long clock;
        public final long count;
        public CounterNode(long clock, long count)
        {
            this.clock = clock;
            this.count = count;
        }
        public int compareClockTo(CounterNode o)
        {
            if (clock == o.clock)
            {
                return 0;
            }
            else if (clock > o.clock)
            {
                return 1;
            }
            return -1;
        }
        @Override
        public String toString()
        {
            return "(" + clock + "," + count + ")";
        }
    }
    public byte[] merge(byte[] left, byte[] right)
    {
        if (left.length > right.length)
        {
            byte[] tmp = right;
            right = left;
            left = tmp;
        }
        int size = 0;
        int leftOffset  = 0;
        int rightOffset = 0;
        while ((leftOffset < left.length) && (rightOffset < right.length))
        {
            int cmp = FBUtilities.compareByteSubArrays(left, leftOffset, right, rightOffset, idLength);
            if (cmp == 0)
            {
                ++size;
                rightOffset += stepLength;
                leftOffset += stepLength;
            }
            else if (cmp > 0)
            {
                ++size;
                rightOffset += stepLength;
            }
            else 
            {
                ++size;
                leftOffset += stepLength;
            }
        }
        size += (left.length  - leftOffset)  / stepLength;
        size += (right.length - rightOffset) / stepLength;
        byte[] merged = new byte[size * stepLength];
        int mergedOffset = 0; leftOffset = 0; rightOffset = 0;
        while ((leftOffset < left.length) && (rightOffset < right.length))
        {
            int cmp = FBUtilities.compareByteSubArrays(left, leftOffset, right, rightOffset, idLength);
            if (cmp == 0)
            {
                long leftClock = FBUtilities.byteArrayToLong(left, leftOffset + idLength);
                long rightClock = FBUtilities.byteArrayToLong(right, rightOffset + idLength);
                if (FBUtilities.compareByteSubArrays(left, leftOffset, localId, 0, idLength) == 0)
                {
                    long leftCount = FBUtilities.byteArrayToLong(left, leftOffset + idLength + clockLength);
                    long rightCount = FBUtilities.byteArrayToLong(right, rightOffset + idLength + clockLength);
                    writeElementAtStepOffset(merged, mergedOffset / stepLength, localId, leftClock + rightClock, leftCount + rightCount);
                }
                else
                {
                    if (leftClock >= rightClock)
                        System.arraycopy(left, leftOffset, merged, mergedOffset, stepLength);
                    else
                        System.arraycopy(right, rightOffset, merged, mergedOffset, stepLength);
                }
                mergedOffset += stepLength;
                rightOffset += stepLength;
                leftOffset += stepLength;
            }
            else if (cmp > 0)
            {
                System.arraycopy(right, rightOffset, merged, mergedOffset, stepLength);
                mergedOffset += stepLength;
                rightOffset += stepLength;
            }
            else 
            {
                System.arraycopy(left, leftOffset, merged, mergedOffset, stepLength);
                mergedOffset += stepLength;
                leftOffset += stepLength;
            }
        }
        if (leftOffset < left.length)
            System.arraycopy(
                left,
                leftOffset,
                merged,
                mergedOffset,
                left.length - leftOffset);
        if (rightOffset < right.length)
            System.arraycopy(
                right,
                rightOffset,
                merged,
                mergedOffset,
                right.length - rightOffset);
        return merged;
    }
    public String toString(byte[] context)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int offset = 0; offset < context.length; offset += stepLength)
        {
            if (offset > 0)
            {
                sb.append(",");
            }
            sb.append("{");
            try
            {
                InetAddress address = InetAddress.getByAddress(
                            ArrayUtils.subarray(context, offset, offset + idLength));
                sb.append(address.getHostAddress());
            }
            catch (UnknownHostException uhe)
            {
                sb.append("?.?.?.?");
            }
            sb.append(", ");
            sb.append(FBUtilities.byteArrayToLong(context, offset + idLength));
            sb.append(", ");
            sb.append(FBUtilities.byteArrayToLong(context, offset + idLength + clockLength));
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }
    public byte[] total(byte[] context)
    {
        long total = 0L;
        for (int offset = 0; offset < context.length; offset += stepLength)
        {
            long count = FBUtilities.byteArrayToLong(context, offset + idLength + clockLength);
            total += count;
        }
        return FBUtilities.toByteArray(total);
    }
    public byte[] cleanNodeCounts(byte[] context, InetAddress node)
    {
        byte[] nodeId = node.getAddress();
        for (int offset = 0; offset < context.length; offset += stepLength)
        {
            int cmp = FBUtilities.compareByteSubArrays(context, offset, nodeId, 0, idLength);
            if (cmp < 0)
                continue;
            else if (cmp == 0)
            {
                byte[] truncatedContext = new byte[context.length - stepLength];
                System.arraycopy(context, 0, truncatedContext, 0, offset);
                System.arraycopy(
                        context,
                        offset + stepLength,
                        truncatedContext,
                        offset,
                        context.length - (offset + stepLength));
                return truncatedContext;
            }
            else 
            {
                break; 
            }
        }
        return context;
    }
}
