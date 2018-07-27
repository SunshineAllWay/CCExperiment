package org.apache.lucene.store;
import java.io.IOException;
import java.io.Closeable;
import java.util.Map;
import org.apache.lucene.util.UnicodeUtil;
public abstract class IndexOutput implements Closeable {
  private UnicodeUtil.UTF8Result utf8Result = new UnicodeUtil.UTF8Result();
  public abstract void writeByte(byte b) throws IOException;
  public void writeBytes(byte[] b, int length) throws IOException {
    writeBytes(b, 0, length);
  }
  public abstract void writeBytes(byte[] b, int offset, int length) throws IOException;
  public void writeInt(int i) throws IOException {
    writeByte((byte)(i >> 24));
    writeByte((byte)(i >> 16));
    writeByte((byte)(i >>  8));
    writeByte((byte) i);
  }
  public void writeVInt(int i) throws IOException {
    while ((i & ~0x7F) != 0) {
      writeByte((byte)((i & 0x7f) | 0x80));
      i >>>= 7;
    }
    writeByte((byte)i);
  }
  public void writeLong(long i) throws IOException {
    writeInt((int) (i >> 32));
    writeInt((int) i);
  }
  public void writeVLong(long i) throws IOException {
    while ((i & ~0x7F) != 0) {
      writeByte((byte)((i & 0x7f) | 0x80));
      i >>>= 7;
    }
    writeByte((byte)i);
  }
  public void writeString(String s) throws IOException {
    UnicodeUtil.UTF16toUTF8(s, 0, s.length(), utf8Result);
    writeVInt(utf8Result.length);
    writeBytes(utf8Result.result, 0, utf8Result.length);
  }
  @Deprecated
  public void writeChars(String s, int start, int length)
       throws IOException {
    final int end = start + length;
    for (int i = start; i < end; i++) {
      final int code = s.charAt(i);
      if (code >= 0x01 && code <= 0x7F)
	writeByte((byte)code);
      else if (((code >= 0x80) && (code <= 0x7FF)) || code == 0) {
	writeByte((byte)(0xC0 | (code >> 6)));
	writeByte((byte)(0x80 | (code & 0x3F)));
      } else {
	writeByte((byte)(0xE0 | (code >>> 12)));
	writeByte((byte)(0x80 | ((code >> 6) & 0x3F)));
	writeByte((byte)(0x80 | (code & 0x3F)));
      }
    }
  }
  @Deprecated
  public void writeChars(char[] s, int start, int length)
    throws IOException {
    final int end = start + length;
    for (int i = start; i < end; i++) {
      final int code = s[i];
      if (code >= 0x01 && code <= 0x7F)
	writeByte((byte)code);
      else if (((code >= 0x80) && (code <= 0x7FF)) || code == 0) {
	writeByte((byte)(0xC0 | (code >> 6)));
	writeByte((byte)(0x80 | (code & 0x3F)));
      } else {
	writeByte((byte)(0xE0 | (code >>> 12)));
	writeByte((byte)(0x80 | ((code >> 6) & 0x3F)));
	writeByte((byte)(0x80 | (code & 0x3F)));
      }
    }
  }
  private static int COPY_BUFFER_SIZE = 16384;
  private byte[] copyBuffer;
  public void copyBytes(IndexInput input, long numBytes) throws IOException {
    assert numBytes >= 0: "numBytes=" + numBytes;
    long left = numBytes;
    if (copyBuffer == null)
      copyBuffer = new byte[COPY_BUFFER_SIZE];
    while(left > 0) {
      final int toCopy;
      if (left > COPY_BUFFER_SIZE)
        toCopy = COPY_BUFFER_SIZE;
      else
        toCopy = (int) left;
      input.readBytes(copyBuffer, 0, toCopy);
      writeBytes(copyBuffer, 0, toCopy);
      left -= toCopy;
    }
  }
  public abstract void flush() throws IOException;
  public abstract void close() throws IOException;
  public abstract long getFilePointer();
  public abstract void seek(long pos) throws IOException;
  public abstract long length() throws IOException;
  public void setLength(long length) throws IOException {}
  public void writeStringStringMap(Map<String,String> map) throws IOException {
    if (map == null) {
      writeInt(0);
    } else {
      writeInt(map.size());
      for(final Map.Entry<String, String> entry: map.entrySet()) {
        writeString(entry.getKey());
        writeString(entry.getValue());
      }
    }
  }
}
