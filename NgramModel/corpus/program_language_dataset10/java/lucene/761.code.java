package org.apache.lucene.analysis.fa;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.Analyzer;
public class TestPersianAnalyzer extends BaseTokenStreamTestCase {
  public void testResourcesAvailable() {
    new PersianAnalyzer(TEST_VERSION_CURRENT);
  }
  public void testBehaviorVerbs() throws Exception {
    Analyzer a = new PersianAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(a, "می‌خورد", new String[] { "خورد" });
    assertAnalyzesTo(a, "خورد", new String[] { "خورد" });
    assertAnalyzesTo(a, "می‌خورد", new String[] { "خورد" });
    assertAnalyzesTo(a, "خواهد خورد", new String[] { "خورد" });
    assertAnalyzesTo(a, "دارد می‌خورد", new String[] { "خورد" });
    assertAnalyzesTo(a, "داشت می‌خورد", new String[] { "خورد" });
    assertAnalyzesTo(a, "خورده‌است", new String[] { "خورده" });
    assertAnalyzesTo(a, "می‌خورده‌است", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده بود", new String[] { "خورده" });
    assertAnalyzesTo(a, "می‌خورده بود", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده باشد", new String[] { "خورده" });
    assertAnalyzesTo(a, "می‌خورده باشد", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده بوده باشد", new String[] { "خورده" });
    assertAnalyzesTo(a, "می‌خورده بوده باشد", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده می‌شود", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده شد", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده می‌شد", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده شده‌است", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده می‌شده‌است", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده شده بود", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده می‌شده بود", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده خواهد شد", new String[] { "خورده" });
    assertAnalyzesTo(a, "دارد خورده می‌شود", new String[] { "خورده" });
    assertAnalyzesTo(a, "داشت خورده می‌شد", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده شود", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده شده باشد", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده می‌شده باشد", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده شده بوده باشد", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده می‌شده بوده باشد", new String[] { "خورده" });
    assertAnalyzesTo(a, "بخورد", new String[] { "بخورد" });
  }
  public void testBehaviorVerbsDefective() throws Exception {
    Analyzer a = new PersianAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(a, "مي خورد", new String[] { "خورد" });
    assertAnalyzesTo(a, "خورد", new String[] { "خورد" });
    assertAnalyzesTo(a, "مي خورد", new String[] { "خورد" });
    assertAnalyzesTo(a, "خواهد خورد", new String[] { "خورد" });
    assertAnalyzesTo(a, "دارد مي خورد", new String[] { "خورد" });
    assertAnalyzesTo(a, "داشت مي خورد", new String[] { "خورد" });
    assertAnalyzesTo(a, "خورده است", new String[] { "خورده" });
    assertAnalyzesTo(a, "مي خورده است", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده بود", new String[] { "خورده" });
    assertAnalyzesTo(a, "مي خورده بود", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده باشد", new String[] { "خورده" });
    assertAnalyzesTo(a, "مي خورده باشد", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده بوده باشد", new String[] { "خورده" });
    assertAnalyzesTo(a, "مي خورده بوده باشد", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده مي شود", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده شد", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده مي شد", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده شده است", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده مي شده است", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده شده بود", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده مي شده بود", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده خواهد شد", new String[] { "خورده" });
    assertAnalyzesTo(a, "دارد خورده مي شود", new String[] { "خورده" });
    assertAnalyzesTo(a, "داشت خورده مي شد", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده شود", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده شده باشد", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده مي شده باشد", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده شده بوده باشد", new String[] { "خورده" });
    assertAnalyzesTo(a, "خورده مي شده بوده باشد", new String[] { "خورده" });
    assertAnalyzesTo(a, "بخورد", new String[] { "بخورد" });
  }
  public void testBehaviorNouns() throws Exception {
    Analyzer a = new PersianAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(a, "برگ ها", new String[] { "برگ" });
    assertAnalyzesTo(a, "برگ‌ها", new String[] { "برگ" });
  }
  public void testBehaviorNonPersian() throws Exception {
    Analyzer a = new PersianAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(a, "English test.", new String[] { "english", "test" });
  }
  public void testReusableTokenStream() throws Exception {
    Analyzer a = new PersianAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesToReuse(a, "خورده مي شده بوده باشد", new String[] { "خورده" });
    assertAnalyzesToReuse(a, "برگ‌ها", new String[] { "برگ" });
  }
  public void testCustomStopwords() throws Exception {
    PersianAnalyzer a = new PersianAnalyzer(TEST_VERSION_CURRENT, new String[] { "the", "and", "a" });
    assertAnalyzesTo(a, "The quick brown fox.", new String[] { "quick",
        "brown", "fox" });
  }
}
