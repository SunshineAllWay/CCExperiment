package org.apache.lucene.analysis.it;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
public class TestItalianAnalyzer extends BaseTokenStreamTestCase {
  public void testResourcesAvailable() {
    new ItalianAnalyzer(TEST_VERSION_CURRENT);
  }
  public void testBasics() throws IOException {
    Analyzer a = new ItalianAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "abbandonata", "abbandon");
    checkOneTermReuse(a, "abbandonati", "abbandon");
    assertAnalyzesTo(a, "dallo", new String[] {});
  }
  public void testExclude() throws IOException {
    Set<String> exclusionSet = new HashSet<String>();
    exclusionSet.add("abbandonata");
    Analyzer a = new ItalianAnalyzer(TEST_VERSION_CURRENT, 
        ItalianAnalyzer.getDefaultStopSet(), exclusionSet);
    checkOneTermReuse(a, "abbandonata", "abbandonata");
    checkOneTermReuse(a, "abbandonati", "abbandon");
  }
}
