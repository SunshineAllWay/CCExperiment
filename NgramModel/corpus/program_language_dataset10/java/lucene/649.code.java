package org.apache.lucene.analysis.de;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.German2Stemmer;
public final class GermanAnalyzer extends StopwordAnalyzerBase {
  @Deprecated
  public final static String[] GERMAN_STOP_WORDS = {
    "einer", "eine", "eines", "einem", "einen",
    "der", "die", "das", "dass", "daß",
    "du", "er", "sie", "es",
    "was", "wer", "wie", "wir",
    "und", "oder", "ohne", "mit",
    "am", "im", "in", "aus", "auf",
    "ist", "sein", "war", "wird",
    "ihr", "ihre", "ihres",
    "als", "für", "von", "mit",
    "dich", "dir", "mich", "mir",
    "mein", "sein", "kein",
    "durch", "wegen", "wird"
  };
  public final static String DEFAULT_STOPWORD_FILE = "german_stop.txt";
  public static final Set<?> getDefaultStopSet(){
    return DefaultSetHolder.DEFAULT_SET;
  }
  private static class DefaultSetHolder {
    @Deprecated
    private static final Set<?> DEFAULT_SET_30 = CharArraySet.unmodifiableSet(new CharArraySet(
        Version.LUCENE_CURRENT, Arrays.asList(GERMAN_STOP_WORDS), false));
    private static final Set<?> DEFAULT_SET;
    static {
      try {
        DEFAULT_SET = 
          WordlistLoader.getSnowballWordSet(SnowballFilter.class, DEFAULT_STOPWORD_FILE);
      } catch (IOException ex) {
        throw new RuntimeException("Unable to load default stopword set");
      }
    }
  }
  private Set<?> exclusionSet;
  public GermanAnalyzer(Version matchVersion) {
    this(matchVersion,
        matchVersion.onOrAfter(Version.LUCENE_31) ? DefaultSetHolder.DEFAULT_SET
            : DefaultSetHolder.DEFAULT_SET_30);
  }
  public GermanAnalyzer(Version matchVersion, Set<?> stopwords) {
    this(matchVersion, stopwords, CharArraySet.EMPTY_SET);
  }
  public GermanAnalyzer(Version matchVersion, Set<?> stopwords, Set<?> stemExclusionSet) {
    super(matchVersion, stopwords);
    exclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(matchVersion, stemExclusionSet));
  }
  @Deprecated
  public GermanAnalyzer(Version matchVersion, String... stopwords) {
    this(matchVersion, StopFilter.makeStopSet(matchVersion, stopwords));
  }
  @Deprecated
  public GermanAnalyzer(Version matchVersion, Map<?,?> stopwords) {
    this(matchVersion, stopwords.keySet());
  }
  @Deprecated
  public GermanAnalyzer(Version matchVersion, File stopwords) throws IOException {
    this(matchVersion, WordlistLoader.getWordSet(stopwords));
  }
  @Deprecated
  public void setStemExclusionTable(String[] exclusionlist) {
    exclusionSet = StopFilter.makeStopSet(matchVersion, exclusionlist);
    setPreviousTokenStream(null); 
  }
  @Deprecated
  public void setStemExclusionTable(Map<?,?> exclusionlist) {
    exclusionSet = new HashSet<Object>(exclusionlist.keySet());
    setPreviousTokenStream(null); 
  }
  @Deprecated
  public void setStemExclusionTable(File exclusionlist) throws IOException {
    exclusionSet = WordlistLoader.getWordSet(exclusionlist);
    setPreviousTokenStream(null); 
  }
  @Override
  protected TokenStreamComponents createComponents(String fieldName,
      Reader reader) {
    final Tokenizer source = new StandardTokenizer(matchVersion, reader);
    TokenStream result = new StandardFilter(source);
    result = new LowerCaseFilter(matchVersion, result);
    result = new StopFilter( matchVersion, result, stopwords);
    result = new KeywordMarkerTokenFilter(result, exclusionSet);
    if (matchVersion.onOrAfter(Version.LUCENE_31))
      result = new SnowballFilter(result, new German2Stemmer());
    else
      result = new GermanStemFilter(result);
    return new TokenStreamComponents(source, result);
  }
}
