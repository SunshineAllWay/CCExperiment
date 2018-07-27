package org.apache.solr.analysis;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public class DoubleMetaphoneFilterFactoryTest extends BaseTokenTestCase {
  public void testDefaults() throws Exception {
    DoubleMetaphoneFilterFactory factory = new DoubleMetaphoneFilterFactory();
    factory.init(new HashMap<String, String>());
    TokenStream inputStream = new WhitespaceTokenizer(new StringReader("international"));
    TokenStream filteredStream = factory.create(inputStream);
    assertEquals(DoubleMetaphoneFilter.class, filteredStream.getClass());
    assertTokenStreamContents(filteredStream, new String[] { "international", "ANTR" });
  }
  public void testSettingSizeAndInject() throws Exception {
    DoubleMetaphoneFilterFactory factory = new DoubleMetaphoneFilterFactory();
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("inject", "false");
    parameters.put("maxCodeLength", "8");
    factory.init(parameters);
    TokenStream inputStream = new WhitespaceTokenizer(new StringReader("international"));
    TokenStream filteredStream = factory.create(inputStream);
    assertEquals(DoubleMetaphoneFilter.class, filteredStream.getClass());
    assertTokenStreamContents(filteredStream, new String[] { "ANTRNXNL" });
  }
  public void testReset() throws Exception {
    DoubleMetaphoneFilterFactory factory = new DoubleMetaphoneFilterFactory();
    factory.init(new HashMap<String, String>());
    TokenStream inputStream = new WhitespaceTokenizer(new StringReader("international"));
    TokenStream filteredStream = factory.create(inputStream);
    TermAttribute termAtt = (TermAttribute) filteredStream.addAttribute(TermAttribute.class);
    assertEquals(DoubleMetaphoneFilter.class, filteredStream.getClass());
    assertTrue(filteredStream.incrementToken());
    assertEquals(13, termAtt.termLength());
    assertEquals("international", termAtt.term());
    filteredStream.reset();
    assertFalse(filteredStream.incrementToken());
  }
}
