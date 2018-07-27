package org.apache.lucene.benchmark.byTask.utils;
import java.io.IOException;
import java.io.Reader;
public class StringBufferReader extends Reader {
  private StringBuffer sb;
  private int length;
  private int next = 0;
  private int mark = 0;
  public StringBufferReader(StringBuffer sb) {
    set(sb);
  }
  private void ensureOpen() throws IOException {
    if (sb == null) {
      throw new IOException("Stream has already been closed");
    }
  }
  @Override
  public void close() {
    synchronized (lock) {
      sb = null;
    }
  }
  @Override
  public void mark(int readAheadLimit) throws IOException {
    if (readAheadLimit < 0){
      throw new IllegalArgumentException("Read-ahead limit cannpt be negative: " + readAheadLimit);
    }
    synchronized (lock) {
      ensureOpen();
      mark = next;
    }
  }
  @Override
  public boolean markSupported() {
    return true;
  }
  @Override
  public int read() throws IOException {
    synchronized (lock) {
      ensureOpen();
      return next >= length ? -1 : sb.charAt(next++);
    }
  }
  @Override
  public int read(char cbuf[], int off, int len) throws IOException {
    synchronized (lock) {
      ensureOpen();
      if (off < 0 || off > cbuf.length || len < 0 || off + len > cbuf.length) {
        throw new IndexOutOfBoundsException("off=" + off + " len=" + len + " cbuf.length=" + cbuf.length);
      }
      if (len == 0) {
        return 0;
      }
      if (next >= length) {
        return -1;
      }
      int n = Math.min(length - next, len);
      sb.getChars(next, next + n, cbuf, off);
      next += n;
      return n;
    }
  }
  @Override
  public boolean ready() throws IOException {
    synchronized (lock) {
      ensureOpen();
      return true;
    }
  }
  @Override
  public void reset() throws IOException {
    synchronized (lock) {
      ensureOpen();
      next = mark;
      length = sb.length();
    }
  }
  public void set(StringBuffer sb) {
    synchronized (lock) {
      this.sb = sb;
      length = sb.length();
    }
  }
  @Override
  public long skip(long ns) throws IOException {
    synchronized (lock) {
      ensureOpen();
      if (next >= length) {
        return 0;
      }
      long n = Math.min(length - next, ns);
      n = Math.max(-next, n);
      next += n;
      return n;
    }
  }
}
