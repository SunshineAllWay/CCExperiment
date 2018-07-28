package org.apache.lucene.analysis.hi;
import java.io.IOException;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class HindiNormalizationFilter extends TokenFilter {
  private final HindiNormalizer normalizer = new HindiNormalizer();
  private final TermAttribute termAtt = addAttribute(TermAttribute.class);
  private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);
  public HindiNormalizationFilter(TokenStream input) {
    super(input);
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      if (!keywordAtt.isKeyword())
        termAtt.setTermLength(normalizer.normalize(termAtt.termBuffer(), 
            termAtt.termLength()));
      return true;
    } 
    return false;
  }
}
