package org.apache.lucene.analysis.ro;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
public class TestRomanianAnalyzer extends BaseTokenStreamTestCase {
  public void testResourcesAvailable() {
    new RomanianAnalyzer(TEST_VERSION_CURRENT);
  }
  public void testBasics() throws IOException {
    Analyzer a = new RomanianAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "absenţa", "absenţ");
    checkOneTermReuse(a, "absenţi", "absenţ");
    assertAnalyzesTo(a, "îl", new String[] {});
  }
  public void testExclude() throws IOException {
    Set<String> exclusionSet = new HashSet<String>();
    exclusionSet.add("absenţa");
    Analyzer a = new RomanianAnalyzer(TEST_VERSION_CURRENT, 
        RomanianAnalyzer.getDefaultStopSet(), exclusionSet);
    checkOneTermReuse(a, "absenţa", "absenţa");
    checkOneTermReuse(a, "absenţi", "absenţ");
  }
}
