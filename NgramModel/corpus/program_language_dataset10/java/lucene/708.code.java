package org.apache.lucene.analysis.shingle;
import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
public final class ShingleAnalyzerWrapper extends Analyzer {
  private final Analyzer defaultAnalyzer;
  private int maxShingleSize = ShingleFilter.DEFAULT_MAX_SHINGLE_SIZE;
  private int minShingleSize = ShingleFilter.DEFAULT_MIN_SHINGLE_SIZE;
  private String tokenSeparator = ShingleFilter.TOKEN_SEPARATOR;
  private boolean outputUnigrams = true;
  public ShingleAnalyzerWrapper(Analyzer defaultAnalyzer) {
    super();
    this.defaultAnalyzer = defaultAnalyzer;
  }
  public ShingleAnalyzerWrapper(Analyzer defaultAnalyzer, int maxShingleSize) {
    this(defaultAnalyzer);
    setMaxShingleSize(maxShingleSize);
  }
  public ShingleAnalyzerWrapper(Analyzer defaultAnalyzer, int minShingleSize, int maxShingleSize) {
    this(defaultAnalyzer);
    setMaxShingleSize(maxShingleSize);
    setMinShingleSize(minShingleSize);
  }
  public ShingleAnalyzerWrapper(Version matchVersion) {
    super();
    this.defaultAnalyzer = new StandardAnalyzer(matchVersion);
  }
  public ShingleAnalyzerWrapper(Version matchVersion, int minShingleSize, int maxShingleSize) {
    this(matchVersion);
    setMaxShingleSize(maxShingleSize);
    setMinShingleSize(minShingleSize);
  }
  public int getMaxShingleSize() {
    return maxShingleSize;
  }
  public void setMaxShingleSize(int maxShingleSize) {
    if (maxShingleSize < 2) {
      throw new IllegalArgumentException("Max shingle size must be >= 2");
    }
    this.maxShingleSize = maxShingleSize;
  }
  public int getMinShingleSize() {
    return minShingleSize;
  }
  public void setMinShingleSize(int minShingleSize) {
    if (minShingleSize < 2) {
      throw new IllegalArgumentException("Min shingle size must be >= 2");
    }
    if (minShingleSize > maxShingleSize) {
      throw new IllegalArgumentException
        ("Min shingle size must be <= max shingle size");
    }
    this.minShingleSize = minShingleSize;
  }
  public String getTokenSeparator() {
    return tokenSeparator;
  }
  public void setTokenSeparator(String tokenSeparator) {
    this.tokenSeparator = (tokenSeparator == null ? "" : tokenSeparator);
  }
  public boolean isOutputUnigrams() {
    return outputUnigrams;
  }
  public void setOutputUnigrams(boolean outputUnigrams) {
    this.outputUnigrams = outputUnigrams;
  }
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    TokenStream wrapped;
    try {
      wrapped = defaultAnalyzer.reusableTokenStream(fieldName, reader);
    } catch (IOException e) {
      wrapped = defaultAnalyzer.tokenStream(fieldName, reader);
    }
    ShingleFilter filter = new ShingleFilter(wrapped, minShingleSize, maxShingleSize);
    filter.setMinShingleSize(minShingleSize);
    filter.setMaxShingleSize(maxShingleSize);
    filter.setTokenSeparator(tokenSeparator);
    filter.setOutputUnigrams(outputUnigrams);
    return filter;
  }
  private class SavedStreams {
    TokenStream wrapped;
    ShingleFilter shingle;
  }
  @Override
  public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
    SavedStreams streams = (SavedStreams) getPreviousTokenStream();
    if (streams == null) {
      streams = new SavedStreams();
      streams.wrapped = defaultAnalyzer.reusableTokenStream(fieldName, reader);
      streams.shingle = new ShingleFilter(streams.wrapped);
      setPreviousTokenStream(streams);
    } else {
      TokenStream result = defaultAnalyzer.reusableTokenStream(fieldName, reader);
      if (result == streams.wrapped) {
        streams.shingle.reset(); 
      } else {
        streams.wrapped = result;
        streams.shingle = new ShingleFilter(streams.wrapped);
      }
    }
    streams.shingle.setMaxShingleSize(maxShingleSize);
    streams.shingle.setMinShingleSize(minShingleSize);
    streams.shingle.setTokenSeparator(tokenSeparator);
    streams.shingle.setOutputUnigrams(outputUnigrams);
    return streams.shingle;
  }
}
