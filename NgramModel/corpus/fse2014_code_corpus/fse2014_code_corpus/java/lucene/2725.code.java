package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestReverseStringFilterFactory extends BaseTokenTestCase {
  public void testReversing() throws Exception {
    Reader reader = new StringReader("simple test");
    Tokenizer tokenizer = new WhitespaceTokenizer(reader);
    ReverseStringFilterFactory factory = new ReverseStringFilterFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    TokenStream stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "elpmis", "tset" });
  }
}
