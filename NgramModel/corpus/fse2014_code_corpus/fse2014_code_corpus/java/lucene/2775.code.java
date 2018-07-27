package org.apache.solr.common.util;
import junit.framework.TestCase;
import java.util.Random;
public class TestHash extends TestCase {
  public void testEqualsLOOKUP3() {
    int[] hashes = new int[] {0xc4c20dd5,0x3ab04cc3,0xebe874a3,0x0e770ef3,0xec321498,0x73845e86,0x8a2db728,0x03c313bb,0xfe5b9199,0x95965125,0xcbc4e7c2};
    String s = "hello world";
    int[] a = new int[s.length()];
    for (int i=0; i<s.length(); i++) {
      a[i] = s.charAt(i);
      int len = i+1;
      int hash = Hash.lookup3(a, 0, len, i*12345);
      assertEquals(hashes[i], hash);
      int hash2 = Hash.lookup3ycs(a, 0, len, i*12345+(len<<2));
      assertEquals(hashes[i], hash2);
      int hash3 = Hash.lookup3ycs(s, 0, len, i*12345+(len<<2));
      assertEquals(hashes[i], hash3);
    }
  }
  void tstEquiv(int[] utf32, int len) {
    int seed=100;
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<len; i++) sb.appendCodePoint(utf32[i]);
    int hash = Hash.lookup3(utf32, 0, len, seed -(len<<2));
    int hash2 = Hash.lookup3ycs(utf32, 0, len, seed);
    assertEquals(hash, hash2);
    int hash3 = Hash.lookup3ycs(sb, 0, sb.length(), seed);
    assertEquals(hash, hash3);
    long hash4 = Hash.lookup3ycs64(sb, 0, sb.length(), seed);
    assertEquals((int)hash4, hash);
  }
  public void testHash() {
    Random r = new Random(0);
    int[] utf32 = new int[20];
    tstEquiv(utf32,0);
    utf32[0]=0x10000;
    tstEquiv(utf32,1);
    utf32[0]=0x8000;
    tstEquiv(utf32,1);
    utf32[0]=Character.MAX_CODE_POINT;
    tstEquiv(utf32,1);
    for (int iter=0; iter<10000; iter++) {
      int len = r.nextInt(utf32.length+1);
      for (int i=0; i<len; i++) {
        int codePoint;
        do  {
          codePoint = r.nextInt(Character.MAX_CODE_POINT+1);
        } while((codePoint & 0xF800) == 0xD800);  
        utf32[i] = codePoint;
      }
      tstEquiv(utf32, len);
    }
  }
}
