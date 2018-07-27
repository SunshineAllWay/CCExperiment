package org.apache.lucene.document;
import org.apache.lucene.util.LuceneTestCase;
public class TestNumberTools extends LuceneTestCase {
    public void testNearZero() {
        for (int i = -100; i <= 100; i++) {
            for (int j = -100; j <= 100; j++) {
                subtestTwoLongs(i, j);
            }
        }
    }
    public void testMax() {
        assertEquals(Long.MAX_VALUE, NumberTools
                .stringToLong(NumberTools.MAX_STRING_VALUE));
        assertEquals(NumberTools.MAX_STRING_VALUE, NumberTools
                .longToString(Long.MAX_VALUE));
        for (long l = Long.MAX_VALUE; l > Long.MAX_VALUE - 10000; l--) {
            subtestTwoLongs(l, l - 1);
        }
    }
    public void testMin() {
        assertEquals(Long.MIN_VALUE, NumberTools
                .stringToLong(NumberTools.MIN_STRING_VALUE));
        assertEquals(NumberTools.MIN_STRING_VALUE, NumberTools
                .longToString(Long.MIN_VALUE));
        for (long l = Long.MIN_VALUE; l < Long.MIN_VALUE + 10000; l++) {
            subtestTwoLongs(l, l + 1);
        }
    }
    private static void subtestTwoLongs(long i, long j) {
        String a = NumberTools.longToString(i);
        String b = NumberTools.longToString(j);
        assertEquals(NumberTools.STR_SIZE, a.length());
        assertEquals(NumberTools.STR_SIZE, b.length());
        if (i < j) {
            assertTrue(a.compareTo(b) < 0);
        } else if (i > j) {
            assertTrue(a.compareTo(b) > 0);
        } else {
            assertEquals(a, b);
        }
        long i2 = NumberTools.stringToLong(a);
        long j2 = NumberTools.stringToLong(b);
        assertEquals(i, i2);
        assertEquals(j, j2);
    }
}