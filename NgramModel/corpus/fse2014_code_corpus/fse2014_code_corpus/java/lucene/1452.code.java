package org.apache.lucene.analysis;
import java.io.IOException;
public abstract class TokenFilter extends TokenStream {
  protected final TokenStream input;
  protected TokenFilter(TokenStream input) {
    super(input);
    this.input = input;
  }
  @Override
  public void end() throws IOException {
    input.end();
  }
  @Override
  public void close() throws IOException {
    input.close();
  }
  @Override
  public void reset() throws IOException {
    input.reset();
  }
}
