package org.apache.lucene.analysis.snowball;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.analysis.tr.TurkishLowerCaseFilter;
import org.apache.lucene.util.Version;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;
@Deprecated
public final class SnowballAnalyzer extends Analyzer {
  private String name;
  private Set<?> stopSet;
  private final Version matchVersion;
  public SnowballAnalyzer(Version matchVersion, String name) {
    this.name = name;
    this.matchVersion = matchVersion;
  }
  @Deprecated
  public SnowballAnalyzer(Version matchVersion, String name, String[] stopWords) {
    this(matchVersion, name);
    stopSet = StopFilter.makeStopSet(matchVersion, stopWords);
  }
  public SnowballAnalyzer(Version matchVersion, String name, Set<?> stopWords) {
    this(matchVersion, name);
    stopSet = CharArraySet.unmodifiableSet(CharArraySet.copy(matchVersion,
        stopWords));
  }
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    TokenStream result = new StandardTokenizer(matchVersion, reader);
    result = new StandardFilter(result);
    if (matchVersion.onOrAfter(Version.LUCENE_31) && name.equals("Turkish"))
      result = new TurkishLowerCaseFilter(result);
    else
      result = new LowerCaseFilter(matchVersion, result);
    if (stopSet != null)
      result = new StopFilter(matchVersion,
                              result, stopSet);
    result = new SnowballFilter(result, name);
    return result;
  }
  private class SavedStreams {
    Tokenizer source;
    TokenStream result;
  }
  @Override
  public TokenStream reusableTokenStream(String fieldName, Reader reader)
      throws IOException {
    SavedStreams streams = (SavedStreams) getPreviousTokenStream();
    if (streams == null) {
      streams = new SavedStreams();
      streams.source = new StandardTokenizer(matchVersion, reader);
      streams.result = new StandardFilter(streams.source);
      if (matchVersion.onOrAfter(Version.LUCENE_31) && name.equals("Turkish"))
        streams.result = new TurkishLowerCaseFilter(streams.result);
      else
        streams.result = new LowerCaseFilter(matchVersion, streams.result);
      if (stopSet != null)
        streams.result = new StopFilter(matchVersion,
                                        streams.result, stopSet);
      streams.result = new SnowballFilter(streams.result, name);
      setPreviousTokenStream(streams);
    } else {
      streams.source.reset(reader);
    }
    return streams.result;
  }
}
