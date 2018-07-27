package org.apache.lucene.analysis.miscellaneous;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.TokenStream;
public class PatternAnalyzerTest extends BaseTokenStreamTestCase {
  public void testNonWordPattern() throws IOException {
    PatternAnalyzer a = new PatternAnalyzer(TEST_VERSION_CURRENT, PatternAnalyzer.NON_WORD_PATTERN,
        false, null);
    check(a, "The quick brown Fox,the abcd1234 (56.78) dc.", new String[] {
        "The", "quick", "brown", "Fox", "the", "abcd", "dc" });
    PatternAnalyzer b = new PatternAnalyzer(TEST_VERSION_CURRENT, PatternAnalyzer.NON_WORD_PATTERN,
        true, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
    check(b, "The quick brown Fox,the abcd1234 (56.78) dc.", new String[] {
        "quick", "brown", "fox", "abcd", "dc" });
  }
  public void testWhitespacePattern() throws IOException {
    PatternAnalyzer a = new PatternAnalyzer(TEST_VERSION_CURRENT, PatternAnalyzer.WHITESPACE_PATTERN,
        false, null);
    check(a, "The quick brown Fox,the abcd1234 (56.78) dc.", new String[] {
        "The", "quick", "brown", "Fox,the", "abcd1234", "(56.78)", "dc." });
    PatternAnalyzer b = new PatternAnalyzer(TEST_VERSION_CURRENT, PatternAnalyzer.WHITESPACE_PATTERN,
        true, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
    check(b, "The quick brown Fox,the abcd1234 (56.78) dc.", new String[] {
        "quick", "brown", "fox,the", "abcd1234", "(56.78)", "dc." });
  }
  public void testCustomPattern() throws IOException {
    PatternAnalyzer a = new PatternAnalyzer(TEST_VERSION_CURRENT, Pattern.compile(","), false, null);
    check(a, "Here,Are,some,Comma,separated,words,", new String[] { "Here",
        "Are", "some", "Comma", "separated", "words" });
    PatternAnalyzer b = new PatternAnalyzer(TEST_VERSION_CURRENT, Pattern.compile(","), true,
        StopAnalyzer.ENGLISH_STOP_WORDS_SET);
    check(b, "Here,Are,some,Comma,separated,words,", new String[] { "here",
        "some", "comma", "separated", "words" });
  }
  public void testHugeDocument() throws IOException {
    StringBuilder document = new StringBuilder();
    char largeWord[] = new char[5000];
    Arrays.fill(largeWord, 'a');
    document.append(largeWord);
    document.append(' ');
    char largeWord2[] = new char[2000];
    Arrays.fill(largeWord2, 'b');
    document.append(largeWord2);
    PatternAnalyzer a = new PatternAnalyzer(TEST_VERSION_CURRENT, PatternAnalyzer.WHITESPACE_PATTERN,
        false, null);
    check(a, document.toString(), new String[] { new String(largeWord),
        new String(largeWord2) });
  }
  private void check(PatternAnalyzer analyzer, String document,
      String expected[]) throws IOException {
    assertAnalyzesTo(analyzer, document, expected);
    TokenStream ts = analyzer.tokenStream("dummy",
        new PatternAnalyzer.FastStringReader(document));
    assertTokenStreamContents(ts, expected);
    TokenStream ts2 = analyzer.tokenStream("dummy", document);
    assertTokenStreamContents(ts2, expected);
  }
}
