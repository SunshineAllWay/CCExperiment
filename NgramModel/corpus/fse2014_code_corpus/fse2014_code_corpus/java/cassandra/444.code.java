package org.apache.cassandra.utils;
import java.io.IOException;
import java.util.Random;
import org.junit.Test;
public class LongBloomFilterTest
{
    public BloomFilter bf;
    @Test
    public void testBigInt()
    {
        int size = 10 * 1000 * 1000;
        bf = BloomFilter.getFilter(size, FilterTestHelper.spec.bucketsPerElement);
        FilterTestHelper.testFalsePositives(bf,
                                            new KeyGenerator.IntGenerator(size),
                                            new KeyGenerator.IntGenerator(size, size * 2));
    }
    @Test
    public void testBigRandom()
    {
        int size = 10 * 1000 * 1000;
        bf = BloomFilter.getFilter(size, FilterTestHelper.spec.bucketsPerElement);
        FilterTestHelper.testFalsePositives(bf,
                                            new KeyGenerator.RandomStringGenerator(new Random().nextInt(), size),
                                            new KeyGenerator.RandomStringGenerator(new Random().nextInt(), size));
    }
    @Test
    public void timeit()
    {
        int size = 300 * FilterTestHelper.ELEMENTS;
        bf = BloomFilter.getFilter(size, FilterTestHelper.spec.bucketsPerElement);
        for (int i = 0; i < 10; i++)
        {
            FilterTestHelper.testFalsePositives(bf,
                                                new KeyGenerator.RandomStringGenerator(new Random().nextInt(), size),
                                                new KeyGenerator.RandomStringGenerator(new Random().nextInt(), size));
            bf.clear();
        }
    }
}
