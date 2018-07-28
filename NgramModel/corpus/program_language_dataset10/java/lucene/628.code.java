package org.apache.lucene.analysis.cjk;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Version;
import java.io.Reader;
import java.util.Arrays;
import java.util.Set;
public final class CJKAnalyzer extends StopwordAnalyzerBase {
  @Deprecated
  public final static String[] STOP_WORDS = {
    "a", "and", "are", "as", "at", "be",
    "but", "by", "for", "if", "in",
    "into", "is", "it", "no", "not",
    "of", "on", "or", "s", "such", "t",
    "that", "the", "their", "then",
    "there", "these", "they", "this",
    "to", "was", "will", "with", "",
    "www"
  };
  public static Set<?> getDefaultStopSet(){
    return DefaultSetHolder.DEFAULT_STOP_SET;
  }
  private static class DefaultSetHolder {
    static final Set<?> DEFAULT_STOP_SET = CharArraySet
        .unmodifiableSet(new CharArraySet(Version.LUCENE_CURRENT, Arrays.asList(STOP_WORDS),
            false));
  }
  public CJKAnalyzer(Version matchVersion) {
    this(matchVersion, DefaultSetHolder.DEFAULT_STOP_SET);
  }
  public CJKAnalyzer(Version matchVersion, Set<?> stopwords){
    super(matchVersion, stopwords);
  }
  @Deprecated
  public CJKAnalyzer(Version matchVersion, String... stopWords) {
    super(matchVersion, StopFilter.makeStopSet(matchVersion, stopWords));
  }
  @Override
  protected TokenStreamComponents createComponents(String fieldName,
      Reader reader) {
    final Tokenizer source = new CJKTokenizer(reader);
    return new TokenStreamComponents(source, new StopFilter(matchVersion, source, stopwords));
  }
}
