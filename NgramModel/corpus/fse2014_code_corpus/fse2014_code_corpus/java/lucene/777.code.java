package org.apache.lucene.analysis.miscellaneous;
import java.io.IOException;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public class TestSingleTokenTokenFilter extends LuceneTestCase {
  public void test() throws IOException {
    Token token = new Token();
    SingleTokenTokenStream ts = new SingleTokenTokenStream(token);
    AttributeImpl tokenAtt = (AttributeImpl) ts.addAttribute(TermAttribute.class);
    assertTrue(tokenAtt instanceof Token);
    ts.reset();
    assertTrue(ts.incrementToken());
    assertEquals(token, tokenAtt);
    assertFalse(ts.incrementToken());
    token = new Token("hallo", 10, 20, "someType");
    ts.setToken(token);
    ts.reset();
    assertTrue(ts.incrementToken());
    assertEquals(token, tokenAtt);
    assertFalse(ts.incrementToken());
  }
}
