package org.apache.lucene.analysis;
import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.CharacterUtils;
import org.apache.lucene.util.Version;
import org.apache.lucene.util.VirtualMethod;
import org.apache.lucene.util.CharacterUtils.CharacterBuffer;
public abstract class CharTokenizer extends Tokenizer {
  public CharTokenizer(Version matchVersion, Reader input) {
    super(input);
    charUtils = CharacterUtils.getInstance(matchVersion);
    offsetAtt = addAttribute(OffsetAttribute.class);
    termAtt = addAttribute(TermAttribute.class);
    useOldAPI = useOldAPI(matchVersion);
    ioBuffer = CharacterUtils.newCharacterBuffer(IO_BUFFER_SIZE);
  }
  public CharTokenizer(Version matchVersion, AttributeSource source,
      Reader input) {
    super(source, input);
    charUtils = CharacterUtils.getInstance(matchVersion);
    offsetAtt = addAttribute(OffsetAttribute.class);
    termAtt = addAttribute(TermAttribute.class);
    useOldAPI = useOldAPI(matchVersion);
    ioBuffer = CharacterUtils.newCharacterBuffer(IO_BUFFER_SIZE);
  }
  public CharTokenizer(Version matchVersion, AttributeFactory factory,
      Reader input) {
    super(factory, input);
    charUtils = CharacterUtils.getInstance(matchVersion);
    offsetAtt = addAttribute(OffsetAttribute.class);
    termAtt = addAttribute(TermAttribute.class);
    useOldAPI = useOldAPI(matchVersion);
    ioBuffer = CharacterUtils.newCharacterBuffer(IO_BUFFER_SIZE);
  }
  @Deprecated
  public CharTokenizer(Reader input) {
    this(Version.LUCENE_30, input);
  }
  @Deprecated
  public CharTokenizer(AttributeSource source, Reader input) {
    this(Version.LUCENE_30, source, input);
  }
  @Deprecated
  public CharTokenizer(AttributeFactory factory, Reader input) {
    this(Version.LUCENE_30, factory, input);
  }
  private int offset = 0, bufferIndex = 0, dataLen = 0;
  private static final int MAX_WORD_LEN = 255;
  private static final int IO_BUFFER_SIZE = 4096;
  private final TermAttribute termAtt;
  private final OffsetAttribute offsetAtt;
  private final CharacterUtils charUtils;
  private final CharacterBuffer ioBuffer;
  @Deprecated
  private final boolean useOldAPI;
  @Deprecated
  private static final VirtualMethod<CharTokenizer> isTokenCharMethod =
    new VirtualMethod<CharTokenizer>(CharTokenizer.class, "isTokenChar", char.class);
  @Deprecated
  private static final VirtualMethod<CharTokenizer> normalizeMethod =
    new VirtualMethod<CharTokenizer>(CharTokenizer.class, "normalize", char.class);
  @Deprecated  
  protected boolean isTokenChar(char c) {
    return isTokenChar((int)c); 
  }
  @Deprecated 
  protected char normalize(char c) {
    return (char) normalize((int) c);
  }
  protected boolean isTokenChar(int c) {
    throw new UnsupportedOperationException("since LUCENE_3_1 subclasses of CharTokenizer must implement isTokenChar(int)");
  }
  protected int normalize(int c) {
    return c;
  }
  @Override
  public final boolean incrementToken() throws IOException {
    clearAttributes();
    if(useOldAPI) 
      return incrementTokenOld();
    int length = 0;
    int start = bufferIndex;
    char[] buffer = termAtt.termBuffer();
    while (true) {
      if (bufferIndex >= dataLen) {
        offset += dataLen;
        if(!charUtils.fill(ioBuffer, input)) { 
          dataLen = 0; 
          if (length > 0)
            break;
          else
            return false;
        }
        dataLen = ioBuffer.getLength();
        bufferIndex = 0;
      }
      final int c = charUtils.codePointAt(ioBuffer.getBuffer(), bufferIndex);
      bufferIndex += Character.charCount(c);
      if (isTokenChar(c)) {               
        if (length == 0)                 
          start = offset + bufferIndex - 1;
        else if (length >= buffer.length-1) 
          buffer = termAtt.resizeTermBuffer(2+length); 
        length += Character.toChars(normalize(c), buffer, length); 
        if (length >= MAX_WORD_LEN) 
          break;
      } else if (length > 0)             
        break;                           
    }
    termAtt.setTermLength(length);
    offsetAtt.setOffset(correctOffset(start), correctOffset(start+length));
    return true;
  }
  @Deprecated
  private boolean incrementTokenOld() throws IOException {
    int length = 0;
    int start = bufferIndex;
    char[] buffer = termAtt.termBuffer();
    final char[] oldIoBuffer = ioBuffer.getBuffer();
    while (true) {
      if (bufferIndex >= dataLen) {
        offset += dataLen;
        dataLen = input.read(oldIoBuffer);
        if (dataLen == -1) {
          dataLen = 0;                            
          if (length > 0)
            break;
          else
            return false;
        }
        bufferIndex = 0;
      }
      final char c = oldIoBuffer[bufferIndex++];
      if (isTokenChar(c)) {               
        if (length == 0)                 
          start = offset + bufferIndex - 1;
        else if (length == buffer.length)
          buffer = termAtt.resizeTermBuffer(1+length);
        buffer[length++] = normalize(c); 
        if (length == MAX_WORD_LEN)      
          break;
      } else if (length > 0)             
        break;                           
    }
    termAtt.setTermLength(length);
    offsetAtt.setOffset(correctOffset(start), correctOffset(start+length));
    return true;
  }  
  @Override
  public final void end() {
    final int finalOffset = correctOffset(offset);
    offsetAtt.setOffset(finalOffset, finalOffset);
  }
  @Override
  public void reset(Reader input) throws IOException {
    super.reset(input);
    bufferIndex = 0;
    offset = 0;
    dataLen = 0;
    ioBuffer.reset(); 
  }
  @Deprecated
  private boolean useOldAPI(Version matchVersion) {
    final Class<? extends CharTokenizer> clazz = this.getClass();
    if (matchVersion.onOrAfter(Version.LUCENE_31)
        && (isTokenCharMethod.isOverriddenAsOf(clazz) || normalizeMethod
            .isOverriddenAsOf(clazz))) throw new IllegalArgumentException(
        "For matchVersion >= LUCENE_31, CharTokenizer subclasses must not override isTokenChar(char) or normalize(char).");
    return !matchVersion.onOrAfter(Version.LUCENE_31);
  } 
}