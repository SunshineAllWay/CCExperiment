package org.apache.lucene.index;
import org.apache.lucene.store.BufferedIndexInput;
public class MockIndexInput extends BufferedIndexInput {
    private byte[] buffer;
    private int pointer = 0;
    private long length;
    public MockIndexInput(byte[] bytes) {
        buffer = bytes;
        length = bytes.length;
    }
    @Override
    protected void readInternal(byte[] dest, int destOffset, int len) {
        int remainder = len;
        int start = pointer;
        while (remainder != 0) {
          int bufferOffset = start % buffer.length;
          int bytesInBuffer = buffer.length - bufferOffset;
          int bytesToCopy = bytesInBuffer >= remainder ? remainder : bytesInBuffer;
          System.arraycopy(buffer, bufferOffset, dest, destOffset, bytesToCopy);
          destOffset += bytesToCopy;
          start += bytesToCopy;
          remainder -= bytesToCopy;
        }
        pointer += len;
    }
    @Override
    public void close() {
    }
    @Override
    protected void seekInternal(long pos) {
        pointer = (int) pos;
    }
    @Override
    public long length() {
      return length;
    }
}
