package org.apache.lucene.analysis.sv;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
public class TestSwedishAnalyzer extends BaseTokenStreamTestCase {
  public void testResourcesAvailable() {
    new SwedishAnalyzer(TEST_VERSION_CURRENT);
  }
  public void testBasics() throws IOException {
    Analyzer a = new SwedishAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "jaktkarlarne", "jaktkarl");
    checkOneTermReuse(a, "jaktkarlens", "jaktkarl");
    assertAnalyzesTo(a, "och", new String[] {});
  }
  public void testExclude() throws IOException {
    Set<String> exclusionSet = new HashSet<String>();
    exclusionSet.add("jaktkarlarne");
    Analyzer a = new SwedishAnalyzer(TEST_VERSION_CURRENT, 
        SwedishAnalyzer.getDefaultStopSet(), exclusionSet);
    checkOneTermReuse(a, "jaktkarlarne", "jaktkarlarne");
    checkOneTermReuse(a, "jaktkarlens", "jaktkarl");
  }
}
