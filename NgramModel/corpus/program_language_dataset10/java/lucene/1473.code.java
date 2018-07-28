package org.apache.lucene.analysis.tokenattributes;
import java.io.Serializable;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.RamUsageEstimator;
public class TermAttributeImpl extends AttributeImpl implements TermAttribute, Cloneable, Serializable {
  private static int MIN_BUFFER_SIZE = 10;
  private char[] termBuffer;
  private int termLength;
  public String term() {
    initTermBuffer();
    return new String(termBuffer, 0, termLength);
  }
  public void setTermBuffer(char[] buffer, int offset, int length) {
    growTermBuffer(length);
    System.arraycopy(buffer, offset, termBuffer, 0, length);
    termLength = length;
  }
  public void setTermBuffer(String buffer) {
    int length = buffer.length();
    growTermBuffer(length);
    buffer.getChars(0, length, termBuffer, 0);
    termLength = length;
  }
  public void setTermBuffer(String buffer, int offset, int length) {
    assert offset <= buffer.length();
    assert offset + length <= buffer.length();
    growTermBuffer(length);
    buffer.getChars(offset, offset + length, termBuffer, 0);
    termLength = length;
  }
  public char[] termBuffer() {
    initTermBuffer();
    return termBuffer;
  }
  public char[] resizeTermBuffer(int newSize) {
    if (termBuffer == null) {
      termBuffer = new char[ArrayUtil.oversize(newSize < MIN_BUFFER_SIZE ? MIN_BUFFER_SIZE : newSize, RamUsageEstimator.NUM_BYTES_CHAR)]; 
    } else {
      if(termBuffer.length < newSize){
        final char[] newCharBuffer = new char[ArrayUtil.oversize(newSize, RamUsageEstimator.NUM_BYTES_CHAR)];
        System.arraycopy(termBuffer, 0, newCharBuffer, 0, termBuffer.length);
        termBuffer = newCharBuffer;
      }
    } 
    return termBuffer;   
  }
  private void growTermBuffer(int newSize) {
    if (termBuffer == null) {
      termBuffer = new char[ArrayUtil.oversize(newSize < MIN_BUFFER_SIZE ? MIN_BUFFER_SIZE : newSize, RamUsageEstimator.NUM_BYTES_CHAR)];   
    } else {
      if(termBuffer.length < newSize){
        termBuffer = new char[ArrayUtil.oversize(newSize, RamUsageEstimator.NUM_BYTES_CHAR)];
      }
    } 
  }
  private void initTermBuffer() {
    if (termBuffer == null) {
      termBuffer = new char[ArrayUtil.oversize(MIN_BUFFER_SIZE, RamUsageEstimator.NUM_BYTES_CHAR)];
      termLength = 0;
    }
  }
  public int termLength() {
    return termLength;
  }
  public void setTermLength(int length) {
    initTermBuffer();
    if (length > termBuffer.length)
      throw new IllegalArgumentException("length " + length + " exceeds the size of the termBuffer (" + termBuffer.length + ")");
    termLength = length;
  }
  @Override
  public int hashCode() {
    initTermBuffer();
    int code = termLength;
    code = code * 31 + ArrayUtil.hashCode(termBuffer, 0, termLength);
    return code;
  }
  @Override
  public void clear() {
    termLength = 0;    
  }
  @Override
  public Object clone() {
    TermAttributeImpl t = (TermAttributeImpl)super.clone();
    if (termBuffer != null) {
      t.termBuffer = termBuffer.clone();
    }
    return t;
  }
  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (other instanceof TermAttributeImpl) {
      initTermBuffer();
      TermAttributeImpl o = ((TermAttributeImpl) other);
      o.initTermBuffer();
      if (termLength != o.termLength)
        return false;
      for(int i=0;i<termLength;i++) {
        if (termBuffer[i] != o.termBuffer[i]) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
  @Override
  public String toString() {
    initTermBuffer();
    return "term=" + new String(termBuffer, 0, termLength);
  }
  @Override
  public void copyTo(AttributeImpl target) {
    initTermBuffer();
    TermAttribute t = (TermAttribute) target;
    t.setTermBuffer(termBuffer, 0, termLength);
  }
}
