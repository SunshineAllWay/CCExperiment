package org.apache.lucene.analysis.cn;
import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.AttributeSource;
@Deprecated
public final class ChineseTokenizer extends Tokenizer {
    public ChineseTokenizer(Reader in) {
      super(in);
      init();
    }
    public ChineseTokenizer(AttributeSource source, Reader in) {
      super(source, in);
      init();
    }
    public ChineseTokenizer(AttributeFactory factory, Reader in) {
      super(factory, in);
      init();
    }
    private void init() {
      termAtt = addAttribute(TermAttribute.class);
      offsetAtt = addAttribute(OffsetAttribute.class);
    }
    private int offset = 0, bufferIndex=0, dataLen=0;
    private final static int MAX_WORD_LEN = 255;
    private final static int IO_BUFFER_SIZE = 1024;
    private final char[] buffer = new char[MAX_WORD_LEN];
    private final char[] ioBuffer = new char[IO_BUFFER_SIZE];
    private int length;
    private int start;
    private TermAttribute termAtt;
    private OffsetAttribute offsetAtt;
    private final void push(char c) {
        if (length == 0) start = offset-1;            
        buffer[length++] = Character.toLowerCase(c);  
    }
    private final boolean flush() {
        if (length>0) {
          termAtt.setTermBuffer(buffer, 0, length);
          offsetAtt.setOffset(correctOffset(start), correctOffset(start+length));
          return true;
        }
        else
            return false;
    }
    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();
        length = 0;
        start = offset;
        while (true) {
            final char c;
            offset++;
            if (bufferIndex >= dataLen) {
                dataLen = input.read(ioBuffer);
                bufferIndex = 0;
            }
            if (dataLen == -1) {
              offset--;
              return flush();
            } else
                c = ioBuffer[bufferIndex++];
            switch(Character.getType(c)) {
            case Character.DECIMAL_DIGIT_NUMBER:
            case Character.LOWERCASE_LETTER:
            case Character.UPPERCASE_LETTER:
                push(c);
                if (length == MAX_WORD_LEN) return flush();
                break;
            case Character.OTHER_LETTER:
                if (length>0) {
                    bufferIndex--;
                    offset--;
                    return flush();
                }
                push(c);
                return flush();
            default:
                if (length>0) return flush();
                break;
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
    }
    @Override
    public void reset(Reader input) throws IOException {
      super.reset(input);
      reset();
    }
}
