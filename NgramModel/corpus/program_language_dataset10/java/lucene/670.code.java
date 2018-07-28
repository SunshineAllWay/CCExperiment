package org.apache.lucene.analysis.in;
import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class IndicNormalizationFilter extends TokenFilter {
  private final TermAttribute termAtt = addAttribute(TermAttribute.class);
  private final IndicNormalizer normalizer = new IndicNormalizer();
  public IndicNormalizationFilter(TokenStream input) {
    super(input);
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      termAtt.setTermLength(normalizer.normalize(termAtt.termBuffer(), termAtt.termLength()));
      return true;
    } else {
      return false;
    }
  }
}
