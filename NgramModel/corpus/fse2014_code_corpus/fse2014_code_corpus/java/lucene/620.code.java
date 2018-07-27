package org.apache.lucene.analysis.ar;
import java.io.IOException;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class ArabicStemFilter extends TokenFilter {
  private final ArabicStemmer stemmer;
  private final TermAttribute termAtt;
  private final KeywordAttribute keywordAttr;
  public ArabicStemFilter(TokenStream input) {
    super(input);
    stemmer = new ArabicStemmer();
    termAtt = addAttribute(TermAttribute.class);
    keywordAttr = addAttribute(KeywordAttribute.class);
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      if(!keywordAttr.isKeyword()) {
        final int newlen = stemmer.stem(termAtt.termBuffer(), termAtt.termLength());
        termAtt.setTermLength(newlen);
      }
      return true;
    } else {
      return false;
    }
  }
}
