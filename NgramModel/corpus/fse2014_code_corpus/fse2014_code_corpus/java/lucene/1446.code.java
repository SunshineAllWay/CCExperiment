package org.apache.lucene.analysis;
import java.io.Reader;
import org.apache.lucene.util.Version;
public final class SimpleAnalyzer extends ReusableAnalyzerBase {
  private final Version matchVersion;
  public SimpleAnalyzer(Version matchVersion) {
    this.matchVersion = matchVersion;
  }
  @Deprecated  public SimpleAnalyzer() {
    this(Version.LUCENE_30);
  }
  @Override
  protected TokenStreamComponents createComponents(final String fieldName,
      final Reader reader) {
    return new TokenStreamComponents(new LowerCaseTokenizer(matchVersion, reader));
  }
}
