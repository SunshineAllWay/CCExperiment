package org.apache.lucene.analysis.bg;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
public final class BulgarianAnalyzer extends StopwordAnalyzerBase {
  public final static String DEFAULT_STOPWORD_FILE = "stopwords.txt";
  @Deprecated
  public static final String STOPWORDS_COMMENT = "#";
  public static Set<?> getDefaultStopSet() {
    return DefaultSetHolder.DEFAULT_STOP_SET;
  }
  private static class DefaultSetHolder {
    static final Set<?> DEFAULT_STOP_SET;
    static {
      try {
        DEFAULT_STOP_SET = loadStopwordSet(false, BulgarianAnalyzer.class, DEFAULT_STOPWORD_FILE, STOPWORDS_COMMENT);
      } catch (IOException ex) {
        throw new RuntimeException("Unable to load default stopword set");
      }
    }
  }
  private final Set<?> stemExclusionSet;
  public BulgarianAnalyzer(Version matchVersion) {
    this(matchVersion, DefaultSetHolder.DEFAULT_STOP_SET);
  }
  public BulgarianAnalyzer(Version matchVersion, Set<?> stopwords) {
    this(matchVersion, stopwords, CharArraySet.EMPTY_SET);
  }
  public BulgarianAnalyzer(Version matchVersion, Set<?> stopwords, Set<?> stemExclusionSet) {
    super(matchVersion, stopwords);
    this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(
        matchVersion, stemExclusionSet));  }
  @Override
  public TokenStreamComponents createComponents(String fieldName, Reader reader) {
    final Tokenizer source = new StandardTokenizer(matchVersion, reader);
    TokenStream result = new StandardFilter(source);
    result = new LowerCaseFilter(matchVersion, result);
    result = new StopFilter(matchVersion, result, stopwords);
    if(!stemExclusionSet.isEmpty())
      result = new KeywordMarkerTokenFilter(result, stemExclusionSet);
    result = new BulgarianStemFilter(result);
    return new TokenStreamComponents(source, result);
  }
}
