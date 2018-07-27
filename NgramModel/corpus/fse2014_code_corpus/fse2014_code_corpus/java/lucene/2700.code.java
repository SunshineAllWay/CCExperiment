package org.apache.solr.analysis;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.solr.common.ResourceLoader;
public class TestCollationKeyFilterFactory extends BaseTokenTestCase {
  public void testBasicUsage() throws IOException {
    String turkishUpperCase = "I WİLL USE TURKİSH CASING";
    String turkishLowerCase = "ı will use turkish casıng";
    CollationKeyFilterFactory factory = new CollationKeyFilterFactory();
    Map<String,String> args = new HashMap<String,String>();
    args.put("language", "tr");
    args.put("strength", "primary");
    factory.init(args);
    factory.inform(new StringMockSolrResourceLoader(""));
    TokenStream tsUpper = factory.create(
        new KeywordTokenizer(new StringReader(turkishUpperCase)));
    TokenStream tsLower = factory.create(
        new KeywordTokenizer(new StringReader(turkishLowerCase)));
    assertCollatesToSame(tsUpper, tsLower);
  }
  public void testNormalization() throws IOException {
    String turkishUpperCase = "I W\u0049\u0307LL USE TURKİSH CASING";
    String turkishLowerCase = "ı will use turkish casıng";
    CollationKeyFilterFactory factory = new CollationKeyFilterFactory();
    Map<String,String> args = new HashMap<String,String>();
    args.put("language", "tr");
    args.put("strength", "primary");
    args.put("decomposition", "canonical");
    factory.init(args);
    factory.inform(new StringMockSolrResourceLoader(""));
    TokenStream tsUpper = factory.create(
        new KeywordTokenizer(new StringReader(turkishUpperCase)));
    TokenStream tsLower = factory.create(
        new KeywordTokenizer(new StringReader(turkishLowerCase)));
    assertCollatesToSame(tsUpper, tsLower);
  }
  public void testFullDecomposition() throws IOException {
    String fullWidth = "Ｔｅｓｔｉｎｇ";
    String halfWidth = "Testing";
    CollationKeyFilterFactory factory = new CollationKeyFilterFactory();
    Map<String,String> args = new HashMap<String,String>();
    args.put("language", "zh");
    args.put("strength", "identical");
    args.put("decomposition", "full");
    factory.init(args);
    factory.inform(new StringMockSolrResourceLoader(""));
    TokenStream tsFull = factory.create(
        new KeywordTokenizer(new StringReader(fullWidth)));
    TokenStream tsHalf = factory.create(
        new KeywordTokenizer(new StringReader(halfWidth)));
    assertCollatesToSame(tsFull, tsHalf);
  }
  public void testSecondaryStrength() throws IOException {
    String upperCase = "TESTING";
    String lowerCase = "testing";
    CollationKeyFilterFactory factory = new CollationKeyFilterFactory();
    Map<String,String> args = new HashMap<String,String>();
    args.put("language", "en");
    args.put("strength", "secondary");
    args.put("decomposition", "no");
    factory.init(args);
    factory.inform(new StringMockSolrResourceLoader(""));
    TokenStream tsUpper = factory.create(
        new KeywordTokenizer(new StringReader(upperCase)));
    TokenStream tsLower = factory.create(
        new KeywordTokenizer(new StringReader(lowerCase)));
    assertCollatesToSame(tsUpper, tsLower);
  }
  public void testCustomRules() throws Exception {
    RuleBasedCollator baseCollator = (RuleBasedCollator) Collator.getInstance(new Locale("de", "DE"));
    String DIN5007_2_tailorings =
      "& ae , a\u0308 & AE , A\u0308"+
      "& oe , o\u0308 & OE , O\u0308"+
      "& ue , u\u0308 & UE , u\u0308";
    RuleBasedCollator tailoredCollator = new RuleBasedCollator(baseCollator.getRules() + DIN5007_2_tailorings);
    String tailoredRules = tailoredCollator.getRules();
    String germanUmlaut = "Töne";
    String germanOE = "Toene";
    CollationKeyFilterFactory factory = new CollationKeyFilterFactory();
    Map<String,String> args = new HashMap<String,String>();
    args.put("custom", "rules.txt");
    args.put("strength", "primary");
    factory.init(args);
    factory.inform(new StringMockSolrResourceLoader(tailoredRules));
    TokenStream tsUmlaut = factory.create(
        new KeywordTokenizer(new StringReader(germanUmlaut)));
    TokenStream tsOE = factory.create(
        new KeywordTokenizer(new StringReader(germanOE)));
    assertCollatesToSame(tsUmlaut, tsOE);
  }
  private class StringMockSolrResourceLoader implements ResourceLoader {
    String text;
    StringMockSolrResourceLoader(String text) {
      this.text = text;
    }
    public List<String> getLines(String resource) throws IOException {
      return null;
    }
    public Object newInstance(String cname, String... subpackages) {
      return null;
    }
    public InputStream openResource(String resource) throws IOException {
      return new ByteArrayInputStream(text.getBytes("UTF-8"));
    }
  }
  private void assertCollatesToSame(TokenStream stream1, TokenStream stream2)
      throws IOException {
    TermAttribute term1 = (TermAttribute) stream1
        .addAttribute(TermAttribute.class);
    TermAttribute term2 = (TermAttribute) stream2
        .addAttribute(TermAttribute.class);
    assertTrue(stream1.incrementToken());
    assertTrue(stream2.incrementToken());
    assertEquals(term1.term(), term2.term());
    assertFalse(stream1.incrementToken());
    assertFalse(stream2.incrementToken());
  }
}
