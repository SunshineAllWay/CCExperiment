package org.apache.solr.analysis;
import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public class ReversedWildcardFilter extends TokenFilter {
  private boolean withOriginal;
  private char markerChar;
  private State save;
  private TermAttribute termAtt;
  private PositionIncrementAttribute posAtt;
  protected ReversedWildcardFilter(TokenStream input, boolean withOriginal, char markerChar) {
    super(input);
    this.termAtt = (TermAttribute)addAttribute(TermAttribute.class);
    this.posAtt = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
    this.withOriginal = withOriginal;
    this.markerChar = markerChar;
  }
  @Override
  public boolean incrementToken() throws IOException {
    if( save != null ) {
      restoreState(save);
      save = null;
      return true;
    }
    if (!input.incrementToken()) return false;
    int oldLen = termAtt.termLength();
    if (oldLen ==0) return true;
    int origOffset = posAtt.getPositionIncrement();
    if (withOriginal == true){
      posAtt.setPositionIncrement(0);
      save = captureState();
    }
    char [] buffer = termAtt.resizeTermBuffer(oldLen + 1);
    buffer[oldLen] = markerChar;
    reverse(buffer, 0, oldLen + 1);
    posAtt.setPositionIncrement(origOffset);
    termAtt.setTermBuffer(buffer, 0, oldLen +1);
    return true;
  }
  public static void reverse(final char[] buffer, final int start, final int len) {
    if (len < 2)
      return;
    int end = (start + len) - 1;
    char frontHigh = buffer[start];
    char endLow = buffer[end];
    boolean allowFrontSur = true, allowEndSur = true;
    final int mid = start + (len >> 1);
    for (int i = start; i < mid; ++i, --end) {
      final char frontLow = buffer[i + 1];
      final char endHigh = buffer[end - 1];
      final boolean surAtFront = allowFrontSur
          && Character.isSurrogatePair(frontHigh, frontLow);
      if (surAtFront && (len < 3)) {
        return;
      }
      final boolean surAtEnd = allowEndSur
          && Character.isSurrogatePair(endHigh, endLow);
      allowFrontSur = allowEndSur = true;
      if (surAtFront == surAtEnd) {
        if (surAtFront) {
          buffer[end] = frontLow;
          buffer[--end] = frontHigh;
          buffer[i] = endHigh;
          buffer[++i] = endLow;
          frontHigh = buffer[i + 1];
          endLow = buffer[end - 1];
        } else {
          buffer[end] = frontHigh;
          buffer[i] = endLow;
          frontHigh = frontLow;
          endLow = endHigh;
        }
      } else {
        if (surAtFront) {
          buffer[end] = frontLow;
          buffer[i] = endLow;
          endLow = endHigh;
          allowFrontSur = false;
        } else {
          buffer[end] = frontHigh;
          buffer[i] = endHigh;
          frontHigh = frontLow;
          allowEndSur = false;
        }
      }
    }
    if ((len & 0x01) == 1 && !(allowFrontSur && allowEndSur)) {
      buffer[end] = allowFrontSur ? endLow : frontHigh;
    }
  }
}
