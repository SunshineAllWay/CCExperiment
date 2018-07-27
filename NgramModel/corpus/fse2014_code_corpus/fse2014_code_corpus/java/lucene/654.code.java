package org.apache.lucene.analysis.en;
import java.io.Reader;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
public final class EnglishAnalyzer extends StopwordAnalyzerBase {
  private final Set<?> stemExclusionSet;
  public static Set<?> getDefaultStopSet(){
    return DefaultSetHolder.DEFAULT_STOP_SET;
  }
  private static class DefaultSetHolder {
    static final Set<?> DEFAULT_STOP_SET = StandardAnalyzer.STOP_WORDS_SET;
  }
  public EnglishAnalyzer(Version matchVersion) {
    this(matchVersion, DefaultSetHolder.DEFAULT_STOP_SET);
  }
  public EnglishAnalyzer(Version matchVersion, Set<?> stopwords) {
    this(matchVersion, stopwords, CharArraySet.EMPTY_SET);
  }
  public EnglishAnalyzer(Version matchVersion, Set<?> stopwords, Set<?> stemExclusionSet) {
    super(matchVersion, stopwords);
    this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(
        matchVersion, stemExclusionSet));
  }
  @Override
  protected TokenStreamComponents createComponents(String fieldName,
      Reader reader) {
    final Tokenizer source = new StandardTokenizer(matchVersion, reader);
    TokenStream result = new StandardFilter(source);
    result = new LowerCaseFilter(matchVersion, result);
    result = new StopFilter(matchVersion, result, stopwords);
    if(!stemExclusionSet.isEmpty())
      result = new KeywordMarkerTokenFilter(result, stemExclusionSet);
    result = new PorterStemFilter(result);
    return new TokenStreamComponents(source, result);
  }
}
