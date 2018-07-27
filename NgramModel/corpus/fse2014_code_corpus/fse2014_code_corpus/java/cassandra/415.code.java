package org.apache.cassandra.utils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLongArray;
import org.apache.cassandra.io.ICompactSerializer;
public class EstimatedHistogram
{
    private long[] bucketOffsets;
    private int numBuckets;
    final AtomicLongArray buckets;
    public static EstimatedHistogramSerializer serializer = new EstimatedHistogramSerializer();
    public EstimatedHistogram()
    {
        this(90);
    }
    public EstimatedHistogram(int bucketCount)
    {
        makeOffsets(bucketCount);
        buckets = new AtomicLongArray(numBuckets);
    }
    public EstimatedHistogram(long[] bucketData)
    {
        makeOffsets(bucketData.length - 1);
        buckets = new AtomicLongArray(bucketData);
    }
    public EstimatedHistogram(long[] offsets, long[] bucketData)
    {
        assert bucketData.length == offsets.length +1;
        bucketOffsets = offsets;
        buckets = new AtomicLongArray(bucketData);
        numBuckets = bucketData.length;
    }
    private void makeOffsets(int size)
    {
        bucketOffsets = new long[size];
        long last = 1;
        bucketOffsets[0] = last;
        for(int i = 1; i < size; i++)
        {
            long next = Math.round(last * 1.2);
            if (next == last)
                next++;
            bucketOffsets[i] = next;
            last = next;
        }
        numBuckets = bucketOffsets.length + 1;
    }
    public long[] getBucketOffsets()
    {
        return bucketOffsets;
    }
    public void add(long n)
    {
        int index = Arrays.binarySearch(bucketOffsets, n);
        if (index < 0)
        {
            index = -index - 1;
        }
        else
        {
            index += 1;
        }
        buckets.incrementAndGet(index);
    }
    public long[] get(boolean reset)
    {
        long[] rv = new long[numBuckets];
        for (int i = 0; i < numBuckets; i++)
            rv[i] = buckets.get(i);
        if (reset)
            for (int i = 0; i < numBuckets; i++)
                buckets.set(i, 0L);
        return rv;
    }
    public long min()
    {
        for (int i = 0; i < numBuckets; i++)
        {
            if (buckets.get(i) > 0)
                return bucketOffsets[i == 0 ? 0 : i - 1];
        }
        return 0;
    }
    public long max()
    {
        int lastBucket = numBuckets - 1;
        if (buckets.get(lastBucket) > 0)
            throw new IllegalStateException("Unable to compute ceiling for max when all buckets are full");
        for (int i = lastBucket - 1; i >= 0; i--)
        {
            if (buckets.get(i) > 0)
                return bucketOffsets[i];
        }
        return 0;
    }
    public long median()
    {
        long max = 0;
        long median = 0;
        for (int i = 0; i < numBuckets; i++)
        {
            if (max < 1 || buckets.get(i) > max)
            {
                max = buckets.get(i);
                if (max > 0)
                    median = bucketOffsets[i == 0 ? 0 : i - 1];
            }
        }
        return median;
    }
    public static class EstimatedHistogramSerializer implements ICompactSerializer<EstimatedHistogram>
    {
        public void serialize(EstimatedHistogram eh, DataOutputStream dos) throws IOException
        {
            long[] offsets = eh.getBucketOffsets();
            long[] buckets = eh.get(false);
            dos.writeInt(buckets.length);
            for (int i = 0; i < buckets.length; i++)
            {
                dos.writeLong(offsets[i == 0 ? 0 : i - 1]);
                dos.writeLong(buckets[i]);
            }
        }
        public EstimatedHistogram deserialize(DataInputStream dis) throws IOException
        {
            int size = dis.readInt();
            long[] offsets = new long[size - 1];
            long[] buckets = new long[size];
            for (int i = 0; i < size; i++) {
                offsets[i == 0 ? 0 : i - 1] = dis.readLong();
                buckets[i] = dis.readLong();
            }
            return new EstimatedHistogram(offsets, buckets);
        }
    }
}
