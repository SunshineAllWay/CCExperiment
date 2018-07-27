package org.apache.lucene.analysis.hi;
import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class HindiStemFilter extends TokenFilter {
  private final TermAttribute termAtt = addAttribute(TermAttribute.class);
  private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);
  private final HindiStemmer stemmer = new HindiStemmer();
  public HindiStemFilter(TokenStream input) {
    super(input);
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      if (!keywordAtt.isKeyword())
        termAtt.setTermLength(stemmer.stem(termAtt.termBuffer(), termAtt.termLength()));
      return true;
    } else {
      return false;
    }
  }
}
