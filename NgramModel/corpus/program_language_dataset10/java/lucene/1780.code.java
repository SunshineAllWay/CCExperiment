package org.apache.lucene.store;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
public class SimpleFSDirectory extends FSDirectory {
  public SimpleFSDirectory(File path, LockFactory lockFactory) throws IOException {
    super(path, lockFactory);
  }
  public SimpleFSDirectory(File path) throws IOException {
    super(path, null);
  }
  @Override
  public IndexOutput createOutput(String name) throws IOException {
    initOutput(name);
    return new SimpleFSIndexOutput(new File(directory, name));
  }
  @Override
  public IndexInput openInput(String name, int bufferSize) throws IOException {
    ensureOpen();
    return new SimpleFSIndexInput(new File(directory, name), bufferSize, getReadChunkSize());
  }
  protected static class SimpleFSIndexInput extends BufferedIndexInput {
    protected static class Descriptor extends RandomAccessFile {
      protected volatile boolean isOpen;
      long position;
      final long length;
      public Descriptor(File file, String mode) throws IOException {
        super(file, mode);
        isOpen=true;
        length=length();
      }
      @Override
      public void close() throws IOException {
        if (isOpen) {
          isOpen=false;
          super.close();
        }
      }
    }
    protected final Descriptor file;
    boolean isClone;
    protected final int chunkSize;
    public SimpleFSIndexInput(File path, int bufferSize, int chunkSize) throws IOException {
      super(bufferSize);
      file = new Descriptor(path, "r");
      this.chunkSize = chunkSize;
    }
    @Override
    protected void readInternal(byte[] b, int offset, int len)
         throws IOException {
      synchronized (file) {
        long position = getFilePointer();
        if (position != file.position) {
          file.seek(position);
          file.position = position;
        }
        int total = 0;
        try {
          do {
            final int readLength;
            if (total + chunkSize > len) {
              readLength = len - total;
            } else {
              readLength = chunkSize;
            }
            final int i = file.read(b, offset + total, readLength);
            if (i == -1) {
              throw new IOException("read past EOF");
            }
            file.position += i;
            total += i;
          } while (total < len);
        } catch (OutOfMemoryError e) {
          final OutOfMemoryError outOfMemoryError = new OutOfMemoryError(
              "OutOfMemoryError likely caused by the Sun VM Bug described in "
              + "https://issues.apache.org/jira/browse/LUCENE-1566; try calling FSDirectory.setReadChunkSize "
              + "with a a value smaller than the current chunks size (" + chunkSize + ")");
          outOfMemoryError.initCause(e);
          throw outOfMemoryError;
        }
      }
    }
    @Override
    public void close() throws IOException {
      if (!isClone) file.close();
    }
    @Override
    protected void seekInternal(long position) {
    }
    @Override
    public long length() {
      return file.length;
    }
    @Override
    public Object clone() {
      SimpleFSIndexInput clone = (SimpleFSIndexInput)super.clone();
      clone.isClone = true;
      return clone;
    }
    boolean isFDValid() throws IOException {
      return file.getFD().valid();
    }
  }
  protected static class SimpleFSIndexOutput extends BufferedIndexOutput {
    RandomAccessFile file = null;
    private volatile boolean isOpen;
    public SimpleFSIndexOutput(File path) throws IOException {
      file = new RandomAccessFile(path, "rw");
      isOpen = true;
    }
    @Override
    public void flushBuffer(byte[] b, int offset, int size) throws IOException {
      file.write(b, offset, size);
    }
    @Override
    public void close() throws IOException {
      if (isOpen) {
        boolean success = false;
        try {
          super.close();
          success = true;
        } finally {
          isOpen = false;
          if (!success) {
            try {
              file.close();
            } catch (Throwable t) {
            }
          } else
            file.close();
        }
      }
    }
    @Override
    public void seek(long pos) throws IOException {
      super.seek(pos);
      file.seek(pos);
    }
    @Override
    public long length() throws IOException {
      return file.length();
    }
    @Override
    public void setLength(long length) throws IOException {
      file.setLength(length);
    }
  }
}
