package org.apache.lucene.analysis.fr;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public class TestElision extends BaseTokenStreamTestCase {
  public void testElision() throws Exception {
    String test = "Plop, juste pour voir l'embrouille avec O'brian. M'enfin.";
    Tokenizer tokenizer = new StandardTokenizer(TEST_VERSION_CURRENT, new StringReader(test));
    Set<String> articles = new HashSet<String>();
    articles.add("l");
    articles.add("M");
    TokenFilter filter = new ElisionFilter(TEST_VERSION_CURRENT, tokenizer, articles);
    List<String> tas = filter(filter);
    assertEquals("embrouille", tas.get(4));
    assertEquals("O'brian", tas.get(6));
    assertEquals("enfin", tas.get(7));
  }
  private List<String> filter(TokenFilter filter) throws IOException {
    List<String> tas = new ArrayList<String>();
    TermAttribute termAtt = filter.getAttribute(TermAttribute.class);
    while (filter.incrementToken()) {
      tas.add(termAtt.term());
    }
    return tas;
  }
}
