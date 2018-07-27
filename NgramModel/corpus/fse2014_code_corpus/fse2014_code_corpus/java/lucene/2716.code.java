package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestNGramFilters extends BaseTokenTestCase {
  public void testNGramTokenizer() throws Exception {
    Reader reader = new StringReader("test");
    Map<String,String> args = new HashMap<String,String>();
    NGramTokenizerFactory factory = new NGramTokenizerFactory();
    factory.init(args);
    Tokenizer stream = factory.create(reader);
    assertTokenStreamContents(stream, 
        new String[] { "t", "e", "s", "t", "te", "es", "st" });
  }
  public void testNGramTokenizer2() throws Exception {
    Reader reader = new StringReader("test");
    Map<String,String> args = new HashMap<String,String>();
    args.put("minGramSize", "2");
    args.put("maxGramSize", "3");
    NGramTokenizerFactory factory = new NGramTokenizerFactory();
    factory.init(args);
    Tokenizer stream = factory.create(reader);
    assertTokenStreamContents(stream, 
        new String[] { "te", "es", "st", "tes", "est" });
  }
  public void testNGramFilter() throws Exception {
    Reader reader = new StringReader("test");
    Map<String,String> args = new HashMap<String,String>();
    NGramFilterFactory factory = new NGramFilterFactory();
    factory.init(args);
    TokenStream stream = factory.create(new WhitespaceTokenizer(reader));
    assertTokenStreamContents(stream, 
        new String[] { "t", "e", "s", "t", "te", "es", "st" });
  }
  public void testNGramFilter2() throws Exception {
    Reader reader = new StringReader("test");
    Map<String,String> args = new HashMap<String,String>();
    args.put("minGramSize", "2");
    args.put("maxGramSize", "3");
    NGramFilterFactory factory = new NGramFilterFactory();
    factory.init(args);
    TokenStream stream = factory.create(new WhitespaceTokenizer(reader));
    assertTokenStreamContents(stream, 
        new String[] { "te", "es", "st", "tes", "est" });
  }
  public void testEdgeNGramTokenizer() throws Exception {
    Reader reader = new StringReader("test");
    Map<String,String> args = new HashMap<String,String>();
    EdgeNGramTokenizerFactory factory = new EdgeNGramTokenizerFactory();
    factory.init(args);
    Tokenizer stream = factory.create(reader);
    assertTokenStreamContents(stream, 
        new String[] { "t" });
  }
  public void testEdgeNGramTokenizer2() throws Exception {
    Reader reader = new StringReader("test");
    Map<String,String> args = new HashMap<String,String>();
    args.put("minGramSize", "1");
    args.put("maxGramSize", "2");
    EdgeNGramTokenizerFactory factory = new EdgeNGramTokenizerFactory();
    factory.init(args);
    Tokenizer stream = factory.create(reader);
    assertTokenStreamContents(stream, 
        new String[] { "t", "te" });
  }
  public void testEdgeNGramTokenizer3() throws Exception {
    Reader reader = new StringReader("ready");
    Map<String,String> args = new HashMap<String,String>();
    args.put("side", "back");
    EdgeNGramTokenizerFactory factory = new EdgeNGramTokenizerFactory();
    factory.init(args);
    Tokenizer stream = factory.create(reader);
    assertTokenStreamContents(stream, 
        new String[] { "y" });
  }
  public void testEdgeNGramFilter() throws Exception {
    Reader reader = new StringReader("test");
    Map<String,String> args = new HashMap<String,String>();
    EdgeNGramFilterFactory factory = new EdgeNGramFilterFactory();
    factory.init(args);
    TokenStream stream = factory.create(new WhitespaceTokenizer(reader));
    assertTokenStreamContents(stream, 
        new String[] { "t" });
  }
  public void testEdgeNGramFilter2() throws Exception {
    Reader reader = new StringReader("test");
    Map<String,String> args = new HashMap<String,String>();
    args.put("minGramSize", "1");
    args.put("maxGramSize", "2");
    EdgeNGramFilterFactory factory = new EdgeNGramFilterFactory();
    factory.init(args);
    TokenStream stream = factory.create(new WhitespaceTokenizer(reader));
    assertTokenStreamContents(stream, 
        new String[] { "t", "te" });
  }
  public void testEdgeNGramFilter3() throws Exception {
    Reader reader = new StringReader("ready");
    Map<String,String> args = new HashMap<String,String>();
    args.put("side", "back");
    EdgeNGramFilterFactory factory = new EdgeNGramFilterFactory();
    factory.init(args);
    TokenStream stream = factory.create(new WhitespaceTokenizer(reader));
    assertTokenStreamContents(stream, 
        new String[] { "y" });
  }
}
