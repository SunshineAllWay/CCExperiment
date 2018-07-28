package org.apache.lucene.analysis.en;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
public class TestEnglishAnalyzer extends BaseTokenStreamTestCase {
  public void testResourcesAvailable() {
    new EnglishAnalyzer(TEST_VERSION_CURRENT);
  }
  public void testBasics() throws IOException {
    Analyzer a = new EnglishAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "books", "book");
    checkOneTermReuse(a, "book", "book");
    assertAnalyzesTo(a, "the", new String[] {});
  }
  public void testExclude() throws IOException {
    Set<String> exclusionSet = new HashSet<String>();
    exclusionSet.add("books");
    Analyzer a = new EnglishAnalyzer(TEST_VERSION_CURRENT, 
        EnglishAnalyzer.getDefaultStopSet(), exclusionSet);
    checkOneTermReuse(a, "books", "books");
    checkOneTermReuse(a, "book", "book");
  }
}
