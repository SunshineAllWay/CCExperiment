package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
public class TestChineseTokenizerFactory extends BaseTokenTestCase {
  public void testTokenizer() throws Exception {
    Reader reader = new StringReader("我是中国人");
    ChineseTokenizerFactory factory = new ChineseTokenizerFactory();
    TokenStream stream = factory.create(reader);
    assertTokenStreamContents(stream, new String[] {"我", "是", "中", "国", "人"});
  }
}
