package org.apache.lucene.analysis.ngram;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.AttributeSource;
import java.io.IOException;
import java.io.Reader;
public final class EdgeNGramTokenizer extends Tokenizer {
  public static final Side DEFAULT_SIDE = Side.FRONT;
  public static final int DEFAULT_MAX_GRAM_SIZE = 1;
  public static final int DEFAULT_MIN_GRAM_SIZE = 1;
  private TermAttribute termAtt;
  private OffsetAttribute offsetAtt;
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
  private int minGram;
  private int maxGram;
  private int gramSize;
  private Side side;
  private boolean started = false;
  private int inLen;
  private String inStr;
  public EdgeNGramTokenizer(Reader input, Side side, int minGram, int maxGram) {
    super(input);
    init(side, minGram, maxGram);
  }
  public EdgeNGramTokenizer(AttributeSource source, Reader input, Side side, int minGram, int maxGram) {
    super(source, input);
    init(side, minGram, maxGram);
  }
  public EdgeNGramTokenizer(AttributeFactory factory, Reader input, Side side, int minGram, int maxGram) {
    super(factory, input);
    init(side, minGram, maxGram);
  }
  public EdgeNGramTokenizer(Reader input, String sideLabel, int minGram, int maxGram) {
    this(input, Side.getSide(sideLabel), minGram, maxGram);
  }
  public EdgeNGramTokenizer(AttributeSource source, Reader input, String sideLabel, int minGram, int maxGram) {
    this(source, input, Side.getSide(sideLabel), minGram, maxGram);
  }
  public EdgeNGramTokenizer(AttributeFactory factory, Reader input, String sideLabel, int minGram, int maxGram) {
    this(factory, input, Side.getSide(sideLabel), minGram, maxGram);
  }
  private void init(Side side, int minGram, int maxGram) {
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
  @Override
  public final boolean incrementToken() throws IOException {
    clearAttributes();
    if (!started) {
      started = true;
      char[] chars = new char[1024];
      int charsRead = input.read(chars);
      inStr = new String(chars, 0, charsRead).trim();  
      inLen = inStr.length();
      gramSize = minGram;
    }
    if (gramSize > inLen) {
      return false;
    }
    if (gramSize > maxGram) {
      return false;
    }
    int start = side == Side.FRONT ? 0 : inLen - gramSize;
    int end = start + gramSize;
    termAtt.setTermBuffer(inStr, start, gramSize);
    offsetAtt.setOffset(correctOffset(start), correctOffset(end));
    gramSize++;
    return true;
  }
  @Override
  public final void end() {
    final int finalOffset = inLen;
    this.offsetAtt.setOffset(finalOffset, finalOffset);
  }    
  @Override
  public void reset(Reader input) throws IOException {
    super.reset(input);
    reset();
  }
  @Override
  public void reset() throws IOException {
    super.reset();
    started = false;
  }
}
