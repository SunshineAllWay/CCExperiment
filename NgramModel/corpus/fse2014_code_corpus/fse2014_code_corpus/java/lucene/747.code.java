package org.apache.lucene.analysis.bg;
import java.io.IOException;
import java.util.Collections;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.util.Version;
public class TestBulgarianAnalyzer extends BaseTokenStreamTestCase {
  public void testResourcesAvailable() {
    new BulgarianAnalyzer(TEST_VERSION_CURRENT);
  }
  public void testStopwords() throws IOException {
    Analyzer a = new BulgarianAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(a, "Как се казваш?", new String[] {"казваш"});
  }
  public void testCustomStopwords() throws IOException {
    Analyzer a = new BulgarianAnalyzer(TEST_VERSION_CURRENT, Collections
        .emptySet());
    assertAnalyzesTo(a, "Как се казваш?", 
        new String[] {"как", "се", "казваш"});
  }
  public void testReusableTokenStream() throws IOException {
    Analyzer a = new BulgarianAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesToReuse(a, "документи", new String[] {"документ"});
    assertAnalyzesToReuse(a, "документ", new String[] {"документ"});
  }
  public void testBasicExamples() throws IOException {
    Analyzer a = new BulgarianAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(a, "енергийни кризи", new String[] {"енергийн", "криз"});
    assertAnalyzesTo(a, "Атомната енергия", new String[] {"атомн", "енерг"});
    assertAnalyzesTo(a, "компютри", new String[] {"компютр"});
    assertAnalyzesTo(a, "компютър", new String[] {"компютр"});
    assertAnalyzesTo(a, "градове", new String[] {"град"});
  }
  public void testWithStemExclusionSet() throws IOException {
    CharArraySet set = new CharArraySet(Version.LUCENE_31, 1, true);
    set.add("строеве");
    Analyzer a = new BulgarianAnalyzer(TEST_VERSION_CURRENT, CharArraySet.EMPTY_SET, set);
    assertAnalyzesTo(a, "строевете строеве", new String[] { "строй", "строеве" });
  }
}
