package org.apache.solr.analysis;
import java.io.IOException;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class HyphenatedWordsFilter extends TokenFilter {
  private final TermAttribute termAttribute = (TermAttribute) addAttribute(TermAttribute.class);
  private final OffsetAttribute offsetAttribute = (OffsetAttribute) addAttribute(OffsetAttribute.class);
  private final StringBuilder hyphenated = new StringBuilder();
  private State savedState;
  public HyphenatedWordsFilter(TokenStream in) {
    super(in);
  }
  @Override
  public boolean incrementToken() throws IOException {
    while (input.incrementToken()) {
      char[] term = termAttribute.termBuffer();
      int termLength = termAttribute.termLength();
      if (termLength > 0 && term[termLength - 1] == '-') {
        if (savedState == null) {
          savedState = captureState();
        }
        hyphenated.append(term, 0, termLength - 1);
      } else if (savedState == null) {
        return true;
      } else {
        hyphenated.append(term, 0, termLength);
        unhyphenate();
        return true;
      }
    }
    if (savedState != null) {
      hyphenated.append('-');
      unhyphenate();
      return true;
    }
    return false;
  }
  @Override
  public void reset() throws IOException {
    super.reset();
    hyphenated.setLength(0);
    savedState = null;
  }
  private void unhyphenate() {
    int endOffset = offsetAttribute.endOffset();
    restoreState(savedState);
    savedState = null;
    char term[] = termAttribute.termBuffer();
    int length = hyphenated.length();
    if (length > termAttribute.termLength()) {
      term = termAttribute.resizeTermBuffer(length);
    }
    hyphenated.getChars(0, length, term, 0);
    termAttribute.setTermLength(length);
    offsetAttribute.setOffset(offsetAttribute.startOffset(), endOffset);
    hyphenated.setLength(0);
  }
}
