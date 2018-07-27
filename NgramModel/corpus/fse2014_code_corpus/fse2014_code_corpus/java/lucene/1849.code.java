package org.apache.lucene.analysis;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.English;
import org.apache.lucene.util.Version;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
public class TestStopFilter extends BaseTokenStreamTestCase {
  public void testExactCase() throws IOException {
    StringReader reader = new StringReader("Now is The Time");
    Set<String> stopWords = new HashSet<String>(Arrays.asList("is", "the", "Time"));
    TokenStream stream = new StopFilter(TEST_VERSION_CURRENT, new WhitespaceTokenizer(TEST_VERSION_CURRENT, reader), stopWords, false);
    final TermAttribute termAtt = stream.getAttribute(TermAttribute.class);
    assertTrue(stream.incrementToken());
    assertEquals("Now", termAtt.term());
    assertTrue(stream.incrementToken());
    assertEquals("The", termAtt.term());
    assertFalse(stream.incrementToken());
  }
  public void testIgnoreCase() throws IOException {
    StringReader reader = new StringReader("Now is The Time");
    Set<Object> stopWords = new HashSet<Object>(Arrays.asList( "is", "the", "Time" ));
    TokenStream stream = new StopFilter(TEST_VERSION_CURRENT, new WhitespaceTokenizer(TEST_VERSION_CURRENT, reader), stopWords, true);
    final TermAttribute termAtt = stream.getAttribute(TermAttribute.class);
    assertTrue(stream.incrementToken());
    assertEquals("Now", termAtt.term());
    assertFalse(stream.incrementToken());
  }
  public void testStopFilt() throws IOException {
    StringReader reader = new StringReader("Now is The Time");
    String[] stopWords = new String[] { "is", "the", "Time" };
    Set<Object> stopSet = StopFilter.makeStopSet(TEST_VERSION_CURRENT, stopWords);
    TokenStream stream = new StopFilter(TEST_VERSION_CURRENT, new WhitespaceTokenizer(TEST_VERSION_CURRENT, reader), stopSet);
    final TermAttribute termAtt = stream.getAttribute(TermAttribute.class);
    assertTrue(stream.incrementToken());
    assertEquals("Now", termAtt.term());
    assertTrue(stream.incrementToken());
    assertEquals("The", termAtt.term());
    assertFalse(stream.incrementToken());
  }
  public void testStopPositons() throws IOException {
    StringBuilder sb = new StringBuilder();
    ArrayList<String> a = new ArrayList<String>();
    for (int i=0; i<20; i++) {
      String w = English.intToEnglish(i).trim();
      sb.append(w).append(" ");
      if (i%3 != 0) a.add(w);
    }
    log(sb.toString());
    String stopWords[] = a.toArray(new String[0]);
    for (int i=0; i<a.size(); i++) log("Stop: "+stopWords[i]);
    Set<Object> stopSet = StopFilter.makeStopSet(TEST_VERSION_CURRENT, stopWords);
    StringReader reader = new StringReader(sb.toString());
    StopFilter stpf = new StopFilter(Version.LUCENE_24, new WhitespaceTokenizer(TEST_VERSION_CURRENT, reader), stopSet);
    doTestStopPositons(stpf,true);
    reader = new StringReader(sb.toString());
    stpf = new StopFilter(TEST_VERSION_CURRENT, new WhitespaceTokenizer(TEST_VERSION_CURRENT, reader), stopSet);
    doTestStopPositons(stpf,false);
    ArrayList<String> a0 = new ArrayList<String>();
    ArrayList<String> a1 = new ArrayList<String>();
    for (int i=0; i<a.size(); i++) {
      if (i%2==0) { 
        a0.add(a.get(i));
      } else {
        a1.add(a.get(i));
      }
    }
    String stopWords0[] =  a0.toArray(new String[0]);
    for (int i=0; i<a0.size(); i++) log("Stop0: "+stopWords0[i]);
    String stopWords1[] =  a1.toArray(new String[0]);
    for (int i=0; i<a1.size(); i++) log("Stop1: "+stopWords1[i]);
    Set<Object> stopSet0 = StopFilter.makeStopSet(TEST_VERSION_CURRENT, stopWords0);
    Set<Object> stopSet1 = StopFilter.makeStopSet(TEST_VERSION_CURRENT, stopWords1);
    reader = new StringReader(sb.toString());
    StopFilter stpf0 = new StopFilter(TEST_VERSION_CURRENT, new WhitespaceTokenizer(TEST_VERSION_CURRENT, reader), stopSet0); 
    stpf0.setEnablePositionIncrements(true);
    StopFilter stpf01 = new StopFilter(TEST_VERSION_CURRENT, stpf0, stopSet1); 
    doTestStopPositons(stpf01,true);
  }
  private void doTestStopPositons(StopFilter stpf, boolean enableIcrements) throws IOException {
    log("---> test with enable-increments-"+(enableIcrements?"enabled":"disabled"));
    stpf.setEnablePositionIncrements(enableIcrements);
    TermAttribute termAtt = stpf.getAttribute(TermAttribute.class);
    PositionIncrementAttribute posIncrAtt = stpf.getAttribute(PositionIncrementAttribute.class);
    for (int i=0; i<20; i+=3) {
      assertTrue(stpf.incrementToken());
      log("Token "+i+": "+stpf);
      String w = English.intToEnglish(i).trim();
      assertEquals("expecting token "+i+" to be "+w,w,termAtt.term());
      assertEquals("all but first token must have position increment of 3",enableIcrements?(i==0?1:3):1,posIncrAtt.getPositionIncrement());
    }
    assertFalse(stpf.incrementToken());
  }
  private static void log(String s) {
    if (VERBOSE) {
      System.out.println(s);
    }
  }
}
