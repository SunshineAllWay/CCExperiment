package org.apache.solr.util;
import junit.framework.TestCase;
public class ArraysUtilsTest extends TestCase {
  public ArraysUtilsTest(String s) {
    super(s);
  }
  protected void setUp() {
  }
  protected void tearDown() {
  }
  public void test() {
    String left = "this is equal";
    String right = left;
    char[] leftChars = left.toCharArray();
    char[] rightChars = right.toCharArray();
    assertTrue(left + " does not equal: " + right, ArraysUtils.equals(leftChars, 0, rightChars, 0, left.length()));
    assertFalse(left + " does not equal: " + right, ArraysUtils.equals(leftChars, 1, rightChars, 0, left.length()));
    assertFalse(left + " does not equal: " + right, ArraysUtils.equals(leftChars, 1, rightChars, 2, left.length()));
    assertFalse(left + " does not equal: " + right, ArraysUtils.equals(leftChars, 25, rightChars, 0, left.length()));
    assertFalse(left + " does not equal: " + right, ArraysUtils.equals(leftChars, 12, rightChars, 0, left.length()));
  }
}