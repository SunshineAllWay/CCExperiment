package org.apache.lucene.util;
public class StringInterner {
  public String intern(String s) {
    return s.intern();
  }
  public String intern(char[] arr, int offset, int len) {
    return intern(new String(arr, offset, len));
  }
}
