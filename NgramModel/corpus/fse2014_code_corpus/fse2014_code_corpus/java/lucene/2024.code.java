package org.apache.lucene.util;
import java.util.Random;
public class TestArrayUtil extends LuceneTestCase {
  public void testGrowth() {
    int currentSize = 0;
    long copyCost = 0;
    while(currentSize != Integer.MAX_VALUE) {
      int nextSize = ArrayUtil.oversize(1+currentSize, RamUsageEstimator.NUM_BYTES_OBJECT_REF);
      assertTrue(nextSize > currentSize);
      if (currentSize > 0) {
        copyCost += currentSize;
        double copyCostPerElement = ((double) copyCost)/currentSize;
        assertTrue("cost " + copyCostPerElement, copyCostPerElement < 10.0);
      }
      currentSize = nextSize;
    }
  }
  public void testMaxSize() {
    for(int elemSize=0;elemSize<10;elemSize++) {
      assertEquals(Integer.MAX_VALUE, ArrayUtil.oversize(Integer.MAX_VALUE, elemSize));
      assertEquals(Integer.MAX_VALUE, ArrayUtil.oversize(Integer.MAX_VALUE-1, elemSize));
    }
  }
  public void testInvalidElementSizes() {
    final Random r = newRandom();
    for(int iter=0;iter<10000;iter++) {
      final int minTargetSize = r.nextInt(Integer.MAX_VALUE);
      final int elemSize = r.nextInt(11);
      final int v = ArrayUtil.oversize(minTargetSize, elemSize);
      assertTrue(v >= minTargetSize);
    }
  }
  public void testParseInt() throws Exception {
    int test;
    try {
      test = ArrayUtil.parseInt("".toCharArray());
      assertTrue(false);
    } catch (NumberFormatException e) {
    }
    try {
      test = ArrayUtil.parseInt("foo".toCharArray());
      assertTrue(false);
    } catch (NumberFormatException e) {
    }
    try {
      test = ArrayUtil.parseInt(String.valueOf(Long.MAX_VALUE).toCharArray());
      assertTrue(false);
    } catch (NumberFormatException e) {
    }
    try {
      test = ArrayUtil.parseInt("0.34".toCharArray());
      assertTrue(false);
    } catch (NumberFormatException e) {
    }
    try {
      test = ArrayUtil.parseInt("1".toCharArray());
      assertTrue(test + " does not equal: " + 1, test == 1);
      test = ArrayUtil.parseInt("-10000".toCharArray());
      assertTrue(test + " does not equal: " + -10000, test == -10000);
      test = ArrayUtil.parseInt("1923".toCharArray());
      assertTrue(test + " does not equal: " + 1923, test == 1923);
      test = ArrayUtil.parseInt("-1".toCharArray());
      assertTrue(test + " does not equal: " + -1, test == -1);
      test = ArrayUtil.parseInt("foo 1923 bar".toCharArray(), 4, 4);
      assertTrue(test + " does not equal: " + 1923, test == 1923);
    } catch (NumberFormatException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }
}
