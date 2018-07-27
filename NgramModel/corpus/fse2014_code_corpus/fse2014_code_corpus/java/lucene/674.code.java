package org.apache.lucene.analysis.miscellaneous;
import org.apache.lucene.analysis.TokenStream;
import java.io.IOException;
public final class EmptyTokenStream extends TokenStream {
  @Override
  public final boolean incrementToken() throws IOException {
    return false;
  }
}
