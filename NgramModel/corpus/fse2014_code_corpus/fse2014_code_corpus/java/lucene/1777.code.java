package org.apache.lucene.store;
import java.util.ArrayList;
import java.io.Serializable;
public class RAMFile implements Serializable {
  private static final long serialVersionUID = 1l;
  protected ArrayList<byte[]> buffers = new ArrayList<byte[]>();
  long length;
  RAMDirectory directory;
  protected long sizeInBytes;
  private long lastModified = System.currentTimeMillis();
  protected RAMFile() {}
  RAMFile(RAMDirectory directory) {
    this.directory = directory;
  }
  public synchronized long getLength() {
    return length;
  }
  protected synchronized void setLength(long length) {
    this.length = length;
  }
  public synchronized long getLastModified() {
    return lastModified;
  }
  protected synchronized void setLastModified(long lastModified) {
    this.lastModified = lastModified;
  }
  protected final byte[] addBuffer(int size) {
    byte[] buffer = newBuffer(size);
    synchronized(this) {
      buffers.add(buffer);
      sizeInBytes += size;
    }
    if (directory != null) {
      directory.sizeInBytes.getAndAdd(size);
    }
    return buffer;
  }
  protected final synchronized byte[] getBuffer(int index) {
    return buffers.get(index);
  }
  protected final synchronized int numBuffers() {
    return buffers.size();
  }
  protected byte[] newBuffer(int size) {
    return new byte[size];
  }
  public synchronized long getSizeInBytes() {
    return sizeInBytes;
  }
}
