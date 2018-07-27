package org.apache.lucene.analysis.miscellaneous;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import java.io.IOException;
public class PrefixAndSuffixAwareTokenFilter extends TokenStream {
  private PrefixAwareTokenFilter suffix;
  public PrefixAndSuffixAwareTokenFilter(TokenStream prefix, TokenStream input, TokenStream suffix) {
    super(suffix);
    prefix = new PrefixAwareTokenFilter(prefix, input) {
      @Override
      public Token updateSuffixToken(Token suffixToken, Token lastInputToken) {
        return PrefixAndSuffixAwareTokenFilter.this.updateInputToken(suffixToken, lastInputToken);
      }
    };
    this.suffix = new PrefixAwareTokenFilter(prefix, suffix) {
      @Override
      public Token updateSuffixToken(Token suffixToken, Token lastInputToken) {
        return PrefixAndSuffixAwareTokenFilter.this.updateSuffixToken(suffixToken, lastInputToken);
      }
    };
  }
  public Token updateInputToken(Token inputToken, Token lastPrefixToken) {
    inputToken.setStartOffset(lastPrefixToken.endOffset() + inputToken.startOffset());
    inputToken.setEndOffset(lastPrefixToken.endOffset() + inputToken.endOffset());
    return inputToken;
  }
  public Token updateSuffixToken(Token suffixToken, Token lastInputToken) {
    suffixToken.setStartOffset(lastInputToken.endOffset() + suffixToken.startOffset());
    suffixToken.setEndOffset(lastInputToken.endOffset() + suffixToken.endOffset());
    return suffixToken;
  }
  @Override
  public final boolean incrementToken() throws IOException {
    return suffix.incrementToken();
  }
  @Override
  public void reset() throws IOException {
    suffix.reset();
  }
  @Override
  public void close() throws IOException {
    suffix.close();
  }
}
