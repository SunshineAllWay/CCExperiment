package org.apache.lucene.analysis.ngram;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.AttributeSource;
import java.io.IOException;
import java.io.Reader;
public final class NGramTokenizer extends Tokenizer {
  public static final int DEFAULT_MIN_NGRAM_SIZE = 1;
  public static final int DEFAULT_MAX_NGRAM_SIZE = 2;
  private int minGram, maxGram;
  private int gramSize;
  private int pos = 0;
  private int inLen;
  private String inStr;
  private boolean started = false;
  private TermAttribute termAtt;
  private OffsetAttribute offsetAtt;
  public NGramTokenizer(Reader input, int minGram, int maxGram) {
    super(input);
    init(minGram, maxGram);
  }
  public NGramTokenizer(AttributeSource source, Reader input, int minGram, int maxGram) {
    super(source, input);
    init(minGram, maxGram);
  }
  public NGramTokenizer(AttributeFactory factory, Reader input, int minGram, int maxGram) {
    super(factory, input);
    init(minGram, maxGram);
  }
  public NGramTokenizer(Reader input) {
    this(input, DEFAULT_MIN_NGRAM_SIZE, DEFAULT_MAX_NGRAM_SIZE);
  }
  private void init(int minGram, int maxGram) {
    if (minGram < 1) {
      throw new IllegalArgumentException("minGram must be greater than zero");
    }
    if (minGram > maxGram) {
      throw new IllegalArgumentException("minGram must not be greater than maxGram");
    }
    this.minGram = minGram;
    this.maxGram = maxGram;
    this.termAtt = addAttribute(TermAttribute.class);
    this.offsetAtt = addAttribute(OffsetAttribute.class);    
  }
  @Override
  public final boolean incrementToken() throws IOException {
    clearAttributes();
    if (!started) {
      started = true;
      gramSize = minGram;
      char[] chars = new char[1024];
      input.read(chars);
      inStr = new String(chars).trim();  
      inLen = inStr.length();
    }
    if (pos+gramSize > inLen) {            
      pos = 0;                           
      gramSize++;                        
      if (gramSize > maxGram)            
        return false;
      if (pos+gramSize > inLen)
        return false;
    }
    int oldPos = pos;
    pos++;
    termAtt.setTermBuffer(inStr, oldPos, gramSize);
    offsetAtt.setOffset(correctOffset(oldPos), correctOffset(oldPos+gramSize));
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
    pos = 0;
  }
}
