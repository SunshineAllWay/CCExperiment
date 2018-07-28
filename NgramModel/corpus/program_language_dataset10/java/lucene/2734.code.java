package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestTurkishLowerCaseFilterFactory extends BaseTokenTestCase {
  public void testCasing() throws Exception {
    Reader reader = new StringReader("AĞACI");
    Tokenizer tokenizer = new WhitespaceTokenizer(reader);
    TurkishLowerCaseFilterFactory factory = new TurkishLowerCaseFilterFactory();
    TokenStream stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "ağacı" });
  }
}
