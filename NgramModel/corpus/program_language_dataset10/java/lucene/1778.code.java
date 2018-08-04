package org.apache.lucene.store;
import java.io.IOException;
class RAMInputStream extends IndexInput implements Cloneable {
  static final int BUFFER_SIZE = RAMOutputStream.BUFFER_SIZE;
  private RAMFile file;
  private long length;
  private byte[] currentBuffer;
  private int currentBufferIndex;
  private int bufferPosition;
  private long bufferStart;
  private int bufferLength;
  RAMInputStream(RAMFile f) throws IOException {
    file = f;
    length = file.length;
    if (length/BUFFER_SIZE >= Integer.MAX_VALUE) {
      throw new IOException("Too large RAMFile! "+length); 
    }
    currentBufferIndex = -1;
    currentBuffer = null;
  }
  @Override
  public void close() {
  }
  @Override
  public long length() {
    return length;
  }
  @Override
  public byte readByte() throws IOException {
    if (bufferPosition >= bufferLength) {
      currentBufferIndex++;
      switchCurrentBuffer(true);
    }
    return currentBuffer[bufferPosition++];
  }
  @Override
  public void readBytes(byte[] b, int offset, int len) throws IOException {
    while (len > 0) {
      if (bufferPosition >= bufferLength) {
        currentBufferIndex++;
        switchCurrentBuffer(true);
      }
      int remainInBuffer = bufferLength - bufferPosition;
      int bytesToCopy = len < remainInBuffer ? len : remainInBuffer;
      System.arraycopy(currentBuffer, bufferPosition, b, offset, bytesToCopy);
      offset += bytesToCopy;
      len -= bytesToCopy;
      bufferPosition += bytesToCopy;
    }
  }
  private final void switchCurrentBuffer(boolean enforceEOF) throws IOException {
    if (currentBufferIndex >= file.numBuffers()) {
      if (enforceEOF)
        throw new IOException("Read past EOF");
      else {
        currentBufferIndex--;
        bufferPosition = BUFFER_SIZE;
      }
    } else {
      currentBuffer = file.getBuffer(currentBufferIndex);
      bufferPosition = 0;
      bufferStart = (long) BUFFER_SIZE * (long) currentBufferIndex;
      long buflen = length - bufferStart;
      bufferLength = buflen > BUFFER_SIZE ? BUFFER_SIZE : (int) buflen;
    }
  }
  @Override
  public long getFilePointer() {
    return currentBufferIndex < 0 ? 0 : bufferStart + bufferPosition;
  }
  @Override
  public void seek(long pos) throws IOException {
    if (currentBuffer==null || pos < bufferStart || pos >= bufferStart + BUFFER_SIZE) {
      currentBufferIndex = (int) (pos / BUFFER_SIZE);
      switchCurrentBuffer(false);
    }
    bufferPosition = (int) (pos % BUFFER_SIZE);
  }
}