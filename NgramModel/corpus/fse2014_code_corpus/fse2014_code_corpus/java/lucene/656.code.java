package org.apache.lucene.analysis.fa;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.ar.ArabicLetterTokenizer;
import org.apache.lucene.analysis.ar.ArabicNormalizationFilter;
import org.apache.lucene.util.Version;
public final class PersianAnalyzer extends StopwordAnalyzerBase {
  public final static String DEFAULT_STOPWORD_FILE = "stopwords.txt";
  public static final String STOPWORDS_COMMENT = "#";
  public static Set<?> getDefaultStopSet(){
    return DefaultSetHolder.DEFAULT_STOP_SET;
  }
  private static class DefaultSetHolder {
    static final Set<?> DEFAULT_STOP_SET;
    static {
      try {
        DEFAULT_STOP_SET = loadStopwordSet(false, PersianAnalyzer.class, DEFAULT_STOPWORD_FILE, STOPWORDS_COMMENT);
      } catch (IOException ex) {
        throw new RuntimeException("Unable to load default stopword set");
      }
    }
  }
  public PersianAnalyzer(Version matchVersion) {
    this(matchVersion, DefaultSetHolder.DEFAULT_STOP_SET);
  }
  public PersianAnalyzer(Version matchVersion, Set<?> stopwords){
    super(matchVersion, stopwords);
  }
  @Deprecated
  public PersianAnalyzer(Version matchVersion, String... stopwords) {
    this(matchVersion, StopFilter.makeStopSet(matchVersion, stopwords));
  }
  @Deprecated
  public PersianAnalyzer(Version matchVersion, Hashtable<?, ?> stopwords) {
    this(matchVersion, stopwords.keySet());
  }
  @Deprecated
  public PersianAnalyzer(Version matchVersion, File stopwords) throws IOException {
    this(matchVersion, WordlistLoader.getWordSet(stopwords, STOPWORDS_COMMENT));
  }
  @Override
  protected TokenStreamComponents createComponents(String fieldName,
      Reader reader) {
    final Tokenizer source = new ArabicLetterTokenizer(matchVersion, reader);
    TokenStream result = new LowerCaseFilter(matchVersion, source);
    result = new ArabicNormalizationFilter(result);
    result = new PersianNormalizationFilter(result);
    return new TokenStreamComponents(source, new StopFilter(matchVersion, result, stopwords));
  }
}
