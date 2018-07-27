package org.apache.lucene.analysis.ngram;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import java.io.IOException;
public final class EdgeNGramTokenFilter extends TokenFilter {
  public static final Side DEFAULT_SIDE = Side.FRONT;
  public static final int DEFAULT_MAX_GRAM_SIZE = 1;
  public static final int DEFAULT_MIN_GRAM_SIZE = 1;
  public static enum Side {
    FRONT {
      @Override
      public String getLabel() { return "front"; }
    },
    BACK  {
      @Override
      public String getLabel() { return "back"; }
    };
    public abstract String getLabel();
    public static Side getSide(String sideName) {
      if (FRONT.getLabel().equals(sideName)) {
        return FRONT;
      }
      if (BACK.getLabel().equals(sideName)) {
        return BACK;
      }
      return null;
    }
  }
  private final int minGram;
  private final int maxGram;
  private Side side;
  private char[] curTermBuffer;
  private int curTermLength;
  private int curGramSize;
  private int tokStart;
  private final TermAttribute termAtt;
  private final OffsetAttribute offsetAtt;
  public EdgeNGramTokenFilter(TokenStream input, Side side, int minGram, int maxGram) {
    super(input);
    if (side == null) {
      throw new IllegalArgumentException("sideLabel must be either front or back");
    }
    if (minGram < 1) {
      throw new IllegalArgumentException("minGram must be greater than zero");
    }
    if (minGram > maxGram) {
      throw new IllegalArgumentException("minGram must not be greater than maxGram");
    }
    this.minGram = minGram;
    this.maxGram = maxGram;
    this.side = side;
    this.termAtt = addAttribute(TermAttribute.class);
    this.offsetAtt = addAttribute(OffsetAttribute.class);
  }
  public EdgeNGramTokenFilter(TokenStream input, String sideLabel, int minGram, int maxGram) {
    this(input, Side.getSide(sideLabel), minGram, maxGram);
  }
  @Override
  public final boolean incrementToken() throws IOException {
    while (true) {
      if (curTermBuffer == null) {
        if (!input.incrementToken()) {
          return false;
        } else {
          curTermBuffer = termAtt.termBuffer().clone();
          curTermLength = termAtt.termLength();
          curGramSize = minGram;
          tokStart = offsetAtt.startOffset();
        }
      }
      if (curGramSize <= maxGram) {
        if (! (curGramSize > curTermLength         
            || curGramSize > maxGram)) {       
          int start = side == Side.FRONT ? 0 : curTermLength - curGramSize;
          int end = start + curGramSize;
          clearAttributes();
          offsetAtt.setOffset(tokStart + start, tokStart + end);
          termAtt.setTermBuffer(curTermBuffer, start, curGramSize);
          curGramSize++;
          return true;
        }
      }
      curTermBuffer = null;
    }
  }
  @Override
  public void reset() throws IOException {
    super.reset();
    curTermBuffer = null;
  }
}
