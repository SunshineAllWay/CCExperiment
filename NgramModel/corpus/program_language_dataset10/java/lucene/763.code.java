package org.apache.lucene.analysis.fi;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
public class TestFinnishAnalyzer extends BaseTokenStreamTestCase {
  public void testResourcesAvailable() {
    new FinnishAnalyzer(TEST_VERSION_CURRENT);
  }
  public void testBasics() throws IOException {
    Analyzer a = new FinnishAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "edeltäjiinsä", "edeltäj");
    checkOneTermReuse(a, "edeltäjistään", "edeltäj");
    assertAnalyzesTo(a, "olla", new String[] {});
  }
  public void testExclude() throws IOException {
    Set<String> exclusionSet = new HashSet<String>();
    exclusionSet.add("edeltäjistään");
    Analyzer a = new FinnishAnalyzer(TEST_VERSION_CURRENT, 
        FinnishAnalyzer.getDefaultStopSet(), exclusionSet);
    checkOneTermReuse(a, "edeltäjiinsä", "edeltäj");
    checkOneTermReuse(a, "edeltäjistään", "edeltäjistään");
  }
}
