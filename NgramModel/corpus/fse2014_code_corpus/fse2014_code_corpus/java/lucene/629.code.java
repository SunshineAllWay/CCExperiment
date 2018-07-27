package org.apache.lucene.analysis.cjk;
import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
public final class CJKTokenizer extends Tokenizer {
    static final int WORD_TYPE = 0;
    static final int SINGLE_TOKEN_TYPE = 1;
    static final int DOUBLE_TOKEN_TYPE = 2;
    static final String[] TOKEN_TYPE_NAMES = { "word", "single", "double" };
    private static final int MAX_WORD_LEN = 255;
    private static final int IO_BUFFER_SIZE = 256;
    private int offset = 0;
    private int bufferIndex = 0;
    private int dataLen = 0;
    private final char[] buffer = new char[MAX_WORD_LEN];
    private final char[] ioBuffer = new char[IO_BUFFER_SIZE];
    private int tokenType = WORD_TYPE;
    private boolean preIsTokened = false;
    private TermAttribute termAtt;
    private OffsetAttribute offsetAtt;
    private TypeAttribute typeAtt;
    public CJKTokenizer(Reader in) {
      super(in);
      init();
    }
    public CJKTokenizer(AttributeSource source, Reader in) {
      super(source, in);
      init();
    }
    public CJKTokenizer(AttributeFactory factory, Reader in) {
      super(factory, in);
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
        while(true) { 
          int length = 0;
          int start = offset;
          while (true) { 
            char c;
            Character.UnicodeBlock ub;
            offset++;
            if (bufferIndex >= dataLen) {
                dataLen = input.read(ioBuffer);
                bufferIndex = 0;
            }
            if (dataLen == -1) {
                if (length > 0) {
                    if (preIsTokened == true) {
                        length = 0;
                        preIsTokened = false;
                    }
                    else{
                      offset--;
                    }
                    break;
                } else {
                    offset--;
                    return false;
                }
            } else {
                c = ioBuffer[bufferIndex++];
                ub = Character.UnicodeBlock.of(c);
            }
            if ((ub == Character.UnicodeBlock.BASIC_LATIN)
                    || (ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS)
               ) {
                if (ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
                  int i = (int) c;
                  if (i >= 65281 && i <= 65374) {
                    i = i - 65248;
                    c = (char) i;
                  }
                }
                if (Character.isLetterOrDigit(c)
                        || ((c == '_') || (c == '+') || (c == '#'))
                   ) {
                    if (length == 0) {
                        start = offset - 1;
                    } else if (tokenType == DOUBLE_TOKEN_TYPE) {
                        offset--;
                        bufferIndex--;
                        if (preIsTokened == true) {
                            length = 0;
                            preIsTokened = false;
                            break;
                        } else {
                            break;
                        }
                    }
                    buffer[length++] = Character.toLowerCase(c);
                    tokenType = SINGLE_TOKEN_TYPE;
                    if (length == MAX_WORD_LEN) {
                        break;
                    }
                } else if (length > 0) {
                    if (preIsTokened == true) {
                        length = 0;
                        preIsTokened = false;
                    } else {
                        break;
                    }
                }
            } else {
                if (Character.isLetter(c)) {
                    if (length == 0) {
                        start = offset - 1;
                        buffer[length++] = c;
                        tokenType = DOUBLE_TOKEN_TYPE;
                    } else {
                      if (tokenType == SINGLE_TOKEN_TYPE) {
                            offset--;
                            bufferIndex--;
                            break;
                        } else {
                            buffer[length++] = c;
                            tokenType = DOUBLE_TOKEN_TYPE;
                            if (length == 2) {
                                offset--;
                                bufferIndex--;
                                preIsTokened = true;
                                break;
                            }
                        }
                    }
                } else if (length > 0) {
                    if (preIsTokened == true) {
                        length = 0;
                        preIsTokened = false;
                    } else {
                        break;
                    }
                }
            }
        }
        if (length > 0) {
          termAtt.setTermBuffer(buffer, 0, length);
          offsetAtt.setOffset(correctOffset(start), correctOffset(start+length));
          typeAtt.setType(TOKEN_TYPE_NAMES[tokenType]);
          return true;
        } else if (dataLen == -1) {
          offset--;
          return false;
        }
      }
    }
    @Override
    public final void end() {
      final int finalOffset = correctOffset(offset);
      this.offsetAtt.setOffset(finalOffset, finalOffset);
    }
    @Override
    public void reset() throws IOException {
      super.reset();
      offset = bufferIndex = dataLen = 0;
      preIsTokened = false;
      tokenType = WORD_TYPE;
    }
    @Override
    public void reset(Reader reader) throws IOException {
      super.reset(reader);
      reset();
    }
}
