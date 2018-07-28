package org.apache.lucene.analysis.tr;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
public class TestTurkishAnalyzer extends BaseTokenStreamTestCase {
  public void testResourcesAvailable() {
    new TurkishAnalyzer(TEST_VERSION_CURRENT);
  }
  public void testBasics() throws IOException {
    Analyzer a = new TurkishAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "ağacı", "ağaç");
    checkOneTermReuse(a, "ağaç", "ağaç");
    assertAnalyzesTo(a, "dolayı", new String[] {});
  }
  public void testExclude() throws IOException {
    Set<String> exclusionSet = new HashSet<String>();
    exclusionSet.add("ağacı");
    Analyzer a = new TurkishAnalyzer(TEST_VERSION_CURRENT, 
        TurkishAnalyzer.getDefaultStopSet(), exclusionSet);
    checkOneTermReuse(a, "ağacı", "ağacı");
    checkOneTermReuse(a, "ağaç", "ağaç");
  }
}
