package org.apache.lucene.analysis;
import java.io.IOException;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class PorterStemFilter extends TokenFilter {
  private final PorterStemmer stemmer;
  private final TermAttribute termAtt;
  private final KeywordAttribute keywordAttr;
  public PorterStemFilter(TokenStream in) {
    super(in);
    stemmer = new PorterStemmer();
    termAtt = addAttribute(TermAttribute.class);
    keywordAttr = addAttribute(KeywordAttribute.class);
  }
  @Override
  public final boolean incrementToken() throws IOException {
    if (!input.incrementToken())
      return false;
    if ((!keywordAttr.isKeyword()) && stemmer.stem(termAtt.termBuffer(), 0, termAtt.termLength()))
      termAtt.setTermBuffer(stemmer.getResultBuffer(), 0, stemmer.getResultLength());
    return true;
  }
}
