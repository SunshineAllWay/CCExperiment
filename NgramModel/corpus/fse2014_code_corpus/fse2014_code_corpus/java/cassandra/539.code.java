package org.apache.cassandra.utils;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.junit.Test;
public class FilterTestHelper
{
    static final double MAX_FAILURE_RATE = 0.1;
    public static final BloomCalculations.BloomSpecification spec = BloomCalculations.computeBloomSpec(15, MAX_FAILURE_RATE);
    static final int ELEMENTS = 10000;
    static final ResetableIterator<ByteBuffer> intKeys()
    {
        return new KeyGenerator.IntGenerator(ELEMENTS);
    }
    static final ResetableIterator<ByteBuffer> randomKeys()
    {
        return new KeyGenerator.RandomStringGenerator(314159, ELEMENTS);
    }
    static final ResetableIterator<ByteBuffer> randomKeys2()
    {
        return new KeyGenerator.RandomStringGenerator(271828, ELEMENTS);
    }
    public static void testFalsePositives(Filter f, ResetableIterator<ByteBuffer> keys, ResetableIterator<ByteBuffer> otherkeys)
    {
        assert keys.size() == otherkeys.size();
        while (keys.hasNext())
        {
            f.add(keys.next());
        }
        int fp = 0;
        while (otherkeys.hasNext())
        {
            if (f.isPresent(otherkeys.next()))
            {
                fp++;
            }
        }
        double fp_ratio = fp / (keys.size() * BloomCalculations.probs[spec.bucketsPerElement][spec.K]);
        assert fp_ratio < 1.03 : fp_ratio;
    }
    public void testTrue()
    {
      assert true;
    }
}
