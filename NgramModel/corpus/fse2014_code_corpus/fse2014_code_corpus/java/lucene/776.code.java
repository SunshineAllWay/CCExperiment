package org.apache.lucene.analysis.miscellaneous;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import java.io.IOException;
import java.io.StringReader;
public class TestPrefixAwareTokenFilter extends BaseTokenStreamTestCase {
  public void test() throws IOException {
    PrefixAwareTokenFilter ts;
    ts = new PrefixAwareTokenFilter(
        new SingleTokenTokenStream(createToken("a", 0, 1)),
        new SingleTokenTokenStream(createToken("b", 0, 1)));
    assertTokenStreamContents(ts, 
        new String[] { "a", "b" },
        new int[] { 0, 1 },
        new int[] { 1, 2 });
    ts = new PrefixAwareTokenFilter(new SingleTokenTokenStream(createToken("^", 0, 0)),
        new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader("hello world")));
    ts = new PrefixAwareTokenFilter(ts, new SingleTokenTokenStream(createToken("$", 0, 0)));
    assertTokenStreamContents(ts,
        new String[] { "^", "hello", "world", "$" },
        new int[] { 0, 0, 6, 11 },
        new int[] { 0, 5, 11, 11 });
  }
  private static Token createToken(String term, int start, int offset)
  {
    Token token = new Token(start, offset);
    token.setTermBuffer(term);
    return token;
  }
}
