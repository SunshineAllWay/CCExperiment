package org.apache.cassandra.utils;
import org.junit.Test;
import static org.junit.Assert.*;
public class EstimatedHistogramTest
{
    @Test
    public void testFindingCorrectBuckets()
    {
        EstimatedHistogram histogram = new EstimatedHistogram();
        histogram.add(0L);
        assertEquals(1, histogram.get(false)[0]);
        histogram.add(23282687);
        assertEquals(1, histogram.get(false)[histogram.buckets.length() - 2]);
        histogram.add(1);
        assertEquals(1, histogram.get(false)[1]);
        histogram.add(9);
        assertEquals(1, histogram.get(false)[8]);
        histogram.add(20);
        histogram.add(21);
        histogram.add(22);
        assertEquals(3, histogram.get(false)[13]);
        assertEquals(1, histogram.min());
        assertEquals(25109160, histogram.max());
        assertEquals(20, histogram.median());
    }
}
