package org.apache.lucene.analysis.ar;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.CharArraySet;
public class TestArabicAnalyzer extends BaseTokenStreamTestCase {
  public void testResourcesAvailable() {
    new ArabicAnalyzer(TEST_VERSION_CURRENT);
  }
  public void testBasicFeatures() throws Exception {
    ArabicAnalyzer a = new ArabicAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(a, "كبير", new String[] { "كبير" });
    assertAnalyzesTo(a, "كبيرة", new String[] { "كبير" }); 
    assertAnalyzesTo(a, "مشروب", new String[] { "مشروب" });
    assertAnalyzesTo(a, "مشروبات", new String[] { "مشروب" }); 
    assertAnalyzesTo(a, "أمريكيين", new String[] { "امريك" }); 
    assertAnalyzesTo(a, "امريكي", new String[] { "امريك" }); 
    assertAnalyzesTo(a, "كتاب", new String[] { "كتاب" }); 
    assertAnalyzesTo(a, "الكتاب", new String[] { "كتاب" }); 
    assertAnalyzesTo(a, "ما ملكت أيمانكم", new String[] { "ملكت", "ايمانكم"});
    assertAnalyzesTo(a, "الذين ملكت أيمانكم", new String[] { "ملكت", "ايمانكم" }); 
  }
  public void testReusableTokenStream() throws Exception {
    ArabicAnalyzer a = new ArabicAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesToReuse(a, "كبير", new String[] { "كبير" });
    assertAnalyzesToReuse(a, "كبيرة", new String[] { "كبير" }); 
  }
  public void testEnglishInput() throws Exception {
    assertAnalyzesTo(new ArabicAnalyzer(TEST_VERSION_CURRENT), "English text.", new String[] {
        "english", "text" });
  }
  public void testCustomStopwords() throws Exception {
    Set<String> set = new HashSet<String>();
    Collections.addAll(set, "the", "and", "a");
    ArabicAnalyzer a = new ArabicAnalyzer(TEST_VERSION_CURRENT, set);
    assertAnalyzesTo(a, "The quick brown fox.", new String[] { "quick",
        "brown", "fox" });
  }
  public void testWithStemExclusionSet() throws IOException {
    Set<String> set = new HashSet<String>();
    set.add("ساهدهات");
    ArabicAnalyzer a = new ArabicAnalyzer(TEST_VERSION_CURRENT, CharArraySet.EMPTY_SET, set);
    assertAnalyzesTo(a, "كبيرة the quick ساهدهات", new String[] { "كبير","the", "quick", "ساهدهات" });
    assertAnalyzesToReuse(a, "كبيرة the quick ساهدهات", new String[] { "كبير","the", "quick", "ساهدهات" });
    a = new ArabicAnalyzer(TEST_VERSION_CURRENT, CharArraySet.EMPTY_SET, CharArraySet.EMPTY_SET);
    assertAnalyzesTo(a, "كبيرة the quick ساهدهات", new String[] { "كبير","the", "quick", "ساهد" });
    assertAnalyzesToReuse(a, "كبيرة the quick ساهدهات", new String[] { "كبير","the", "quick", "ساهد" });
  }
}
