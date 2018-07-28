package org.apache.lucene.analysis;
import java.io.IOException;
import java.io.Reader;
public abstract class ReusableAnalyzerBase extends Analyzer {
  protected abstract TokenStreamComponents createComponents(String fieldName,
      Reader aReader);
  @Override
  public final TokenStream reusableTokenStream(final String fieldName,
      final Reader reader) throws IOException {
    TokenStreamComponents streamChain = (TokenStreamComponents)
    getPreviousTokenStream();
    if (streamChain == null || !streamChain.reset(reader)) {
      streamChain = createComponents(fieldName, reader);
      setPreviousTokenStream(streamChain);
    }
    return streamChain.getTokenStream();
  }
  @Override
  public final TokenStream tokenStream(final String fieldName,
      final Reader reader) {
    return createComponents(fieldName, reader).getTokenStream();
  }
  public static class TokenStreamComponents {
    final Tokenizer source;
    final TokenStream sink;
    public TokenStreamComponents(final Tokenizer source,
        final TokenStream result) {
      this.source = source;
      this.sink = result;
    }
    public TokenStreamComponents(final Tokenizer source) {
      this.source = source;
      this.sink = source;
    }
    protected boolean reset(final Reader reader) throws IOException {
      source.reset(reader);
      if(sink != source)
        sink.reset(); 
      return true;
    }
    protected TokenStream getTokenStream() {
      return sink;
    }
  }
}
