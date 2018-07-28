package org.apache.lucene.analysis.ar;
import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class ArabicNormalizationFilter extends TokenFilter {
  private final ArabicNormalizer normalizer;
  private final TermAttribute termAtt;
  public ArabicNormalizationFilter(TokenStream input) {
    super(input);
    normalizer = new ArabicNormalizer();
    termAtt = addAttribute(TermAttribute.class);
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      int newlen = normalizer.normalize(termAtt.termBuffer(), termAtt.termLength());
      termAtt.setTermLength(newlen);
      return true;
    }
    return false;
  }
}
