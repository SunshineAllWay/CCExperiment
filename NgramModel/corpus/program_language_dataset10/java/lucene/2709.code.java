package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
public class TestHindiFilters extends BaseTokenTestCase {
  public void testTokenizer() throws Exception {
    Reader reader = new StringReader("मुझे हिंदी का और अभ्यास करना होगा ।");
    IndicTokenizerFactory factory = new IndicTokenizerFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    Tokenizer stream = factory.create(reader);
    assertTokenStreamContents(stream, 
        new String[] { "मुझे", "हिंदी", "का", "और", "अभ्यास", "करना", "होगा" });
  }
  public void testIndicNormalizer() throws Exception {
    Reader reader = new StringReader("ত্‍ अाैर");
    IndicTokenizerFactory factory = new IndicTokenizerFactory();
    IndicNormalizationFilterFactory filterFactory = new IndicNormalizationFilterFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    filterFactory.init(DEFAULT_VERSION_PARAM);
    Tokenizer tokenizer = factory.create(reader);
    TokenStream stream = filterFactory.create(tokenizer);
    assertTokenStreamContents(stream, new String[] { "ৎ", "और" });
  }
  public void testHindiNormalizer() throws Exception {
    Reader reader = new StringReader("क़िताब");
    IndicTokenizerFactory factory = new IndicTokenizerFactory();
    IndicNormalizationFilterFactory indicFilterFactory = new IndicNormalizationFilterFactory();
    HindiNormalizationFilterFactory hindiFilterFactory = new HindiNormalizationFilterFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    hindiFilterFactory.init(DEFAULT_VERSION_PARAM);
    Tokenizer tokenizer = factory.create(reader);
    TokenStream stream = indicFilterFactory.create(tokenizer);
    stream = hindiFilterFactory.create(stream);
    assertTokenStreamContents(stream, new String[] {"किताब"});
  }
  public void testStemmer() throws Exception {
    Reader reader = new StringReader("किताबें");
    IndicTokenizerFactory factory = new IndicTokenizerFactory();
    IndicNormalizationFilterFactory indicFilterFactory = new IndicNormalizationFilterFactory();
    HindiNormalizationFilterFactory hindiFilterFactory = new HindiNormalizationFilterFactory();
    HindiStemFilterFactory stemFactory = new HindiStemFilterFactory();
    factory.init(DEFAULT_VERSION_PARAM);
    stemFactory.init(DEFAULT_VERSION_PARAM);
    Tokenizer tokenizer = factory.create(reader);
    TokenStream stream = indicFilterFactory.create(tokenizer);
    stream = hindiFilterFactory.create(stream);
    stream = stemFactory.create(stream);
    assertTokenStreamContents(stream, new String[] {"किताब"});
  }
}
