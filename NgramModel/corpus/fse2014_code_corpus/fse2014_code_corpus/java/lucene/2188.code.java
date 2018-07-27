package org.apache.solr.common.util;
import java.io.Writer;
import java.io.IOException;
public class FastWriter extends Writer {
  private static final int BUFSIZE = 8192;
  private final Writer sink;
  private final char[] buf;
  private int pos;
  public FastWriter(Writer w) {
    this(w, new char[BUFSIZE], 0);
  }
  public FastWriter(Writer sink, char[] tempBuffer, int start) {
    this.sink = sink;
    this.buf = tempBuffer;
    this.pos = start;
  }
  public static FastWriter wrap(Writer sink) {
    return (sink instanceof FastWriter) ? (FastWriter)sink : new FastWriter(sink);
  }
  @Override
  public void write(int c) throws IOException {
    write((char)c); 
  }
  public void write(char c) throws IOException {
    if (pos >= buf.length) {
      sink.write(buf,0,pos);
      pos=0;
    }
    buf[pos++] = (char)c;
  }
  @Override
  public FastWriter append(char c) throws IOException {
    if (pos >= buf.length) {
      sink.write(buf,0,pos);
      pos=0;
    }
    buf[pos++] = (char)c;
    return this;
  }
  @Override
  public void write(char cbuf[], int off, int len) throws IOException {
    int space = buf.length - pos;
    if (len < space) {
      System.arraycopy(cbuf, off, buf, pos, len);
      pos += len;
    } else if (len<BUFSIZE) {
      System.arraycopy(cbuf, off, buf, pos, space);
      sink.write(buf, 0, buf.length);
      pos = len-space;
      System.arraycopy(cbuf, off+space, buf, 0, pos);
    } else {
      sink.write(buf,0,pos);  
      pos=0;
      sink.write(cbuf, off, len);
    }
  }
  @Override
  public void write(String str, int off, int len) throws IOException {
    int space = buf.length - pos;
    if (len < space) {
      str.getChars(off, off+len, buf, pos);
      pos += len;
    } else if (len<BUFSIZE) {
      str.getChars(off, off+space, buf, pos);
      sink.write(buf, 0, buf.length);
      str.getChars(off+space, off+len, buf, 0);
      pos = len-space;
    } else {
      sink.write(buf,0,pos);  
      pos=0;
      sink.write(str, off, len);
    }
  }
  @Override
  public void flush() throws IOException {
    sink.write(buf,0,pos);
    pos=0;
    sink.flush();
  }
  @Override
  public void close() throws IOException {
    flush();
    sink.close();
  }
  public void flushBuffer() throws IOException {
    sink.write(buf, 0, pos);
    pos=0;
  }
}
