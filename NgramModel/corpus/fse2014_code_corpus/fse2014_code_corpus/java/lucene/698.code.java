package org.apache.lucene.analysis.position;
import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
public final class PositionFilter extends TokenFilter {
  private int positionIncrement = 0;
  private boolean firstTokenPositioned = false;
  private PositionIncrementAttribute posIncrAtt;
  public PositionFilter(final TokenStream input) {
    super(input);
    posIncrAtt = addAttribute(PositionIncrementAttribute.class);
  }
  public PositionFilter(final TokenStream input, final int positionIncrement) {
    this(input);
    this.positionIncrement = positionIncrement;
  }
  @Override
  public final boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      if (firstTokenPositioned) {
        posIncrAtt.setPositionIncrement(positionIncrement);
      } else {
        firstTokenPositioned = true;
      }
      return true;
    } else {
      return false;
    }
  }
  @Override
  public void reset() throws IOException {
    super.reset();
    firstTokenPositioned = false;
  }
}
