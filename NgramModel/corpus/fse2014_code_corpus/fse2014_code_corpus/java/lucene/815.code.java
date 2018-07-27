package org.apache.lucene.analysis.cn.smart;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.hhmm.SegToken;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
public final class WordTokenFilter extends TokenFilter {
  private WordSegmenter wordSegmenter;
  private Iterator<SegToken> tokenIter;
  private List<SegToken> tokenBuffer;
  private TermAttribute termAtt;
  private OffsetAttribute offsetAtt;
  private TypeAttribute typeAtt;
  public WordTokenFilter(TokenStream in) {
    super(in);
    this.wordSegmenter = new WordSegmenter();
    termAtt = addAttribute(TermAttribute.class);
    offsetAtt = addAttribute(OffsetAttribute.class);
    typeAtt = addAttribute(TypeAttribute.class);
  }
  @Override
  public boolean incrementToken() throws IOException {   
    if (tokenIter == null || !tokenIter.hasNext()) {
      if (input.incrementToken()) {
        tokenBuffer = wordSegmenter.segmentSentence(termAtt.term(), offsetAtt.startOffset());
        tokenIter = tokenBuffer.iterator();
        if (!tokenIter.hasNext())
          return false;
      } else {
        return false; 
      }
    } 
    clearAttributes();
    SegToken nextWord = tokenIter.next();
    termAtt.setTermBuffer(nextWord.charArray, 0, nextWord.charArray.length);
    offsetAtt.setOffset(nextWord.startOffset, nextWord.endOffset);
    typeAtt.setType("word");
    return true;
  }
  @Override
  public void reset() throws IOException {
    super.reset();
    tokenIter = null;
  }
}
