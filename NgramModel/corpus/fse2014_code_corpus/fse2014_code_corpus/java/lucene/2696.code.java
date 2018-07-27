package org.apache.solr.analysis;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestCapitalizationFilter extends BaseTokenTestCase {
  public void testCapitalization() throws Exception 
  {
    Map<String,String> args = new HashMap<String, String>();
    args.put( CapitalizationFilterFactory.KEEP, "and the it BIG" );
    args.put( CapitalizationFilterFactory.ONLY_FIRST_WORD, "true" );  
    CapitalizationFilterFactory factory = new CapitalizationFilterFactory();
    factory.init( args );
    char[] termBuffer;
    termBuffer = "kiTTEN".toCharArray();
    factory.processWord(termBuffer, 0, termBuffer.length, 0 );
    assertEquals( "Kitten",  new String(termBuffer, 0, termBuffer.length));
    factory.forceFirstLetter = true;
    termBuffer = "and".toCharArray();
    factory.processWord(termBuffer, 0, termBuffer.length, 0 );
    assertEquals( "And",  new String(termBuffer, 0, termBuffer.length));
    termBuffer = "AnD".toCharArray();
    factory.processWord(termBuffer, 0, termBuffer.length, 0 );
    assertEquals( "And",  new String(termBuffer, 0, termBuffer.length));
    factory.forceFirstLetter = false;
    termBuffer = "AnD".toCharArray();
    factory.processWord(termBuffer, 0, termBuffer.length, 0 );
    assertEquals( "And",  new String(termBuffer, 0, termBuffer.length)); 
    factory.forceFirstLetter = true;
    termBuffer = "big".toCharArray();
    factory.processWord(termBuffer, 0, termBuffer.length, 0 );
    assertEquals( "Big",  new String(termBuffer, 0, termBuffer.length));
    termBuffer = "BIG".toCharArray();
    factory.processWord(termBuffer, 0, termBuffer.length, 0 );
    assertEquals( "BIG",  new String(termBuffer, 0, termBuffer.length));
    Tokenizer tokenizer = new KeywordTokenizer(new StringReader("Hello thEre my Name is Ryan"));
    TokenStream stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "Hello there my name is ryan" });
    factory.onlyFirstWord = false;
    tokenizer = new WhitespaceTokenizer(new StringReader("Hello thEre my Name is Ryan"));
    stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "Hello", "There", "My", "Name", "Is", "Ryan" });
    factory.minWordLength = 3;
    tokenizer = new WhitespaceTokenizer(new StringReader("Hello thEre my Name is Ryan" ));
    stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "Hello", "There", "my", "Name", "is", "Ryan" });
    tokenizer = new WhitespaceTokenizer(new StringReader("McKinley" ));
    stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "Mckinley" });
    factory = new CapitalizationFilterFactory();
    args.put( "okPrefix", "McK" );  
    factory.init( args );
    tokenizer = new WhitespaceTokenizer(new StringReader("McKinley" ));
    stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "McKinley" });
    factory.forceFirstLetter = false;
    factory.onlyFirstWord = false;
    tokenizer = new WhitespaceTokenizer(new StringReader("1st 2nd third" ));
    stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "1st", "2nd", "Third" });
    factory.forceFirstLetter = true;  
    tokenizer = new KeywordTokenizer(new StringReader("the The the" ));
    stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "The The the" });
  }
  public void testKeepIgnoreCase() throws Exception {
    Map<String,String> args = new HashMap<String, String>();
    args.put( CapitalizationFilterFactory.KEEP, "kitten" );
    args.put( CapitalizationFilterFactory.KEEP_IGNORE_CASE, "true" );
    args.put( CapitalizationFilterFactory.ONLY_FIRST_WORD, "true" );
    CapitalizationFilterFactory factory = new CapitalizationFilterFactory();
    factory.init( args );
    char[] termBuffer;
    termBuffer = "kiTTEN".toCharArray();
    factory.forceFirstLetter = true;
    factory.processWord(termBuffer, 0, termBuffer.length, 0 );
    assertEquals( "KiTTEN",  new String(termBuffer, 0, termBuffer.length));
    factory.forceFirstLetter = false;
    termBuffer = "kiTTEN".toCharArray();
    factory.processWord(termBuffer, 0, termBuffer.length, 0 );
    assertEquals( "kiTTEN",  new String(termBuffer, 0, termBuffer.length));
    factory.keep = null;
    termBuffer = "kiTTEN".toCharArray();
    factory.processWord(termBuffer, 0, termBuffer.length, 0 );
    assertEquals( "Kitten",  new String(termBuffer, 0, termBuffer.length));
  }
  public void testMinWordLength() throws Exception {
    Map<String,String> args = new HashMap<String,String>();
    args.put(CapitalizationFilterFactory.ONLY_FIRST_WORD, "true");
    args.put(CapitalizationFilterFactory.MIN_WORD_LENGTH, "5");
    CapitalizationFilterFactory factory = new CapitalizationFilterFactory();
    factory.init(args);
    Tokenizer tokenizer = new WhitespaceTokenizer(new StringReader(
        "helo testing"));
    TokenStream ts = factory.create(tokenizer);
    assertTokenStreamContents(ts, new String[] {"helo", "Testing"});
  }
  public void testMaxWordCount() throws Exception {
    Map<String,String> args = new HashMap<String,String>();
    args.put(CapitalizationFilterFactory.MAX_WORD_COUNT, "2");
    CapitalizationFilterFactory factory = new CapitalizationFilterFactory();
    factory.init(args);
    Tokenizer tokenizer = new WhitespaceTokenizer(new StringReader(
        "one two three four"));
    TokenStream ts = factory.create(tokenizer);
    assertTokenStreamContents(ts, new String[] {"One", "Two", "Three", "Four"});
  }
  public void testMaxWordCount2() throws Exception {
    Map<String,String> args = new HashMap<String,String>();
    args.put(CapitalizationFilterFactory.MAX_WORD_COUNT, "2");
    CapitalizationFilterFactory factory = new CapitalizationFilterFactory();
    factory.init(args);
    Tokenizer tokenizer = new KeywordTokenizer(new StringReader(
        "one two three four"));
    TokenStream ts = factory.create(tokenizer);
    assertTokenStreamContents(ts, new String[] {"one two three four"});
  }
  public void testMaxTokenLength() throws Exception {
    Map<String,String> args = new HashMap<String,String>();
    args.put(CapitalizationFilterFactory.MAX_TOKEN_LENGTH, "2");
    CapitalizationFilterFactory factory = new CapitalizationFilterFactory();
    factory.init(args);
    Tokenizer tokenizer = new WhitespaceTokenizer(new StringReader(
        "this is a test"));
    TokenStream ts = factory.create(tokenizer);
    assertTokenStreamContents(ts, new String[] {"this", "is", "A", "test"});
  }
  public void testForceFirstLetter() throws Exception {
    Map<String,String> args = new HashMap<String,String>();
    args.put(CapitalizationFilterFactory.KEEP, "kitten");
    args.put(CapitalizationFilterFactory.FORCE_FIRST_LETTER, "true");
    CapitalizationFilterFactory factory = new CapitalizationFilterFactory();
    factory.init(args);
    Tokenizer tokenizer = new WhitespaceTokenizer(new StringReader("kitten"));
    TokenStream ts = factory.create(tokenizer);
    assertTokenStreamContents(ts, new String[] {"Kitten"});
  }
}
