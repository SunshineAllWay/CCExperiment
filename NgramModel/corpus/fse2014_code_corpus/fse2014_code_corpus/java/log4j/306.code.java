package org.apache.log4j.helpers;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
public class BoundedFIFOTestCase extends TestCase {
  static Logger cat = Logger.getLogger("x");
  static int MAX = 1000;  
  static LoggingEvent[] e = new LoggingEvent[MAX];
  {
    for (int i = 0; i < MAX; i++) {
      e[i] =  new LoggingEvent("", cat, Level.DEBUG, "e"+i, null);
    }
  }
  public BoundedFIFOTestCase(String name) {
    super(name);
  }
  public
  void setUp() {
  }
  public
  void test1() {
    for(int size = 1; size <= 128; size *=2) {
      BoundedFIFO bf = new BoundedFIFO(size);
      assertEquals(bf.getMaxSize(), size);
      assertNull(bf.get());
      int i;
      int j;
      int k;
      for(i = 1; i < 2*size; i++) {      
	for(j = 0; j < i; j++) {
	  bf.put(e[j]); assertEquals(bf.length(), j < size ?  j+1 : size);
	}
	int max = size < j ? size : j;
	j--;
	for(k = 0; k <= j; k++) {	  
	  assertEquals(bf.length(), max - k > 0 ? max - k : 0); 
	  Object r = bf.get();
	  if(k >= size) 
	    assertNull(r);
	  else 
	    assertEquals(r, e[k]);
	}
      }
    }
  }
  public
  void test2() {
    int size = 3;
    BoundedFIFO bf = new BoundedFIFO(size);
    bf.put(e[0]);	
    assertEquals(bf.get(), e[0]);
    assertNull(bf.get());
    bf.put(e[1]); assertEquals(bf.length(), 1);
    bf.put(e[2]); assertEquals(bf.length(), 2);
    bf.put(e[3]); assertEquals(bf.length(), 3);
    assertEquals(bf.get(), e[1]); assertEquals(bf.length(), 2);
    assertEquals(bf.get(), e[2]); assertEquals(bf.length(), 1);
    assertEquals(bf.get(), e[3]); assertEquals(bf.length(), 0);
    assertNull(bf.get()); assertEquals(bf.length(), 0);
  }
  int min(int a, int b) {
    return a < b ? a : b;
  }
  public
  void testResize1() {
    int size = 10;
    for(int n = 1; n < size*2; n++) {
      for(int i = 0; i < size*2; i++) {
        BoundedFIFO bf = new BoundedFIFO(size);
        for(int f = 0; f < i; f++) {
          bf.put(e[f]);
        }
        bf.resize(n);
        int expectedSize = min(n, min(i, size));
        assertEquals(bf.length(), expectedSize);
        for(int c = 0; c < expectedSize; c++) {
          assertEquals(bf.get(), e[c]);
        }
      }
    }
  }
  public
  void testResize2() {
    int size = 10;
    for(int n = 1; n < size*2; n++) {
      for(int i = 0; i < size*2; i++) {
	for(int d = 0; d < min(i,size); d++) {
	  BoundedFIFO bf = new BoundedFIFO(size);
	  for(int p = 0; p < i; p++) {
	    bf.put(e[p]);
	  }
	  for(int g = 0; g < d; g++) {
	    bf.get();
	  }
	  int x = bf.length();
	  bf.resize(n);
	  int expectedSize = min(n, x);
	  assertEquals(bf.length(), expectedSize);
	  for(int c = 0; c < expectedSize; c++) {
	    assertEquals(bf.get(), e[c+d]);
	  }
	  assertNull(bf.get());
	}
      }
    }
  }
  public
  void testResize3() {
    int size = 10;
    for(int n = 1; n < size*2; n++) {
      for(int i = 0; i < size; i++) {
	for(int d = 0; d < i; d++) {
	  for(int r = 0; r < d; r++) {
	    BoundedFIFO bf = new BoundedFIFO(size);
	    for(int p0 = 0; p0 < i; p0++)
	      bf.put(e[p0]);
	    for(int g = 0; g < d; g++) 
	      bf.get();	    
	    for(int p1 = 0; p1 < r; p1++) 
	      bf.put(e[i+p1]);
	    int x =  bf.length();
	    bf.resize(n);
	    int expectedSize = min(n, x);
	    assertEquals(bf.length(), expectedSize);
	    for(int c = 0; c < expectedSize; c++) {
	      assertEquals(bf.get(), e[c+d]);
	    }
	  }
	}
      }
    }
  }
  public
  static
  Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new BoundedFIFOTestCase("test1"));
    suite.addTest(new BoundedFIFOTestCase("test2"));
    suite.addTest(new BoundedFIFOTestCase("testResize1"));
    suite.addTest(new BoundedFIFOTestCase("testResize2"));
    suite.addTest(new BoundedFIFOTestCase("testResize3"));
    return suite;
  }
}
