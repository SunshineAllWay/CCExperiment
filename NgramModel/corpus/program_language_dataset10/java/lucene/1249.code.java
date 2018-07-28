package org.apache.lucene.search.regex;
import junit.framework.TestCase;
public class TestJakartaRegexpCapabilities extends TestCase {
  public void testGetPrefix(){
    JakartaRegexpCapabilities cap = new JakartaRegexpCapabilities();
    cap.compile("luc[e]?");
    assertTrue(cap.match("luce"));
    assertEquals("luc", cap.prefix());
    cap.compile("lucene");
    assertTrue(cap.match("lucene"));
    assertEquals("lucene", cap.prefix());
  }
  public void testShakyPrefix(){
    JakartaRegexpCapabilities cap = new JakartaRegexpCapabilities();
    cap.compile("(ab|ac)");
    assertTrue(cap.match("ab"));
    assertTrue(cap.match("ac"));
    assertNull(cap.prefix());
  }
}
