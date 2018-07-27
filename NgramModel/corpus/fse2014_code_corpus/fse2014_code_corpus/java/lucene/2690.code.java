package org.apache.solr.analysis;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class LengthFilterTest extends BaseTokenTestCase {
  public void test() throws IOException {
    LengthFilterFactory factory = new LengthFilterFactory();
    Map<String, String> args = new HashMap<String, String>();
    args.put(LengthFilterFactory.MIN_KEY, String.valueOf(4));
    args.put(LengthFilterFactory.MAX_KEY, String.valueOf(10));
    factory.init(args);
    String test = "foo foobar super-duper-trooper";
    TokenStream stream = factory.create(new WhitespaceTokenizer(new StringReader(test)));
    assertTokenStreamContents(stream, new String[] { "foobar" });
  }
}