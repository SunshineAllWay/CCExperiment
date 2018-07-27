package org.apache.solr.analysis;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class DoubleMetaphoneFilterTest extends BaseTokenTestCase {
  public void testSize4FalseInject() throws Exception {
    TokenStream stream = new WhitespaceTokenizer(new StringReader("international"));
    TokenStream filter = new DoubleMetaphoneFilter(stream, 4, false);
    assertTokenStreamContents(filter, new String[] { "ANTR" });
  }
  public void testSize4TrueInject() throws Exception {
    TokenStream stream = new WhitespaceTokenizer(new StringReader("international"));
    TokenStream filter = new DoubleMetaphoneFilter(stream, 4, true);
    assertTokenStreamContents(filter, new String[] { "international", "ANTR" });
  }
  public void testAlternateInjectFalse() throws Exception {
    TokenStream stream = new WhitespaceTokenizer(new StringReader("Kuczewski"));
    TokenStream filter = new DoubleMetaphoneFilter(stream, 4, false);
    assertTokenStreamContents(filter, new String[] { "KSSK", "KXFS" });
  }
  public void testSize8FalseInject() throws Exception {
    TokenStream stream = new WhitespaceTokenizer(new StringReader("international"));
    TokenStream filter = new DoubleMetaphoneFilter(stream, 8, false);
    assertTokenStreamContents(filter, new String[] { "ANTRNXNL" });
  }
  public void testNonConvertableStringsWithInject() throws Exception {
    TokenStream stream = new WhitespaceTokenizer(new StringReader("12345 #$%@#^%&"));
    TokenStream filter = new DoubleMetaphoneFilter(stream, 8, true);
    assertTokenStreamContents(filter, new String[] { "12345", "#$%@#^%&" });
  }
  public void testNonConvertableStringsWithoutInject() throws Exception {
    TokenStream stream = new WhitespaceTokenizer(new StringReader("12345 #$%@#^%&"));
    TokenStream filter = new DoubleMetaphoneFilter(stream, 8, false);
    assertTokenStreamContents(filter, new String[] { "12345", "#$%@#^%&" });
    stream = new WhitespaceTokenizer(new StringReader("12345 #$%@#^%& hello"));
    filter = new DoubleMetaphoneFilter(stream, 8, false);
    assertTokenStreamContents(filter, new String[] { "12345", "#$%@#^%&", "HL" });
  }
}
