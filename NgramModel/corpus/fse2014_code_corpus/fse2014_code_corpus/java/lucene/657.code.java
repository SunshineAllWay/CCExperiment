package org.apache.lucene.analysis.fa;
import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class PersianNormalizationFilter extends TokenFilter {
  private final PersianNormalizer normalizer;
  private final TermAttribute termAtt;
  public PersianNormalizationFilter(TokenStream input) {
    super(input);
    normalizer = new PersianNormalizer();
    termAtt = addAttribute(TermAttribute.class);
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      final int newlen = normalizer.normalize(termAtt.termBuffer(), termAtt
          .termLength());
      termAtt.setTermLength(newlen);
      return true;
    } 
    return false;
  }
}
