package org.apache.lucene.analysis.cz;
import java.io.IOException;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class CzechStemFilter extends TokenFilter {
  private final CzechStemmer stemmer;
  private final TermAttribute termAtt;
  private final KeywordAttribute keywordAttr;
  public CzechStemFilter(TokenStream input) {
    super(input);
    stemmer = new CzechStemmer();
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
