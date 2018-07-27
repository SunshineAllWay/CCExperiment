package org.apache.lucene.analysis.pt;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
public class TestPortugueseAnalyzer extends BaseTokenStreamTestCase {
  public void testResourcesAvailable() {
    new PortugueseAnalyzer(TEST_VERSION_CURRENT);
  }
  public void testBasics() throws IOException {
    Analyzer a = new PortugueseAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "quilométricas", "quilométr");
    checkOneTermReuse(a, "quilométricos", "quilométr");
    assertAnalyzesTo(a, "não", new String[] {});
  }
  public void testExclude() throws IOException {
    Set<String> exclusionSet = new HashSet<String>();
    exclusionSet.add("quilométricas");
    Analyzer a = new PortugueseAnalyzer(TEST_VERSION_CURRENT, 
        PortugueseAnalyzer.getDefaultStopSet(), exclusionSet);
    checkOneTermReuse(a, "quilométricas", "quilométricas");
    checkOneTermReuse(a, "quilométricos", "quilométr");
  }
}
