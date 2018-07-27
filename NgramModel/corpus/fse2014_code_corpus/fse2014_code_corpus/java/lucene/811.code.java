package org.apache.lucene.analysis.cn.smart;
import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
public final class SentenceTokenizer extends Tokenizer {
  private final static String PUNCTION = "。，！？；,!?;";
  private final StringBuilder buffer = new StringBuilder();
  private int tokenStart = 0, tokenEnd = 0;
  private TermAttribute termAtt;
  private OffsetAttribute offsetAtt;
  private TypeAttribute typeAtt;
  public SentenceTokenizer(Reader reader) {
    super(reader);
    init();
  }
  public SentenceTokenizer(AttributeSource source, Reader reader) {
    super(source, reader);
    init();
  }
  public SentenceTokenizer(AttributeFactory factory, Reader reader) {
    super(factory, reader);
    init();
  }
  private void init() {
    termAtt = addAttribute(TermAttribute.class);
    offsetAtt = addAttribute(OffsetAttribute.class);
    typeAtt = addAttribute(TypeAttribute.class);    
  }
  @Override
  public boolean incrementToken() throws IOException {
    clearAttributes();
    buffer.setLength(0);
    int ci;
    char ch, pch;
    boolean atBegin = true;
    tokenStart = tokenEnd;
    ci = input.read();
    ch = (char) ci;
    while (true) {
      if (ci == -1) {
        break;
      } else if (PUNCTION.indexOf(ch) != -1) {
        buffer.append(ch);
        tokenEnd++;
        break;
      } else if (atBegin && Utility.SPACES.indexOf(ch) != -1) {
        tokenStart++;
        tokenEnd++;
        ci = input.read();
        ch = (char) ci;
      } else {
        buffer.append(ch);
        atBegin = false;
        tokenEnd++;
        pch = ch;
        ci = input.read();
        ch = (char) ci;
        if (Utility.SPACES.indexOf(ch) != -1
            && Utility.SPACES.indexOf(pch) != -1) {
          tokenEnd++;
          break;
        }
      }
    }
    if (buffer.length() == 0)
      return false;
    else {
      termAtt.setTermBuffer(buffer.toString());
      offsetAtt.setOffset(correctOffset(tokenStart), correctOffset(tokenEnd));
      typeAtt.setType("sentence");
      return true;
    }
  }
  @Override
  public void reset() throws IOException {
    super.reset();
    tokenStart = tokenEnd = 0;
  }
  @Override
  public void reset(Reader input) throws IOException {
    super.reset(input);
    reset();
  }
  @Override
  public void end() throws IOException {
    final int finalOffset = correctOffset(tokenEnd);
    offsetAtt.setOffset(finalOffset, finalOffset);
  }
}
