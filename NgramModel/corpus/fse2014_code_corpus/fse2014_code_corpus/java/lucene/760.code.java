package org.apache.lucene.analysis.es;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
public class TestSpanishAnalyzer extends BaseTokenStreamTestCase {
  public void testResourcesAvailable() {
    new SpanishAnalyzer(TEST_VERSION_CURRENT);
  }
  public void testBasics() throws IOException {
    Analyzer a = new SpanishAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "chicana", "chican");
    checkOneTermReuse(a, "chicano", "chican");
    assertAnalyzesTo(a, "los", new String[] {});
  }
  public void testExclude() throws IOException {
    Set<String> exclusionSet = new HashSet<String>();
    exclusionSet.add("chicano");
    Analyzer a = new SpanishAnalyzer(TEST_VERSION_CURRENT, 
        SpanishAnalyzer.getDefaultStopSet(), exclusionSet);
    checkOneTermReuse(a, "chicana", "chican");
    checkOneTermReuse(a, "chicano", "chicano");
  }
}
