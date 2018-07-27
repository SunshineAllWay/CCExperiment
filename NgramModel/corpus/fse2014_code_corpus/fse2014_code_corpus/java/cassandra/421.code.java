package org.apache.cassandra.utils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import org.apache.cassandra.io.ICompactSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class LegacyBloomFilter extends Filter
{
    private static final int EXCESS = 20;
    private static final Logger logger = LoggerFactory.getLogger(LegacyBloomFilter.class);
    static ICompactSerializer<LegacyBloomFilter> serializer_ = new LegacyBloomFilterSerializer();
    public static ICompactSerializer<LegacyBloomFilter> serializer()
    {
        return serializer_;
    }
    private BitSet filter_;
    LegacyBloomFilter(int hashes, BitSet filter)
    {
        hashCount = hashes;
        filter_ = filter;
    }
    private static BitSet bucketsFor(long numElements, int bucketsPer)
    {
        long numBits = numElements * bucketsPer + EXCESS;
        return new BitSet((int)Math.min(Integer.MAX_VALUE, numBits));
    }
    public static LegacyBloomFilter getFilter(long numElements, int targetBucketsPerElem)
    {
        int maxBucketsPerElement = Math.max(1, BloomCalculations.maxBucketsPerElement(numElements));
        int bucketsPerElement = Math.min(targetBucketsPerElem, maxBucketsPerElement);
        if (bucketsPerElement < targetBucketsPerElem)
        {
            logger.warn(String.format("Cannot provide an optimal LegacyBloomFilter for %d elements (%d/%d buckets per element).",
                                      numElements, bucketsPerElement, targetBucketsPerElem));
        }
        BloomCalculations.BloomSpecification spec = BloomCalculations.computeBloomSpec(bucketsPerElement);
        return new LegacyBloomFilter(spec.K, bucketsFor(numElements, spec.bucketsPerElement));
    }
    public static LegacyBloomFilter getFilter(long numElements, double maxFalsePosProbability)
    {
        assert maxFalsePosProbability <= 1.0 : "Invalid probability";
        int bucketsPerElement = BloomCalculations.maxBucketsPerElement(numElements);
        BloomCalculations.BloomSpecification spec = BloomCalculations.computeBloomSpec(bucketsPerElement, maxFalsePosProbability);
        return new LegacyBloomFilter(spec.K, bucketsFor(numElements, spec.bucketsPerElement));
    }
    public void clear()
    {
        filter_.clear();
    }
    int buckets()
    {
        return filter_.size();
    }
    public boolean isPresent(ByteBuffer key)
    {
        for (int bucketIndex : getHashBuckets(key))
        {
            if (!filter_.get(bucketIndex))
            {
                return false;
            }
        }
        return true;
    }
    public void add(ByteBuffer key)
    {
        for (int bucketIndex : getHashBuckets(key))
        {
            filter_.set(bucketIndex);
        }
    }
    public String toString()
    {
        return filter_.toString();
    }
    ICompactSerializer tserializer()
    {
        return serializer_;
    }
    int emptyBuckets()
    {
        int n = 0;
        for (int i = 0; i < buckets(); i++)
        {
            if (!filter_.get(i))
            {
                n++;
            }
        }
        return n;
    }
    public static LegacyBloomFilter alwaysMatchingBloomFilter()
    {
        BitSet set = new BitSet(64);
        set.set(0, 64);
        return new LegacyBloomFilter(1, set);
    }
    public int[] getHashBuckets(ByteBuffer key)
    {
        return LegacyBloomFilter.getHashBuckets(key, hashCount, buckets());
    }
    static int[] getHashBuckets(ByteBuffer b, int hashCount, int max)
    {
        int[] result = new int[hashCount];
        int hash1 = MurmurHash.hash32(b, b.position(), b.remaining(), 0);
        int hash2 = MurmurHash.hash32(b, b.position(), b.remaining(), hash1);
        for (int i = 0; i < hashCount; i++)
        {
            result[i] = Math.abs((hash1 + i * hash2) % max);
        }
        return result;
    }
    public BitSet getBitSet(){
      return filter_;
    }
}