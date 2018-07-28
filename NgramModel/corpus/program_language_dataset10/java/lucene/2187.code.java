package org.apache.solr.common.util;
import java.io.*;
public class FastOutputStream extends OutputStream implements DataOutput {
  private final OutputStream out;
  private final byte[] buf;
  private long written;  
  private int pos;
  public FastOutputStream(OutputStream w) {
    this(w, new byte[8192], 0);
  }
  public FastOutputStream(OutputStream sink, byte[] tempBuffer, int start) {
    this.out = sink;
    this.buf = tempBuffer;
    this.pos = start;
  }
  public static FastOutputStream wrap(OutputStream sink) {
   return (sink instanceof FastOutputStream) ? (FastOutputStream)sink : new FastOutputStream(sink);
  }
  @Override
  public void write(int b) throws IOException {
    write((byte)b);
  }
  public void write(byte b[]) throws IOException {
    write(b,0,b.length);
  }
  public void write(byte b) throws IOException {
    if (pos >= buf.length) {
      out.write(buf);
      written += pos;
      pos=0;
    }
    buf[pos++] = b;
  }
  @Override
  public void write(byte arr[], int off, int len) throws IOException {
    int space = buf.length - pos;
    if (len < space) {
      System.arraycopy(arr, off, buf, pos, len);
      pos += len;
    } else if (len<buf.length) {
      System.arraycopy(arr, off, buf, pos, space);
      out.write(buf);
      written += buf.length;
      pos = len-space;
      System.arraycopy(arr, off+space, buf, 0, pos);
    } else {
      if (pos>0) {
        out.write(buf,0,pos);  
        written += pos;
        pos=0;
      }
      out.write(arr, off, len);
      written += len;            
    }
  }
  public void reserve(int len) throws IOException {
    if (len > (buf.length - pos))
      flushBuffer();
  }
  public void writeBoolean(boolean v) throws IOException {
    write(v ? 1:0);
  }
  public void writeByte(int v) throws IOException {
    write((byte)v);
  }
  public void writeShort(int v) throws IOException {
    write((byte)(v >>> 8));
    write((byte)v);
  }
  public void writeChar(int v) throws IOException {
    writeShort(v);
  }
  public void writeInt(int v) throws IOException {
    reserve(4);
    buf[pos] = (byte)(v>>>24);
    buf[pos+1] = (byte)(v>>>16);
    buf[pos+2] = (byte)(v>>>8);
    buf[pos+3] = (byte)(v);
    pos+=4;
  }
  public void writeLong(long v) throws IOException {
    reserve(8);
    buf[pos] = (byte)(v>>>56);
    buf[pos+1] = (byte)(v>>>48);
    buf[pos+2] = (byte)(v>>>40);
    buf[pos+3] = (byte)(v>>>32);
    buf[pos+4] = (byte)(v>>>24);
    buf[pos+5] = (byte)(v>>>16);
    buf[pos+6] = (byte)(v>>>8);
    buf[pos+7] = (byte)(v);
    pos+=8;
  }
  public void writeFloat(float v) throws IOException {
    writeInt(Float.floatToRawIntBits(v));
  }
  public void writeDouble(double v) throws IOException {
    writeLong(Double.doubleToRawLongBits(v));
  }
  public void writeBytes(String s) throws IOException {
    for (int i=0; i<s.length(); i++)
      write((byte)s.charAt(i));
  }
  public void writeChars(String s) throws IOException {
    for (int i=0; i<s.length(); i++)
      writeChar(s.charAt(i)); 
  }
  public void writeUTF(String s) throws IOException {
    DataOutputStream daos = new DataOutputStream(this);
    daos.writeUTF(s);
  }
  @Override
  public void flush() throws IOException {
    flushBuffer();
    out.flush();
  }
  @Override
  public void close() throws IOException {
    flushBuffer();
    out.close();
  }
  public void flushBuffer() throws IOException {
    out.write(buf, 0, pos);
    written += pos;
    pos=0;
  }
  public long size() {
    return written + pos;
  }
}
