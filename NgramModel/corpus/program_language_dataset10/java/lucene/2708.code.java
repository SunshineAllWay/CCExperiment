package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestGreekLowerCaseFilterFactory extends BaseTokenTestCase {
  public void testStemming() throws Exception {
    Reader reader = new StringReader("Μάϊος ΜΆΪΟΣ");
    Tokenizer tokenizer = new WhitespaceTokenizer(reader);
    GreekLowerCaseFilterFactory factory = new GreekLowerCaseFilterFactory();
    TokenStream stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "μαιοσ", "μαιοσ" });
  }
}
