package org.apache.cassandra.utils;
import static org.junit.Assert.*;
import java.util.Iterator;
import org.junit.Test;
public class BoundedStatsDequeTest
{
    @Test
    public void test()
    {
        int size = 4;
        BoundedStatsDeque bsd = new BoundedStatsDeque(size);
        assertEquals(0, bsd.size());
        assertEquals(0, bsd.sum(), 0.001d);
        assertEquals(Double.NaN, bsd.mean(), 0.001d);
        assertEquals(Double.NaN, bsd.variance(), 0.001d);
        assertEquals(Double.NaN, bsd.stdev(), 0.001d);
        assertEquals(0, bsd.sumOfDeviations(), 0.001d);
        bsd.add(1d); 
        bsd.add(2d);
        bsd.add(3d);
        bsd.add(4d);
        bsd.add(5d);
        Iterator<Double> iter = bsd.iterator();
        assertTrue(iter.hasNext());
        assertEquals(2d, iter.next(), 0);
        assertTrue(iter.hasNext());
        assertEquals(3d, iter.next(), 0);
        assertTrue(iter.hasNext());
        assertEquals(4d, iter.next(), 0);
        assertTrue(iter.hasNext());
        assertEquals(5d, iter.next(), 0);
        assertFalse(iter.hasNext());
        assertEquals(size, bsd.size());
        assertEquals(14, bsd.sum(), 0.001d);
        assertEquals(3.5, bsd.mean(), 0.001d);
        assertEquals(1.25, bsd.variance(), 0.001d);
        assertEquals(1.1180d, bsd.stdev(), 0.001d);
        assertEquals(5, bsd.sumOfDeviations(), 0.001d);
        bsd.clear();
        assertFalse(bsd.iterator().hasNext());
    }
}
