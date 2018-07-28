package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestPersianNormalizationFilterFactory extends BaseTokenTestCase {
  public void testNormalization() throws Exception {
    Reader reader = new StringReader("های");
    Tokenizer tokenizer = new WhitespaceTokenizer(reader);
    PersianNormalizationFilterFactory factory = new PersianNormalizationFilterFactory();
    TokenStream stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "هاي" });
  }
}
