package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestThaiWordFilterFactory extends BaseTokenTestCase {
  public void testWordBreak() throws Exception {
    Reader reader = new StringReader("การที่ได้ต้องแสดงว่างานดี");
    Tokenizer tokenizer = new WhitespaceTokenizer(reader);
    ThaiWordFilterFactory factory = new ThaiWordFilterFactory();
    TokenStream stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] {"การ", "ที่", "ได้",
        "ต้อง", "แสดง", "ว่า", "งาน", "ดี"});
  }
}
