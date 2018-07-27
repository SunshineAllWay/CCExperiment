package org.apache.lucene.analysis.hi;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
public class TestHindiAnalyzer extends BaseTokenStreamTestCase {
  public void testResourcesAvailable() {
    new HindiAnalyzer(TEST_VERSION_CURRENT);
  }
  public void testBasics() throws Exception {
    Analyzer a = new HindiAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "हिन्दी", "हिंद");
    checkOneTermReuse(a, "हिंदी", "हिंद");
  }
  public void testExclusionSet() throws Exception {
    Set<String> exclusionSet = new HashSet<String>();
    exclusionSet.add("हिंदी");
    Analyzer a = new HindiAnalyzer(TEST_VERSION_CURRENT, 
        HindiAnalyzer.getDefaultStopSet(), exclusionSet);
    checkOneTermReuse(a, "हिंदी", "हिंदी");
  }
}
