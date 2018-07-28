package org.apache.cassandra.utils;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.utils.obs.OpenBitSet;
public class BloomFilter extends Filter
{
    private static final Logger logger = LoggerFactory.getLogger(BloomFilter.class);
    private static final int EXCESS = 20;
    static ICompactSerializer<BloomFilter> serializer_ = new BloomFilterSerializer();
    public OpenBitSet bitset;
    BloomFilter(int hashes, OpenBitSet bs)
    {
        hashCount = hashes;
        bitset = bs;
    }
    public static ICompactSerializer<BloomFilter> serializer()
    {
        return serializer_;
    }
    long emptyBuckets()
    {
        long n = 0;
        for (long i = 0; i < buckets(); i++)
        {
            if (!bitset.get(i))
            {
                n++;
            }
        }
        return n;
    }
    private static OpenBitSet bucketsFor(long numElements, int bucketsPer)
    {
        long numBits = numElements * bucketsPer + EXCESS; 
        return new OpenBitSet((long)Math.min(Long.MAX_VALUE, numBits));
    }
    public static BloomFilter getFilter(long numElements, int targetBucketsPerElem)
    {
        int maxBucketsPerElement = Math.max(1, BloomCalculations.maxBucketsPerElement(numElements));
        int bucketsPerElement = Math.min(targetBucketsPerElem, maxBucketsPerElement);
        if (bucketsPerElement < targetBucketsPerElem)
        {
            logger.warn(String.format("Cannot provide an optimal BloomFilter for %d elements (%d/%d buckets per element).",
                                    numElements, bucketsPerElement, targetBucketsPerElem));
        }
        BloomCalculations.BloomSpecification spec = BloomCalculations.computeBloomSpec(bucketsPerElement);
        return new BloomFilter(spec.K, bucketsFor(numElements, spec.bucketsPerElement));
    }
    public static BloomFilter getFilter(long numElements, double maxFalsePosProbability)
    {
        assert maxFalsePosProbability <= 1.0 : "Invalid probability";
        int bucketsPerElement = BloomCalculations.maxBucketsPerElement(numElements);
        BloomCalculations.BloomSpecification spec = BloomCalculations.computeBloomSpec(bucketsPerElement, maxFalsePosProbability);
        return new BloomFilter(spec.K, bucketsFor(numElements, spec.bucketsPerElement));
    }
    private long buckets()
    {
      return bitset.size();
    }
    private long[] getHashBuckets(ByteBuffer key)
    {
        return BloomFilter.getHashBuckets(key, hashCount, buckets());
    }
    static long[] getHashBuckets(ByteBuffer b, int hashCount, long max)
    {
        long[] result = new long[hashCount];
        long hash1 = MurmurHash.hash64(b, b.position(), b.remaining(), 0L);
        long hash2 = MurmurHash.hash64(b, b.position(), b.remaining(), hash1);
        for (int i = 0; i < hashCount; ++i)
        {
            result[i] = Math.abs((hash1 + (long)i * hash2) % max);
        }
        return result;
    }
    public void add(ByteBuffer key)
    {
        for (long bucketIndex : getHashBuckets(key))
        {
            bitset.set(bucketIndex);
        }
    }
    public boolean isPresent(ByteBuffer key)
    {
      for (long bucketIndex : getHashBuckets(key))
      {
          if (!bitset.get(bucketIndex))
          {
              return false;
          }
      }
      return true;
    }
    public void clear()
    {
        bitset.clear(0, bitset.size());
    }
}
