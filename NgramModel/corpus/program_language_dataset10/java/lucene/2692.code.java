package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
public class TestArabicFilters extends BaseTokenTestCase {
  public void testTokenizer() throws Exception {
    Reader reader = new StringReader("الذين مَلكت أيمانكم");
    ArabicLetterTokenizerFactory factory = new ArabicLetterTokenizerFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    Tokenizer stream = factory.create(reader);
    assertTokenStreamContents(stream, new String[] {"الذين", "مَلكت", "أيمانكم"});
  }
  public void testNormalizer() throws Exception {
    Reader reader = new StringReader("الذين مَلكت أيمانكم");
    ArabicLetterTokenizerFactory factory = new ArabicLetterTokenizerFactory();
    ArabicNormalizationFilterFactory filterFactory = new ArabicNormalizationFilterFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    filterFactory.init(DEFAULT_VERSION_PARAM);
    Tokenizer tokenizer = factory.create(reader);
    TokenStream stream = filterFactory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] {"الذين", "ملكت", "ايمانكم"});
  }
  public void testStemmer() throws Exception {
    Reader reader = new StringReader("الذين مَلكت أيمانكم");
    ArabicLetterTokenizerFactory factory = new ArabicLetterTokenizerFactory();
    ArabicNormalizationFilterFactory normFactory = new ArabicNormalizationFilterFactory();
    ArabicStemFilterFactory stemFactory = new ArabicStemFilterFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    normFactory.init(DEFAULT_VERSION_PARAM);
    Tokenizer tokenizer = factory.create(reader);
    TokenStream stream = normFactory.create(tokenizer);
    stream = stemFactory.create(stream);
    assertTokenStreamContents(stream, new String[] {"ذين", "ملكت", "ايمانكم"});
  }
}
