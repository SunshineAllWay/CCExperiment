package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestChineseFilterFactory extends BaseTokenTestCase {
  public void testFiltering() throws Exception {
    Reader reader = new StringReader("this 1234 Is such a silly filter");
    Tokenizer tokenizer = new WhitespaceTokenizer(reader);
    ChineseFilterFactory factory = new ChineseFilterFactory();
    TokenStream stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "Is", "silly", "filter" });
  }
}
