package org.apache.lucene.analysis;
import java.io.IOException;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class LengthFilter extends TokenFilter {
  final int min;
  final int max;
  private TermAttribute termAtt;
  public LengthFilter(TokenStream in, int min, int max)
  {
    super(in);
    this.min = min;
    this.max = max;
    termAtt = addAttribute(TermAttribute.class);
  }
  @Override
  public final boolean incrementToken() throws IOException {
    while (input.incrementToken()) {
      int len = termAtt.termLength();
      if (len >= min && len <= max) {
          return true;
      }
    }
    return false;
  }
}
