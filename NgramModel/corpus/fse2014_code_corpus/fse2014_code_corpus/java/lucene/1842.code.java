package org.apache.lucene.analysis;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import java.io.StringReader;
public class TestLengthFilter extends BaseTokenStreamTestCase {
  public void testFilter() throws Exception {
    TokenStream stream = new WhitespaceTokenizer(TEST_VERSION_CURRENT, 
        new StringReader("short toolong evenmuchlongertext a ab toolong foo"));
    LengthFilter filter = new LengthFilter(stream, 2, 6);
    TermAttribute termAtt = filter.getAttribute(TermAttribute.class);
    assertTrue(filter.incrementToken());
    assertEquals("short", termAtt.term());
    assertTrue(filter.incrementToken());
    assertEquals("ab", termAtt.term());
    assertTrue(filter.incrementToken());
    assertEquals("foo", termAtt.term());
    assertFalse(filter.incrementToken());
  }
}
