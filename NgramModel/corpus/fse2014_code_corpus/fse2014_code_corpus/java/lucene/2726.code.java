package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
public class TestRussianFilters extends BaseTokenTestCase {
  public void testTokenizer() throws Exception {
    Reader reader = new StringReader("Вместе с тем о силе электромагнитной 100");
    RussianLetterTokenizerFactory factory = new RussianLetterTokenizerFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    Tokenizer stream = factory.create(reader);
    assertTokenStreamContents(stream, new String[] {"Вместе", "с", "тем", "о",
        "силе", "электромагнитной", "100"});
  }
  public void testLowerCase() throws Exception {
    Reader reader = new StringReader("Вместе с тем о силе электромагнитной 100");
    RussianLetterTokenizerFactory factory = new RussianLetterTokenizerFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    RussianLowerCaseFilterFactory filterFactory = new RussianLowerCaseFilterFactory();
    filterFactory.init(DEFAULT_VERSION_PARAM);
    Tokenizer tokenizer = factory.create(reader);
    TokenStream stream = filterFactory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] {"вместе", "с", "тем", "о",
        "силе", "электромагнитной", "100"});
  }
  public void testStemmer() throws Exception {
    Reader reader = new StringReader("Вместе с тем о силе электромагнитной 100");
    RussianLetterTokenizerFactory factory = new RussianLetterTokenizerFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    RussianLowerCaseFilterFactory caseFactory = new RussianLowerCaseFilterFactory();
    caseFactory.init(DEFAULT_VERSION_PARAM);
    RussianStemFilterFactory stemFactory = new RussianStemFilterFactory();
    stemFactory.init(DEFAULT_VERSION_PARAM);
    Tokenizer tokenizer = factory.create(reader);
    TokenStream stream = caseFactory.create(tokenizer);
    stream = stemFactory.create(stream);
    assertTokenStreamContents(stream, new String[] {"вмест", "с", "тем", "о",
        "сил", "электромагнитн", "100"});
  }
}
