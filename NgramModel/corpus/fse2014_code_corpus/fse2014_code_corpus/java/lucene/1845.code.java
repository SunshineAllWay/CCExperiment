package org.apache.lucene.analysis;
import java.io.StringReader;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public class TestPerFieldAnalzyerWrapper extends BaseTokenStreamTestCase {
  public void testPerField() throws Exception {
    String text = "Qwerty";
    PerFieldAnalyzerWrapper analyzer =
              new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(TEST_VERSION_CURRENT));
    analyzer.addAnalyzer("special", new SimpleAnalyzer(TEST_VERSION_CURRENT));
    TokenStream tokenStream = analyzer.tokenStream("field",
                                            new StringReader(text));
    TermAttribute termAtt = tokenStream.getAttribute(TermAttribute.class);
    assertTrue(tokenStream.incrementToken());
    assertEquals("WhitespaceAnalyzer does not lowercase",
                 "Qwerty",
                 termAtt.term());
    tokenStream = analyzer.tokenStream("special",
                                            new StringReader(text));
    termAtt = tokenStream.getAttribute(TermAttribute.class);
    assertTrue(tokenStream.incrementToken());
    assertEquals("SimpleAnalyzer lowercases",
                 "qwerty",
                 termAtt.term());
  }
}
