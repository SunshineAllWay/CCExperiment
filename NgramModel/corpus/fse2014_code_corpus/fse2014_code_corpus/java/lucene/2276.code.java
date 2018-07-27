package org.apache.solr.analysis;
import org.apache.lucene.analysis.*;
import java.io.Reader;
import java.io.IOException;
public abstract class SolrAnalyzer extends Analyzer {
  int posIncGap=0;
  public void setPositionIncrementGap(int gap) {
    posIncGap=gap;
  }
  public int getPositionIncrementGap(String fieldName) {
    return posIncGap;
  }
  public Reader charStream(Reader reader){
    return reader;
  }
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    return getStream(fieldName, reader).getTokenStream();
  }
  public static class TokenStreamInfo {
    private final Tokenizer tokenizer;
    private final TokenStream tokenStream;
    public TokenStreamInfo(Tokenizer tokenizer, TokenStream tokenStream) {
      this.tokenizer = tokenizer;
      this.tokenStream = tokenStream;
    }
    public Tokenizer getTokenizer() { return tokenizer; }
    public TokenStream getTokenStream() { return tokenStream; }
  }
  public abstract TokenStreamInfo getStream(String fieldName, Reader reader);
  @Override
  public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
    TokenStreamInfo tsi = (TokenStreamInfo)getPreviousTokenStream();
    if (tsi != null) {
      tsi.getTokenizer().reset(charStream(reader));
      return tsi.getTokenStream();
    } else {
      tsi = getStream(fieldName, reader);
      setPreviousTokenStream(tsi);
      return tsi.getTokenStream();
    }
  }
}
