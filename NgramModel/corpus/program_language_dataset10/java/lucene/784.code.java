package org.apache.lucene.analysis.no;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
public class TestNorwegianAnalyzer extends BaseTokenStreamTestCase {
  public void testResourcesAvailable() {
    new NorwegianAnalyzer(TEST_VERSION_CURRENT);
  }
  public void testBasics() throws IOException {
    Analyzer a = new NorwegianAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "havnedistriktene", "havnedistrikt");
    checkOneTermReuse(a, "havnedistrikter", "havnedistrikt");
    assertAnalyzesTo(a, "det", new String[] {});
  }
  public void testExclude() throws IOException {
    Set<String> exclusionSet = new HashSet<String>();
    exclusionSet.add("havnedistriktene");
    Analyzer a = new NorwegianAnalyzer(TEST_VERSION_CURRENT, 
        NorwegianAnalyzer.getDefaultStopSet(), exclusionSet);
    checkOneTermReuse(a, "havnedistriktene", "havnedistriktene");
    checkOneTermReuse(a, "havnedistrikter", "havnedistrikt");
  }
}
