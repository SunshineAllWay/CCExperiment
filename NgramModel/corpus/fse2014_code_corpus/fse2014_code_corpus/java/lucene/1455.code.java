package org.apache.lucene.analysis;
import java.io.Reader;
import org.apache.lucene.util.Version;
public final class WhitespaceAnalyzer extends ReusableAnalyzerBase {
  private final Version matchVersion;
  public WhitespaceAnalyzer(Version matchVersion) {
    this.matchVersion = matchVersion;
  }
  @Deprecated
  public WhitespaceAnalyzer() {
    this(Version.LUCENE_30);
  }
  @Override
  protected TokenStreamComponents createComponents(final String fieldName,
      final Reader reader) {
    return new TokenStreamComponents(new WhitespaceTokenizer(matchVersion, reader));
  }
}
