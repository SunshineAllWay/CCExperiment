package org.apache.solr.util;
public class ArraysUtils {
  public static boolean equals(char[] left, int offsetLeft, char[] right, int offsetRight, int length) {
    if ((offsetLeft + length <= left.length) && (offsetRight + length <= right.length)) {
      for (int i = 0; i < length; i++) {
        if (left[offsetLeft + i] != right[offsetRight + i]) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
}
