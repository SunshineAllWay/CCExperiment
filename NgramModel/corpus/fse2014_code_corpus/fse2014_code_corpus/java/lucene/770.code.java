package org.apache.lucene.analysis.in;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestIndicNormalizer extends BaseTokenStreamTestCase {
  public void testBasics() throws IOException {
    check("अाॅअाॅ", "ऑऑ");
    check("अाॆअाॆ", "ऒऒ");
    check("अाेअाे", "ओओ");
    check("अाैअाै", "औऔ");
    check("अाअा", "आआ");
    check("अाैर", "और");
    check("ত্‍", "ৎ");
  }
  private void check(String input, String output) throws IOException {
    Tokenizer tokenizer = new WhitespaceTokenizer(TEST_VERSION_CURRENT, 
        new StringReader(input));
    TokenFilter tf = new IndicNormalizationFilter(tokenizer);
    assertTokenStreamContents(tf, new String[] { output });
  }
}
