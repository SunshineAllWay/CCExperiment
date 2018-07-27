package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestStandardFactories extends BaseTokenTestCase {
  public void testStandardTokenizer() throws Exception {
    Reader reader = new StringReader("What's this thing do?");
    StandardTokenizerFactory factory = new StandardTokenizerFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    Tokenizer stream = factory.create(reader);
    assertTokenStreamContents(stream, 
        new String[] {"What's", "this", "thing", "do" });
  }
  public void testStandardFilter() throws Exception {
    Reader reader = new StringReader("What's this thing do?");
    StandardTokenizerFactory factory = new StandardTokenizerFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    StandardFilterFactory filterFactory = new StandardFilterFactory();
    filterFactory.init(DEFAULT_VERSION_PARAM);
    Tokenizer tokenizer = factory.create(reader);
    TokenStream stream = filterFactory.create(tokenizer);
    assertTokenStreamContents(stream, 
        new String[] {"What", "this", "thing", "do"});
  }
  public void testKeywordTokenizer() throws Exception {
    Reader reader = new StringReader("What's this thing do?");
    KeywordTokenizerFactory factory = new KeywordTokenizerFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    Tokenizer stream = factory.create(reader);
    assertTokenStreamContents(stream, 
        new String[] {"What's this thing do?"});
  }
  public void testWhitespaceTokenizer() throws Exception {
    Reader reader = new StringReader("What's this thing do?");
    WhitespaceTokenizerFactory factory = new WhitespaceTokenizerFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    Tokenizer stream = factory.create(reader);
    assertTokenStreamContents(stream, 
        new String[] {"What's", "this", "thing", "do?"});
  }
  public void testLetterTokenizer() throws Exception {
    Reader reader = new StringReader("What's this thing do?");
    LetterTokenizerFactory factory = new LetterTokenizerFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    Tokenizer stream = factory.create(reader);
    assertTokenStreamContents(stream, 
        new String[] {"What", "s", "this", "thing", "do"});
  }
  public void testLowerCaseTokenizer() throws Exception {
    Reader reader = new StringReader("What's this thing do?");
    LowerCaseTokenizerFactory factory = new LowerCaseTokenizerFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    Tokenizer stream = factory.create(reader);
    assertTokenStreamContents(stream, 
        new String[] {"what", "s", "this", "thing", "do"});
  }
  public void testASCIIFolding() throws Exception {
    Reader reader = new StringReader("Česká");
    Tokenizer tokenizer = new WhitespaceTokenizer(reader);
    ASCIIFoldingFilterFactory factory = new ASCIIFoldingFilterFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    TokenStream stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "Ceska" });
  }
  public void testISOLatin1Folding() throws Exception {
    Reader reader = new StringReader("Česká");
    Tokenizer tokenizer = new WhitespaceTokenizer(reader);
    ISOLatin1AccentFilterFactory factory = new ISOLatin1AccentFilterFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    TokenStream stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "Česka" });
  }
}
