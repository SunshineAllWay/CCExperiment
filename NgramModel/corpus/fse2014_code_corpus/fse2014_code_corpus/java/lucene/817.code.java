package org.apache.lucene.analysis.cn.smart.hhmm;
import java.io.UnsupportedEncodingException;
abstract class AbstractDictionary {
  public static final int GB2312_FIRST_CHAR = 1410;
  public static final int GB2312_CHAR_NUM = 87 * 94;
  public static final int CHAR_NUM_IN_FILE = 6768;
  public String getCCByGB2312Id(int ccid) {
    if (ccid < 0 || ccid > WordDictionary.GB2312_CHAR_NUM)
      return "";
    int cc1 = ccid / 94 + 161;
    int cc2 = ccid % 94 + 161;
    byte[] buffer = new byte[2];
    buffer[0] = (byte) cc1;
    buffer[1] = (byte) cc2;
    try {
      String cchar = new String(buffer, "GB2312");
      return cchar;
    } catch (UnsupportedEncodingException e) {
      return "";
    }
  }
  public short getGB2312Id(char ch) {
    try {
      byte[] buffer = Character.toString(ch).getBytes("GB2312");
      if (buffer.length != 2) {
        return -1;
      }
      int b0 = (int) (buffer[0] & 0x0FF) - 161; 
      int b1 = (int) (buffer[1] & 0x0FF) - 161; 
      return (short) (b0 * 94 + b1);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return -1;
  }
  public long hash1(char c) {
    final long p = 1099511628211L;
    long hash = 0xcbf29ce484222325L;
    hash = (hash ^ (c & 0x00FF)) * p;
    hash = (hash ^ (c >> 8)) * p;
    hash += hash << 13;
    hash ^= hash >> 7;
    hash += hash << 3;
    hash ^= hash >> 17;
    hash += hash << 5;
    return hash;
  }
  public long hash1(char carray[]) {
    final long p = 1099511628211L;
    long hash = 0xcbf29ce484222325L;
    for (int i = 0; i < carray.length; i++) {
      char d = carray[i];
      hash = (hash ^ (d & 0x00FF)) * p;
      hash = (hash ^ (d >> 8)) * p;
    }
    return hash;
  }
  public int hash2(char c) {
    int hash = 5381;
    hash = ((hash << 5) + hash) + c & 0x00FF;
    hash = ((hash << 5) + hash) + c >> 8;
    return hash;
  }
  public int hash2(char carray[]) {
    int hash = 5381;
    for (int i = 0; i < carray.length; i++) {
      char d = carray[i];
      hash = ((hash << 5) + hash) + d & 0x00FF;
      hash = ((hash << 5) + hash) + d >> 8;
    }
    return hash;
  }
}
