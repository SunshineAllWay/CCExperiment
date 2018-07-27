package org.apache.lucene.search;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
public class TestQueryTermVector extends LuceneTestCase {
  public TestQueryTermVector(String s) {
    super(s);
  }
  public void testConstructor() {
    String [] queryTerm = {"foo", "bar", "foo", "again", "foo", "bar", "go", "go", "go"};
    String [] gold = {"again", "bar", "foo", "go"};
    int [] goldFreqs = {1, 2, 3, 3};
    QueryTermVector result = new QueryTermVector(queryTerm);
    String [] terms = result.getTerms();
    assertTrue(terms.length == 4);
    int [] freq = result.getTermFrequencies();
    assertTrue(freq.length == 4);
    checkGold(terms, gold, freq, goldFreqs);
    result = new QueryTermVector(null);
    assertTrue(result.getTerms().length == 0);
    result = new QueryTermVector("foo bar foo again foo bar go go go", new WhitespaceAnalyzer(TEST_VERSION_CURRENT));
    terms = result.getTerms();
    assertTrue(terms.length == 4);
    freq = result.getTermFrequencies();
    assertTrue(freq.length == 4);
    checkGold(terms, gold, freq, goldFreqs);
  }
  private void checkGold(String[] terms, String[] gold, int[] freq, int[] goldFreqs) {
    for (int i = 0; i < terms.length; i++) {
      assertTrue(terms[i].equals(gold[i]));
      assertTrue(freq[i] == goldFreqs[i]);
    }
  }
}
