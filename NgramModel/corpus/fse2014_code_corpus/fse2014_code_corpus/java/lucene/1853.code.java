package org.apache.lucene.analysis.tokenattributes;
import org.apache.lucene.util.LuceneTestCase;
public class TestTermAttributeImpl extends LuceneTestCase {
  public TestTermAttributeImpl(String name) {
    super(name);
  }
  public void testResize() {
    TermAttributeImpl t = new TermAttributeImpl();
    char[] content = "hello".toCharArray();
    t.setTermBuffer(content, 0, content.length);
    for (int i = 0; i < 2000; i++)
    {
      t.resizeTermBuffer(i);
      assertTrue(i <= t.termBuffer().length);
      assertEquals("hello", t.term());
    }
  }
  public void testGrow() {
    TermAttributeImpl t = new TermAttributeImpl();
    StringBuilder buf = new StringBuilder("ab");
    for (int i = 0; i < 20; i++)
    {
      char[] content = buf.toString().toCharArray();
      t.setTermBuffer(content, 0, content.length);
      assertEquals(buf.length(), t.termLength());
      assertEquals(buf.toString(), t.term());
      buf.append(buf.toString());
    }
    assertEquals(1048576, t.termLength());
    t = new TermAttributeImpl();
    buf = new StringBuilder("ab");
    for (int i = 0; i < 20; i++)
    {
      String content = buf.toString();
      t.setTermBuffer(content, 0, content.length());
      assertEquals(content.length(), t.termLength());
      assertEquals(content, t.term());
      buf.append(content);
    }
    assertEquals(1048576, t.termLength());
    t = new TermAttributeImpl();
    buf = new StringBuilder("ab");
    for (int i = 0; i < 20; i++)
    {
      String content = buf.toString();
      t.setTermBuffer(content);
      assertEquals(content.length(), t.termLength());
      assertEquals(content, t.term());
      buf.append(content);
    }
    assertEquals(1048576, t.termLength());
    t = new TermAttributeImpl();
    buf = new StringBuilder("a");
    for (int i = 0; i < 20000; i++)
    {
      String content = buf.toString();
      t.setTermBuffer(content);
      assertEquals(content.length(), t.termLength());
      assertEquals(content, t.term());
      buf.append("a");
    }
    assertEquals(20000, t.termLength());
    t = new TermAttributeImpl();
    buf = new StringBuilder("a");
    for (int i = 0; i < 20000; i++)
    {
      String content = buf.toString();
      t.setTermBuffer(content);
      assertEquals(content.length(), t.termLength());
      assertEquals(content, t.term());
      buf.append("a");
    }
    assertEquals(20000, t.termLength());
  }
  public void testToString() throws Exception {
    char[] b = {'a', 'l', 'o', 'h', 'a'};
    TermAttributeImpl t = new TermAttributeImpl();
    t.setTermBuffer(b, 0, 5);
    assertEquals("term=aloha", t.toString());
    t.setTermBuffer("hi there");
    assertEquals("term=hi there", t.toString());
  }
  public void testMixedStringArray() throws Exception {
    TermAttributeImpl t = new TermAttributeImpl();
    t.setTermBuffer("hello");
    assertEquals(t.termLength(), 5);
    assertEquals(t.term(), "hello");
    t.setTermBuffer("hello2");
    assertEquals(t.termLength(), 6);
    assertEquals(t.term(), "hello2");
    t.setTermBuffer("hello3".toCharArray(), 0, 6);
    assertEquals(t.term(), "hello3");
    char[] buffer = t.termBuffer();
    buffer[1] = 'o';
    assertEquals(t.term(), "hollo3");
  }
  public void testClone() throws Exception {
    TermAttributeImpl t = new TermAttributeImpl();
    char[] content = "hello".toCharArray();
    t.setTermBuffer(content, 0, 5);
    char[] buf = t.termBuffer();
    TermAttributeImpl copy = (TermAttributeImpl) TestSimpleAttributeImpls.assertCloneIsEqual(t);
    assertEquals(t.term(), copy.term());
    assertNotSame(buf, copy.termBuffer());
  }
  public void testEquals() throws Exception {
    TermAttributeImpl t1a = new TermAttributeImpl();
    char[] content1a = "hello".toCharArray();
    t1a.setTermBuffer(content1a, 0, 5);
    TermAttributeImpl t1b = new TermAttributeImpl();
    char[] content1b = "hello".toCharArray();
    t1b.setTermBuffer(content1b, 0, 5);
    TermAttributeImpl t2 = new TermAttributeImpl();
    char[] content2 = "hello2".toCharArray();
    t2.setTermBuffer(content2, 0, 6);
    assertTrue(t1a.equals(t1b));
    assertFalse(t1a.equals(t2));
    assertFalse(t2.equals(t1b));
  }
  public void testCopyTo() throws Exception {
    TermAttributeImpl t = new TermAttributeImpl();
    TermAttributeImpl copy = (TermAttributeImpl) TestSimpleAttributeImpls.assertCopyIsEqual(t);
    assertEquals("", t.term());
    assertEquals("", copy.term());
    t = new TermAttributeImpl();
    char[] content = "hello".toCharArray();
    t.setTermBuffer(content, 0, 5);
    char[] buf = t.termBuffer();
    copy = (TermAttributeImpl) TestSimpleAttributeImpls.assertCopyIsEqual(t);
    assertEquals(t.term(), copy.term());
    assertNotSame(buf, copy.termBuffer());
  }
}
