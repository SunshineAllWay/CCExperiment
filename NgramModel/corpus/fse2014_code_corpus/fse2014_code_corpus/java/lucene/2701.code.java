package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestCzechStemFilterFactory extends BaseTokenTestCase {
  public void testStemming() throws Exception {
    Reader reader = new StringReader("angličtí");
    Tokenizer tokenizer = new WhitespaceTokenizer(reader);
    CzechStemFilterFactory factory = new CzechStemFilterFactory();
    TokenStream stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "anglick" });
  }
}
