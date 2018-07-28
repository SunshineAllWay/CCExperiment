package org.apache.lucene.analysis.standard;
import org.apache.lucene.analysis.*;
import org.apache.lucene.util.Version;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;
public class StandardAnalyzer extends Analyzer {
  private Set<?> stopSet;
  private final boolean replaceInvalidAcronym;
  public static final Set<?> STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET; 
  private final Version matchVersion;
  public StandardAnalyzer(Version matchVersion) {
    this(matchVersion, STOP_WORDS_SET);
  }
  public StandardAnalyzer(Version matchVersion, Set<?> stopWords) {
    stopSet = stopWords;
    replaceInvalidAcronym = matchVersion.onOrAfter(Version.LUCENE_24);
    this.matchVersion = matchVersion;
  }
  public StandardAnalyzer(Version matchVersion, File stopwords) throws IOException {
    this(matchVersion, WordlistLoader.getWordSet(stopwords));
  }
  public StandardAnalyzer(Version matchVersion, Reader stopwords) throws IOException {
    this(matchVersion, WordlistLoader.getWordSet(stopwords));
  }
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    StandardTokenizer tokenStream = new StandardTokenizer(matchVersion, reader);
    tokenStream.setMaxTokenLength(maxTokenLength);
    TokenStream result = new StandardFilter(tokenStream);
    result = new LowerCaseFilter(matchVersion, result);
    result = new StopFilter(matchVersion, result, stopSet);
    return result;
  }
  private static final class SavedStreams {
    StandardTokenizer tokenStream;
    TokenStream filteredTokenStream;
  }
  public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;
  private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;
  public void setMaxTokenLength(int length) {
    maxTokenLength = length;
  }
  public int getMaxTokenLength() {
    return maxTokenLength;
  }
  @Override
  public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
    if (overridesTokenStreamMethod) {
      return tokenStream(fieldName, reader);
    }
    SavedStreams streams = (SavedStreams) getPreviousTokenStream();
    if (streams == null) {
      streams = new SavedStreams();
      setPreviousTokenStream(streams);
      streams.tokenStream = new StandardTokenizer(matchVersion, reader);
      streams.filteredTokenStream = new StandardFilter(streams.tokenStream);
      streams.filteredTokenStream = new LowerCaseFilter(matchVersion,
          streams.filteredTokenStream);
      streams.filteredTokenStream = new StopFilter(matchVersion, streams.filteredTokenStream, stopSet);
    } else {
      streams.tokenStream.reset(reader);
    }
    streams.tokenStream.setMaxTokenLength(maxTokenLength);
    streams.tokenStream.setReplaceInvalidAcronym(replaceInvalidAcronym);
    return streams.filteredTokenStream;
  }
}
