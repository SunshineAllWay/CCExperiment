package org.apache.lucene.analysis.reverse;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.util.Version;
public class TestReverseStringFilter extends BaseTokenStreamTestCase {
  public void testFilter() throws Exception {
    TokenStream stream = new WhitespaceTokenizer(TEST_VERSION_CURRENT, 
        new StringReader("Do have a nice day"));     
    ReverseStringFilter filter = new ReverseStringFilter(TEST_VERSION_CURRENT, stream);
    TermAttribute text = filter.getAttribute(TermAttribute.class);
    assertTrue(filter.incrementToken());
    assertEquals("oD", text.term());
    assertTrue(filter.incrementToken());
    assertEquals("evah", text.term());
    assertTrue(filter.incrementToken());
    assertEquals("a", text.term());
    assertTrue(filter.incrementToken());
    assertEquals("ecin", text.term());
    assertTrue(filter.incrementToken());
    assertEquals("yad", text.term());
    assertFalse(filter.incrementToken());
  }
  public void testFilterWithMark() throws Exception {
    TokenStream stream = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(
        "Do have a nice day")); 
    ReverseStringFilter filter = new ReverseStringFilter(TEST_VERSION_CURRENT, stream, '\u0001');
    TermAttribute text = filter
        .getAttribute(TermAttribute.class);
    assertTrue(filter.incrementToken());
    assertEquals("\u0001oD", text.term());
    assertTrue(filter.incrementToken());
    assertEquals("\u0001evah", text.term());
    assertTrue(filter.incrementToken());
    assertEquals("\u0001a", text.term());
    assertTrue(filter.incrementToken());
    assertEquals("\u0001ecin", text.term());
    assertTrue(filter.incrementToken());
    assertEquals("\u0001yad", text.term());
    assertFalse(filter.incrementToken());
  }
  public void testReverseString() throws Exception {
    assertEquals( "A", ReverseStringFilter.reverse(TEST_VERSION_CURRENT, "A" ) );
    assertEquals( "BA", ReverseStringFilter.reverse(TEST_VERSION_CURRENT, "AB" ) );
    assertEquals( "CBA", ReverseStringFilter.reverse(TEST_VERSION_CURRENT, "ABC" ) );
  }
  public void testReverseChar() throws Exception {
    char[] buffer = { 'A', 'B', 'C', 'D', 'E', 'F' };
    ReverseStringFilter.reverse(TEST_VERSION_CURRENT, buffer, 2, 3 );
    assertEquals( "ABEDCF", new String( buffer ) );
  }
  public void testBackCompat() throws Exception {
    assertEquals("\uDF05\uD866\uDF05\uD866", ReverseStringFilter.reverse("𩬅𩬅"));
  }
  public void testReverseSupplementary() throws Exception {
    assertEquals("𩬅艱鍟䇹愯瀛", ReverseStringFilter.reverse(TEST_VERSION_CURRENT, "瀛愯䇹鍟艱𩬅"));
    assertEquals("a𩬅艱鍟䇹愯瀛", ReverseStringFilter.reverse(TEST_VERSION_CURRENT, "瀛愯䇹鍟艱𩬅a"));
    assertEquals("fedcba𩬅", ReverseStringFilter.reverse(TEST_VERSION_CURRENT, "𩬅abcdef"));
    assertEquals("fedcba𩬅z", ReverseStringFilter.reverse(TEST_VERSION_CURRENT, "z𩬅abcdef"));
    assertEquals("gfe𩬅dcba", ReverseStringFilter.reverse(TEST_VERSION_CURRENT, "abcd𩬅efg"));
  }
  public void testReverseSupplementaryChar() throws Exception {
    char[] buffer = "abc瀛愯䇹鍟艱𩬅".toCharArray();
    ReverseStringFilter.reverse(TEST_VERSION_CURRENT, buffer, 3, 7);
    assertEquals("abc𩬅艱鍟䇹愯瀛", new String(buffer));
    buffer = "abc瀛愯䇹鍟艱𩬅d".toCharArray();
    ReverseStringFilter.reverse(TEST_VERSION_CURRENT, buffer, 3, 8);
    assertEquals("abcd𩬅艱鍟䇹愯瀛", new String(buffer));
    buffer = "abc𩬅瀛愯䇹鍟艱".toCharArray();
    ReverseStringFilter.reverse(TEST_VERSION_CURRENT, buffer, 3, 7);
    assertEquals("abc艱鍟䇹愯瀛𩬅", new String(buffer));
    buffer = "abcd𩬅瀛愯䇹鍟艱".toCharArray();
    ReverseStringFilter.reverse(TEST_VERSION_CURRENT, buffer, 3, 8);
    assertEquals("abc艱鍟䇹愯瀛𩬅d", new String(buffer));
    buffer = "abc瀛愯𩬅def".toCharArray();
    ReverseStringFilter.reverse(TEST_VERSION_CURRENT, buffer, 3, 7);
    assertEquals("abcfed𩬅愯瀛", new String(buffer));
  }
}
