package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestBulgarianStemFilterFactory extends BaseTokenTestCase {
  public void testStemming() throws Exception {
    Reader reader = new StringReader("компютри");
    Tokenizer tokenizer = new WhitespaceTokenizer(reader);
    BulgarianStemFilterFactory factory = new BulgarianStemFilterFactory();
    TokenStream stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "компютр" });
  }
}
