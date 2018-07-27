package org.apache.lucene.analysis.hi;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.in.IndicNormalizationFilter;
import org.apache.lucene.analysis.in.IndicTokenizer;
import org.apache.lucene.util.Version;
public final class HindiAnalyzer extends StopwordAnalyzerBase {
  private final Set<?> stemExclusionSet;
  public final static String DEFAULT_STOPWORD_FILE = "stopwords.txt";
  private static final String STOPWORDS_COMMENT = "#";
  public static Set<?> getDefaultStopSet(){
    return DefaultSetHolder.DEFAULT_STOP_SET;
  }
  private static class DefaultSetHolder {
    static final Set<?> DEFAULT_STOP_SET;
    static {
      try {
        DEFAULT_STOP_SET = loadStopwordSet(false, HindiAnalyzer.class, DEFAULT_STOPWORD_FILE, STOPWORDS_COMMENT);
      } catch (IOException ex) {
        throw new RuntimeException("Unable to load default stopword set");
      }
    }
  }
  public HindiAnalyzer(Version version, Set<?> stopwords, Set<?> stemExclusionSet) {
    super(version, stopwords);
    this.stemExclusionSet = CharArraySet.unmodifiableSet(
        CharArraySet.copy(matchVersion, stemExclusionSet));
  }
  public HindiAnalyzer(Version version, Set<?> stopwords) {
    this(version, stopwords, CharArraySet.EMPTY_SET);
  }
  public HindiAnalyzer(Version version) {
    this(version, DefaultSetHolder.DEFAULT_STOP_SET);
  }
  @Override
  protected TokenStreamComponents createComponents(String fieldName,
      Reader reader) {
    final Tokenizer source = new IndicTokenizer(matchVersion, reader);
    TokenStream result = new LowerCaseFilter(matchVersion, source);
    if (!stemExclusionSet.isEmpty())
      result = new KeywordMarkerTokenFilter(result, stemExclusionSet);
    result = new IndicNormalizationFilter(result);
    result = new HindiNormalizationFilter(result);
    result = new StopFilter(matchVersion, result, stopwords);
    result = new HindiStemFilter(result);
    return new TokenStreamComponents(source, result);
  }
}
