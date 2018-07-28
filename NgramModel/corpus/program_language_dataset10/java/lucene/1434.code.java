package org.apache.lucene.analysis;
import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.AttributeSource;
public final class KeywordTokenizer extends Tokenizer {
  private static final int DEFAULT_BUFFER_SIZE = 256;
  private boolean done;
  private int finalOffset;
  private TermAttribute termAtt;
  private OffsetAttribute offsetAtt;
  public KeywordTokenizer(Reader input) {
    this(input, DEFAULT_BUFFER_SIZE);
  }
  public KeywordTokenizer(Reader input, int bufferSize) {
    super(input);
    init(bufferSize);
  }
  public KeywordTokenizer(AttributeSource source, Reader input, int bufferSize) {
    super(source, input);
    init(bufferSize);
  }
  public KeywordTokenizer(AttributeFactory factory, Reader input, int bufferSize) {
    super(factory, input);
    init(bufferSize);
  }
  private void init(int bufferSize) {
    this.done = false;
    termAtt = addAttribute(TermAttribute.class);
    offsetAtt = addAttribute(OffsetAttribute.class);
    termAtt.resizeTermBuffer(bufferSize);    
  }
  @Override
  public final boolean incrementToken() throws IOException {
    if (!done) {
      clearAttributes();
      done = true;
      int upto = 0;
      char[] buffer = termAtt.termBuffer();
      while (true) {
        final int length = input.read(buffer, upto, buffer.length-upto);
        if (length == -1) break;
        upto += length;
        if (upto == buffer.length)
          buffer = termAtt.resizeTermBuffer(1+buffer.length);
      }
      termAtt.setTermLength(upto);
      finalOffset = correctOffset(upto);
      offsetAtt.setOffset(correctOffset(0), finalOffset);
      return true;
    }
    return false;
  }
  @Override
  public final void end() {
    offsetAtt.setOffset(finalOffset, finalOffset);
  }
  @Override
  public void reset(Reader input) throws IOException {
    super.reset(input);
    this.done = false;
  }
}
