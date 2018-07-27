package org.apache.lucene.analysis.hu;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
public class TestHungarianAnalyzer extends BaseTokenStreamTestCase {
  public void testResourcesAvailable() {
    new HungarianAnalyzer(TEST_VERSION_CURRENT);
  }
  public void testBasics() throws IOException {
    Analyzer a = new HungarianAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "babakocsi", "babakocs");
    checkOneTermReuse(a, "babakocsijáért", "babakocs");
    assertAnalyzesTo(a, "által", new String[] {});
  }
  public void testExclude() throws IOException {
    Set<String> exclusionSet = new HashSet<String>();
    exclusionSet.add("babakocsi");
    Analyzer a = new HungarianAnalyzer(TEST_VERSION_CURRENT, 
        HungarianAnalyzer.getDefaultStopSet(), exclusionSet);
    checkOneTermReuse(a, "babakocsi", "babakocsi");
    checkOneTermReuse(a, "babakocsijáért", "babakocs");
  }
}
