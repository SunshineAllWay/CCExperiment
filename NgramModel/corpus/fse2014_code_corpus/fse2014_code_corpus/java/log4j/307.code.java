package org.apache.log4j.helpers;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
public class CyclicBufferTestCase extends TestCase {
  static Logger cat = Logger.getLogger("x");
  static int MAX = 1000;
  static LoggingEvent[] e = new LoggingEvent[MAX];
  {
    for (int i = 0; i < MAX; i++) {
      e[i] =  new LoggingEvent("", cat, Level.DEBUG, "e"+i, null);
    }
  }
  public CyclicBufferTestCase(String name) {
    super(name);
  }
  public
  void setUp() {
  }
  public
  void test0() {
    int size = 2;
    CyclicBuffer cb = new CyclicBuffer(size);    
    assertEquals(cb.getMaxSize(), size);    
    cb.add(e[0]);
    assertEquals(cb.length(), 1);    
    assertEquals(cb.get(), e[0]); assertEquals(cb.length(), 0);
    assertNull(cb.get()); assertEquals(cb.length(), 0);
    cb = new CyclicBuffer(size);    
    cb.add(e[0]);
    cb.add(e[1]);
    assertEquals(cb.length(), 2);    
    assertEquals(cb.get(), e[0]); assertEquals(cb.length(), 1);
    assertEquals(cb.get(), e[1]); assertEquals(cb.length(), 0);
    assertNull(cb.get()); assertEquals(cb.length(), 0);
  }
  public
  void test1() {
    for(int bufSize = 1; bufSize <= 128; bufSize *=2) 
      doTest1(bufSize);
  }
  void doTest1(int size) {
    CyclicBuffer cb = new CyclicBuffer(size);
    assertEquals(cb.getMaxSize(), size);
    for(int i = -(size+10); i < (size+10); i++) {
      assertNull(cb.get(i));
    }
    for(int i = 0; i < MAX; i++) {
      cb.add(e[i]);
      int limit = i < size-1 ? i : size-1;
      for(int j = limit; j >= 0; j--) {
	assertEquals(cb.get(j), e[i-(limit-j)]);
      }
      assertNull(cb.get(-1));
      assertNull(cb.get(limit+1));
    }
  }
  public
  void testResize() {
    for(int isize = 1; isize <= 128; isize *=2) {      
      doTestResize(isize, isize/2+1, isize/2+1);
      doTestResize(isize, isize/2+1, isize+10);
      doTestResize(isize, isize+10, isize/2+1);
      doTestResize(isize, isize+10, isize+10);
    }
  }
  void doTestResize(int initialSize, int numberOfAdds, int newSize) {
    CyclicBuffer cb = new CyclicBuffer(initialSize);
    for(int i = 0; i < numberOfAdds; i++) {
      cb.add(e[i]);
    }    
    cb.resize(newSize);
    int offset = numberOfAdds - initialSize;
    if(offset< 0)
      offset = 0;
    int len = newSize < numberOfAdds ? newSize : numberOfAdds;
    len = len < initialSize ? len : initialSize;
    for(int j = 0; j < len; j++) {
      assertEquals(cb.get(j), e[offset+j]);
    }
  }
  public
  static
  Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new CyclicBufferTestCase("test0"));
    suite.addTest(new CyclicBufferTestCase("test1"));
    suite.addTest(new CyclicBufferTestCase("testResize"));
    return suite;
  }
}
