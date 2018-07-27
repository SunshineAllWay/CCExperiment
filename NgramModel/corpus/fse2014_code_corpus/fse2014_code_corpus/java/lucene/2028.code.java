package org.apache.lucene.util;
public class TestCloseableThreadLocal extends LuceneTestCase {
  public static final String TEST_VALUE = "initvaluetest";
  public void testInitValue() {
    InitValueThreadLocal tl = new InitValueThreadLocal();
    String str = (String)tl.get();
    assertEquals(TEST_VALUE, str);
  }
  public void testNullValue() throws Exception {
    CloseableThreadLocal<Object> ctl = new CloseableThreadLocal<Object>();
    ctl.set(null);
    assertNull(ctl.get());
  }
  public void testDefaultValueWithoutSetting() throws Exception {
    CloseableThreadLocal<Object> ctl = new CloseableThreadLocal<Object>();
    assertNull(ctl.get());
  }
  public class InitValueThreadLocal extends CloseableThreadLocal<Object> {
    @Override
    protected Object initialValue() {
      return TEST_VALUE;
    } 
  }
}
