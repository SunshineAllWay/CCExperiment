package org.apache.lucene.analysis.th;
import java.io.Reader;
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
public final class ThaiAnalyzer extends ReusableAnalyzerBase {
  private final Version matchVersion;
  public ThaiAnalyzer(Version matchVersion) {
    this.matchVersion = matchVersion;
  }
  @Override
  protected TokenStreamComponents createComponents(String fieldName,
      Reader reader) {
    final Tokenizer source = new StandardTokenizer(matchVersion, reader);
    TokenStream result = new StandardFilter(source);
    result = new ThaiWordFilter(result);
    return new TokenStreamComponents(source, new StopFilter(matchVersion,
        result, StopAnalyzer.ENGLISH_STOP_WORDS_SET));
  }
}
