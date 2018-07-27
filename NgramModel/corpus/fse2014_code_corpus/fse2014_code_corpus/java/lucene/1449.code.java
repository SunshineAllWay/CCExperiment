package org.apache.lucene.analysis;
import java.io.IOException;
import java.util.Set;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.util.Version;
public abstract class StopwordAnalyzerBase extends ReusableAnalyzerBase {
  protected final CharArraySet stopwords;
  protected final Version matchVersion;
  public Set<?> getStopwordSet() {
    return stopwords;
  }
  protected StopwordAnalyzerBase(final Version version, final Set<?> stopwords) {
    matchVersion = version;
    this.stopwords = stopwords == null ? CharArraySet.EMPTY_SET : CharArraySet
        .unmodifiableSet(CharArraySet.copy(version, stopwords));
  }
  protected StopwordAnalyzerBase(final Version version) {
    this(version, null);
  }
  protected static CharArraySet loadStopwordSet(final boolean ignoreCase,
      final Class<? extends ReusableAnalyzerBase> aClass, final String resource,
      final String comment) throws IOException {
    final Set<String> wordSet = WordlistLoader.getWordSet(aClass, resource,
        comment);
    final CharArraySet set = new CharArraySet(Version.LUCENE_31, wordSet.size(), ignoreCase);
    set.addAll(wordSet);
    return set;
  }
}
