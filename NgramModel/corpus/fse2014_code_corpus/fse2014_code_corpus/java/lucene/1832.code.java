package org.apache.lucene.analysis;
import java.io.IOException;
import java.io.StringReader;
import java.io.Reader;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.Payload;
public class TestAnalyzers extends BaseTokenStreamTestCase {
   public TestAnalyzers(String name) {
      super(name);
   }
  public void testSimple() throws Exception {
    Analyzer a = new SimpleAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(a, "foo bar FOO BAR", 
                     new String[] { "foo", "bar", "foo", "bar" });
    assertAnalyzesTo(a, "foo      bar .  FOO <> BAR", 
                     new String[] { "foo", "bar", "foo", "bar" });
    assertAnalyzesTo(a, "foo.bar.FOO.BAR", 
                     new String[] { "foo", "bar", "foo", "bar" });
    assertAnalyzesTo(a, "U.S.A.", 
                     new String[] { "u", "s", "a" });
    assertAnalyzesTo(a, "C++", 
                     new String[] { "c" });
    assertAnalyzesTo(a, "B2B", 
                     new String[] { "b", "b" });
    assertAnalyzesTo(a, "2B", 
                     new String[] { "b" });
    assertAnalyzesTo(a, "\"QUOTED\" word", 
                     new String[] { "quoted", "word" });
  }
  public void testNull() throws Exception {
    Analyzer a = new WhitespaceAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(a, "foo bar FOO BAR", 
                     new String[] { "foo", "bar", "FOO", "BAR" });
    assertAnalyzesTo(a, "foo      bar .  FOO <> BAR", 
                     new String[] { "foo", "bar", ".", "FOO", "<>", "BAR" });
    assertAnalyzesTo(a, "foo.bar.FOO.BAR", 
                     new String[] { "foo.bar.FOO.BAR" });
    assertAnalyzesTo(a, "U.S.A.", 
                     new String[] { "U.S.A." });
    assertAnalyzesTo(a, "C++", 
                     new String[] { "C++" });
    assertAnalyzesTo(a, "B2B", 
                     new String[] { "B2B" });
    assertAnalyzesTo(a, "2B", 
                     new String[] { "2B" });
    assertAnalyzesTo(a, "\"QUOTED\" word", 
                     new String[] { "\"QUOTED\"", "word" });
  }
  public void testStop() throws Exception {
    Analyzer a = new StopAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(a, "foo bar FOO BAR", 
                     new String[] { "foo", "bar", "foo", "bar" });
    assertAnalyzesTo(a, "foo a bar such FOO THESE BAR", 
                     new String[] { "foo", "bar", "foo", "bar" });
  }
  void verifyPayload(TokenStream ts) throws IOException {
    PayloadAttribute payloadAtt = ts.getAttribute(PayloadAttribute.class);
    for(byte b=1;;b++) {
      boolean hasNext = ts.incrementToken();
      if (!hasNext) break;
      assertEquals(b, payloadAtt.getPayload().toByteArray()[0]);
    }
  }
  public void testPayloadCopy() throws IOException {
    String s = "how now brown cow";
    TokenStream ts;
    ts = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(s));
    ts = new PayloadSetter(ts);
    verifyPayload(ts);
    ts = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(s));
    ts = new PayloadSetter(ts);
    verifyPayload(ts);
  }
  @SuppressWarnings("unused")
  public void _testStandardConstants() {
    int x = StandardTokenizer.ALPHANUM;
    x = StandardTokenizer.APOSTROPHE;
    x = StandardTokenizer.ACRONYM;
    x = StandardTokenizer.COMPANY;
    x = StandardTokenizer.EMAIL;
    x = StandardTokenizer.HOST;
    x = StandardTokenizer.NUM;
    x = StandardTokenizer.CJ;
    String[] y = StandardTokenizer.TOKEN_TYPES;
  }
  private static class MyStandardAnalyzer extends StandardAnalyzer {
    public MyStandardAnalyzer() {
      super(TEST_VERSION_CURRENT);
    }
    @Override
    public TokenStream tokenStream(String field, Reader reader) {
      return new WhitespaceAnalyzer(TEST_VERSION_CURRENT).tokenStream(field, reader);
    }
  }
  public void testSubclassOverridingOnlyTokenStream() throws Throwable {
    Analyzer a = new MyStandardAnalyzer();
    TokenStream ts = a.reusableTokenStream("field", new StringReader("the"));
    assertTrue(ts.incrementToken());
    assertFalse(ts.incrementToken());
  }
  private static class LowerCaseWhitespaceAnalyzer extends Analyzer {
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
      return new LowerCaseFilter(TEST_VERSION_CURRENT,
          new WhitespaceTokenizer(TEST_VERSION_CURRENT, reader));
    }
  }
  @Deprecated
  private static class LowerCaseWhitespaceAnalyzerBWComp extends Analyzer {
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
      return new LowerCaseFilter(new WhitespaceTokenizer(reader));
    }
  }
  public void testLowerCaseFilter() throws IOException {
    Analyzer a = new LowerCaseWhitespaceAnalyzer();
    assertAnalyzesTo(a, "AbaCaDabA", new String[] { "abacadaba" });
    assertAnalyzesTo(a, "\ud801\udc16\ud801\udc16\ud801\udc16\ud801\udc16",
        new String[] {"\ud801\udc3e\ud801\udc3e\ud801\udc3e\ud801\udc3e"});
    assertAnalyzesTo(a, "AbaCa\ud801\udc16DabA", 
        new String[] { "abaca\ud801\udc3edaba" });
    assertAnalyzesTo(a, "AbaC\uD801AdaBa", 
        new String [] { "abac\uD801adaba" });
    assertAnalyzesTo(a, "AbaC\uDC16AdaBa", 
        new String [] { "abac\uDC16adaba" });
  }
  public void testLowerCaseFilterLowSurrogateLeftover() throws IOException {
    WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(TEST_VERSION_CURRENT, 
        new StringReader("BogustermBogusterm\udc16"));
    LowerCaseFilter filter = new LowerCaseFilter(TEST_VERSION_CURRENT,
        tokenizer);
    assertTokenStreamContents(filter, new String[] {"bogustermbogusterm\udc16"});
    filter.reset();
    String highSurEndingUpper = "BogustermBoguster\ud801";
    String highSurEndingLower = "bogustermboguster\ud801";
    tokenizer.reset(new StringReader(highSurEndingUpper));
    assertTokenStreamContents(filter, new String[] {highSurEndingLower});
    assertTrue(filter.hasAttribute(TermAttribute.class));
    char[] termBuffer = filter.getAttribute(TermAttribute.class).termBuffer();
    int length = highSurEndingLower.length();
    assertEquals('\ud801', termBuffer[length - 1]);
    assertEquals('\udc3e', termBuffer[length]);
  }
  @Deprecated
  public void testLowerCaseFilterBWComp() throws IOException {
    Analyzer a = new LowerCaseWhitespaceAnalyzerBWComp();
    assertAnalyzesTo(a, "AbaCaDabA", new String[] { "abacadaba" });
    assertAnalyzesTo(a, "\ud801\udc16\ud801\udc16\ud801\udc16\ud801\udc16",
        new String[] {"\ud801\udc16\ud801\udc16\ud801\udc16\ud801\udc16"});
    assertAnalyzesTo(a, "AbaCa\ud801\udc16DabA",
        new String[] { "abaca\ud801\udc16daba" });
    assertAnalyzesTo(a, "AbaC\uD801AdaBa", 
        new String [] { "abac\uD801adaba" });
    assertAnalyzesTo(a, "AbaC\uDC16AdaBa", 
        new String [] { "abac\uDC16adaba" });
  }
}
class PayloadSetter extends TokenFilter {
  PayloadAttribute payloadAtt;
  public  PayloadSetter(TokenStream input) {
    super(input);
    payloadAtt = addAttribute(PayloadAttribute.class);
  }
  byte[] data = new byte[1];
  Payload p = new Payload(data,0,1);
  @Override
  public boolean incrementToken() throws IOException {
    boolean hasNext = input.incrementToken();
    if (!hasNext) return false;
    payloadAtt.setPayload(p);  
    data[0]++;
    return true;
  }
}