package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestShingleFilterFactory extends BaseTokenTestCase { 
  public void testDefaults() throws Exception {
    Reader reader = new StringReader("this is a test");
    Map<String,String> args = new HashMap<String,String>();
    ShingleFilterFactory factory = new ShingleFilterFactory();
    factory.init(args);
    TokenStream stream = factory.create(new WhitespaceTokenizer(reader));
    assertTokenStreamContents(stream, new String[] {"this", "this is", "is",
        "is a", "a", "a test", "test"});
  }
  public void testNoUnigrams() throws Exception {
    Reader reader = new StringReader("this is a test");
    Map<String,String> args = new HashMap<String,String>();
    args.put("outputUnigrams", "false");
    ShingleFilterFactory factory = new ShingleFilterFactory();
    factory.init(args);
    TokenStream stream = factory.create(new WhitespaceTokenizer(reader));
    assertTokenStreamContents(stream,
        new String[] {"this is", "is a", "a test"});
  }
  public void testMaxShingleSize() throws Exception {
    Reader reader = new StringReader("this is a test");
    Map<String,String> args = new HashMap<String,String>();
    args.put("maxShingleSize", "3");
    ShingleFilterFactory factory = new ShingleFilterFactory();
    factory.init(args);
    TokenStream stream = factory.create(new WhitespaceTokenizer(reader));
    assertTokenStreamContents(stream, 
        new String[] {"this", "this is", "this is a", "is",
        "is a", "is a test", "a", "a test", "test"});
  }
}
