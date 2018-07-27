package org.apache.lucene.analysis.cn.smart;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;
public class TestSmartChineseAnalyzer extends BaseTokenStreamTestCase {
  public void testChineseStopWordsDefault() throws Exception {
    Analyzer ca = new SmartChineseAnalyzer(Version.LUCENE_CURRENT); 
    String sentence = "我购买了道具和服装。";
    String result[] = { "我", "购买", "了", "道具", "和", "服装" };
    assertAnalyzesTo(ca, sentence, result);
    ca = new SmartChineseAnalyzer(Version.LUCENE_CURRENT, SmartChineseAnalyzer.getDefaultStopSet());
    assertAnalyzesTo(ca, sentence, result);
  }
  public void testChineseStopWordsDefaultTwoPhrases() throws Exception {
    Analyzer ca = new SmartChineseAnalyzer(Version.LUCENE_CURRENT); 
    String sentence = "我购买了道具和服装。 我购买了道具和服装。";
    String result[] = { "我", "购买", "了", "道具", "和", "服装", "我", "购买", "了", "道具", "和", "服装" };
    assertAnalyzesTo(ca, sentence, result);
  }
  public void testChineseStopWordsDefaultTwoPhrasesIdeoSpace() throws Exception {
    Analyzer ca = new SmartChineseAnalyzer(Version.LUCENE_CURRENT); 
    String sentence = "我购买了道具和服装　我购买了道具和服装。";
    String result[] = { "我", "购买", "了", "道具", "和", "服装", "我", "购买", "了", "道具", "和", "服装" };
    assertAnalyzesTo(ca, sentence, result);
  }
  public void testChineseStopWordsOff() throws Exception {
    Analyzer[] analyzers = new Analyzer[] {
      new SmartChineseAnalyzer(Version.LUCENE_CURRENT, false),
      new SmartChineseAnalyzer(Version.LUCENE_CURRENT, null) };
    String sentence = "我购买了道具和服装。";
    String result[] = { "我", "购买", "了", "道具", "和", "服装", "," };
    for (Analyzer analyzer : analyzers) {
      assertAnalyzesTo(analyzer, sentence, result);
      assertAnalyzesToReuse(analyzer, sentence, result);
    }
  }
  public void testChineseStopWords2() throws Exception {
    Analyzer ca = new SmartChineseAnalyzer(Version.LUCENE_CURRENT); 
    String sentence = "Title:San"; 
    String result[] = { "titl", "san"};
    int startOffsets[] = { 0, 6 };
    int endOffsets[] = { 5, 9 };
    int posIncr[] = { 1, 2 };
    assertAnalyzesTo(ca, sentence, result, startOffsets, endOffsets, posIncr);
  }
  public void testChineseAnalyzer() throws Exception {
    Analyzer ca = new SmartChineseAnalyzer(Version.LUCENE_CURRENT, true);
    String sentence = "我购买了道具和服装。";
    String[] result = { "我", "购买", "了", "道具", "和", "服装" };
    assertAnalyzesTo(ca, sentence, result);
  }
  public void testMixedLatinChinese() throws Exception {
    assertAnalyzesTo(new SmartChineseAnalyzer(Version.LUCENE_CURRENT, true), "我购买 Tests 了道具和服装", 
        new String[] { "我", "购买", "test", "了", "道具", "和", "服装"});
  }
  public void testNumerics() throws Exception {
    assertAnalyzesTo(new SmartChineseAnalyzer(Version.LUCENE_CURRENT, true), "我购买 Tests 了道具和服装1234",
      new String[] { "我", "购买", "test", "了", "道具", "和", "服装", "1234"});
  }
  public void testFullWidth() throws Exception {
    assertAnalyzesTo(new SmartChineseAnalyzer(Version.LUCENE_CURRENT, true), "我购买 Ｔｅｓｔｓ 了道具和服装１２３４",
        new String[] { "我", "购买", "test", "了", "道具", "和", "服装", "1234"});
  }
  public void testDelimiters() throws Exception {
    assertAnalyzesTo(new SmartChineseAnalyzer(Version.LUCENE_CURRENT, true), "我购买︱ Tests 了道具和服装", 
        new String[] { "我", "购买", "test", "了", "道具", "和", "服装"});
  }
  public void testNonChinese() throws Exception {
    assertAnalyzesTo(new SmartChineseAnalyzer(Version.LUCENE_CURRENT, true), "我购买 روبرتTests 了道具和服装", 
        new String[] { "我", "购买", "ر", "و", "ب", "ر", "ت", "test", "了", "道具", "和", "服装"});
  }
  public void testOOV() throws Exception {
    assertAnalyzesTo(new SmartChineseAnalyzer(Version.LUCENE_CURRENT, true), "优素福·拉扎·吉拉尼",
      new String[] { "优", "素", "福", "拉", "扎", "吉", "拉", "尼" });
    assertAnalyzesTo(new SmartChineseAnalyzer(Version.LUCENE_CURRENT, true), "优素福拉扎吉拉尼",
      new String[] { "优", "素", "福", "拉", "扎", "吉", "拉", "尼" });
  }
  public void testOffsets() throws Exception {
    assertAnalyzesTo(new SmartChineseAnalyzer(Version.LUCENE_CURRENT, true), "我购买了道具和服装",
        new String[] { "我", "购买", "了", "道具", "和", "服装" },
        new int[] { 0, 1, 3, 4, 6, 7 },
        new int[] { 1, 3, 4, 6, 7, 9 });
  }
  public void testReusableTokenStream() throws Exception {
    Analyzer a = new SmartChineseAnalyzer(Version.LUCENE_CURRENT);
    assertAnalyzesToReuse(a, "我购买 Tests 了道具和服装", 
        new String[] { "我", "购买", "test", "了", "道具", "和", "服装"},
        new int[] { 0, 1, 4, 10, 11, 13, 14 },
        new int[] { 1, 3, 9, 11, 13, 14, 16 });
    assertAnalyzesToReuse(a, "我购买了道具和服装。",
        new String[] { "我", "购买", "了", "道具", "和", "服装" },
        new int[] { 0, 1, 3, 4, 6, 7 },
        new int[] { 1, 3, 4, 6, 7, 9 });
  }
}
