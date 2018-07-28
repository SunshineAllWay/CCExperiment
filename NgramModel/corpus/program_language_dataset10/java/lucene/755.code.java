package org.apache.lucene.analysis.da;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
public class TestDanishAnalyzer extends BaseTokenStreamTestCase {
  public void testResourcesAvailable() {
    new DanishAnalyzer(TEST_VERSION_CURRENT);
  }
  public void testBasics() throws IOException {
    Analyzer a = new DanishAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "undersøg", "undersøg");
    checkOneTermReuse(a, "undersøgelse", "undersøg");
    assertAnalyzesTo(a, "på", new String[] {});
  }
  public void testExclude() throws IOException {
    Set<String> exclusionSet = new HashSet<String>();
    exclusionSet.add("undersøgelse");
    Analyzer a = new DanishAnalyzer(TEST_VERSION_CURRENT, 
        DanishAnalyzer.getDefaultStopSet(), exclusionSet);
    checkOneTermReuse(a, "undersøgelse", "undersøgelse");
    checkOneTermReuse(a, "undersøg", "undersøg");
  }
}
