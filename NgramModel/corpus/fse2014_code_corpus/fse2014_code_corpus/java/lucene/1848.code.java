package org.apache.lucene.analysis;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;
import java.io.StringReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
public class TestStopAnalyzer extends BaseTokenStreamTestCase {
  private StopAnalyzer stop = new StopAnalyzer(TEST_VERSION_CURRENT);
  private Set<Object> inValidTokens = new HashSet<Object>();
  public TestStopAnalyzer(String s) {
    super(s);
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Iterator<?> it = StopAnalyzer.ENGLISH_STOP_WORDS_SET.iterator();
    while(it.hasNext()) {
      inValidTokens.add(it.next());
    }
  }
  public void testDefaults() throws IOException {
    assertTrue(stop != null);
    StringReader reader = new StringReader("This is a test of the english stop analyzer");
    TokenStream stream = stop.tokenStream("test", reader);
    assertTrue(stream != null);
    TermAttribute termAtt = stream.getAttribute(TermAttribute.class);
    while (stream.incrementToken()) {
      assertFalse(inValidTokens.contains(termAtt.term()));
    }
  }
  public void testStopList() throws IOException {
    Set<Object> stopWordsSet = new HashSet<Object>();
    stopWordsSet.add("good");
    stopWordsSet.add("test");
    stopWordsSet.add("analyzer");
    StopAnalyzer newStop = new StopAnalyzer(Version.LUCENE_24, stopWordsSet);
    StringReader reader = new StringReader("This is a good test of the english stop analyzer");
    TokenStream stream = newStop.tokenStream("test", reader);
    assertNotNull(stream);
    TermAttribute termAtt = stream.getAttribute(TermAttribute.class);
    PositionIncrementAttribute posIncrAtt = stream.addAttribute(PositionIncrementAttribute.class);
    while (stream.incrementToken()) {
      String text = termAtt.term();
      assertFalse(stopWordsSet.contains(text));
      assertEquals(1,posIncrAtt.getPositionIncrement()); 
    }
  }
  public void testStopListPositions() throws IOException {
    Set<Object> stopWordsSet = new HashSet<Object>();
    stopWordsSet.add("good");
    stopWordsSet.add("test");
    stopWordsSet.add("analyzer");
    StopAnalyzer newStop = new StopAnalyzer(TEST_VERSION_CURRENT, stopWordsSet);
    StringReader reader = new StringReader("This is a good test of the english stop analyzer with positions");
    int expectedIncr[] =                  { 1,   1, 1,          3, 1,  1,      1,            2,   1};
    TokenStream stream = newStop.tokenStream("test", reader);
    assertNotNull(stream);
    int i = 0;
    TermAttribute termAtt = stream.getAttribute(TermAttribute.class);
    PositionIncrementAttribute posIncrAtt = stream.addAttribute(PositionIncrementAttribute.class);
    while (stream.incrementToken()) {
      String text = termAtt.term();
      assertFalse(stopWordsSet.contains(text));
      assertEquals(expectedIncr[i++],posIncrAtt.getPositionIncrement());
    }
  }
}
