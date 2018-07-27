package org.apache.lucene.analysis.miscellaneous;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
public class TestStemmerOverrideFilter extends BaseTokenStreamTestCase {
  public void testOverride() throws IOException {
    Map<String,String> dictionary = new HashMap<String,String>();
    dictionary.put("booked", "books");
    Tokenizer tokenizer = new KeywordTokenizer(new StringReader("booked"));
    TokenStream stream = new PorterStemFilter(
        new StemmerOverrideFilter(TEST_VERSION_CURRENT, tokenizer, dictionary));
    assertTokenStreamContents(stream, new String[] { "books" });
  }
}
