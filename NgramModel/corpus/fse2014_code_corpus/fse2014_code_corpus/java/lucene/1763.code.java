package org.apache.lucene.store;
import java.io.IOException;
import java.io.Closeable;
import java.util.Map;
import java.util.HashMap;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.RamUsageEstimator;
public abstract class IndexInput implements Cloneable,Closeable {
  private byte[] bytes;                           
  private char[] chars;                           
  private boolean preUTF8Strings;                 
  public abstract byte readByte() throws IOException;
  public abstract void readBytes(byte[] b, int offset, int len)
    throws IOException;
  public void readBytes(byte[] b, int offset, int len, boolean useBuffer)
    throws IOException
  {
    readBytes(b, offset, len);
  }
  public int readInt() throws IOException {
    return ((readByte() & 0xFF) << 24) | ((readByte() & 0xFF) << 16)
         | ((readByte() & 0xFF) <<  8) |  (readByte() & 0xFF);
  }
  public int readVInt() throws IOException {
    byte b = readByte();
    int i = b & 0x7F;
    for (int shift = 7; (b & 0x80) != 0; shift += 7) {
      b = readByte();
      i |= (b & 0x7F) << shift;
    }
    return i;
  }
  public long readLong() throws IOException {
    return (((long)readInt()) << 32) | (readInt() & 0xFFFFFFFFL);
  }
  public long readVLong() throws IOException {
    byte b = readByte();
    long i = b & 0x7F;
    for (int shift = 7; (b & 0x80) != 0; shift += 7) {
      b = readByte();
      i |= (b & 0x7FL) << shift;
    }
    return i;
  }
  public void setModifiedUTF8StringsMode() {
    preUTF8Strings = true;
  }
  public String readString() throws IOException {
    if (preUTF8Strings)
      return readModifiedUTF8String();
    int length = readVInt();
    if (bytes == null || length > bytes.length) {
      bytes = new byte[ArrayUtil.oversize(length, 1)];
    }
    readBytes(bytes, 0, length);
    return new String(bytes, 0, length, "UTF-8");
  }
  private String readModifiedUTF8String() throws IOException {
    int length = readVInt();
    if (chars == null || length > chars.length) {
      chars = new char[ArrayUtil.oversize(length, RamUsageEstimator.NUM_BYTES_CHAR)];
    }
    readChars(chars, 0, length);
    return new String(chars, 0, length);
  }
  @Deprecated
  public void readChars(char[] buffer, int start, int length)
       throws IOException {
    final int end = start + length;
    for (int i = start; i < end; i++) {
      byte b = readByte();
      if ((b & 0x80) == 0)
	buffer[i] = (char)(b & 0x7F);
      else if ((b & 0xE0) != 0xE0) {
	buffer[i] = (char)(((b & 0x1F) << 6)
		 | (readByte() & 0x3F));
      } else {
	buffer[i] = (char)(((b & 0x0F) << 12)
		| ((readByte() & 0x3F) << 6)
	        |  (readByte() & 0x3F));
      }
    }
  }
  @Deprecated
  public void skipChars(int length) throws IOException{
    for (int i = 0; i < length; i++) {
      byte b = readByte();
      if ((b & 0x80) == 0){
      } else if ((b & 0xE0) != 0xE0) {
        readByte();
      } else {      
        readByte();
        readByte();
      }
    }
  }
  public abstract void close() throws IOException;
  public abstract long getFilePointer();
  public abstract void seek(long pos) throws IOException;
  public abstract long length();
  @Override
  public Object clone() {
    IndexInput clone = null;
    try {
      clone = (IndexInput)super.clone();
    } catch (CloneNotSupportedException e) {}
    clone.bytes = null;
    clone.chars = null;
    return clone;
  }
  public Map<String,String> readStringStringMap() throws IOException {
    final Map<String,String> map = new HashMap<String,String>();
    final int count = readInt();
    for(int i=0;i<count;i++) {
      final String key = readString();
      final String val = readString();
      map.put(key, val);
    }
    return map;
  }
}
