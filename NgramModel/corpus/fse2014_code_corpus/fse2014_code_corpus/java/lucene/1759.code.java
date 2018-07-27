package org.apache.lucene.store;
import java.io.IOException;
import java.io.Closeable;
import org.apache.lucene.index.IndexFileNameFilter;
public abstract class Directory implements Closeable {
  volatile protected boolean isOpen = true;
  protected LockFactory lockFactory;
  public abstract String[] listAll() throws IOException;
  public abstract boolean fileExists(String name)
       throws IOException;
  public abstract long fileModified(String name)
       throws IOException;
  public abstract void touchFile(String name)
       throws IOException;
  public abstract void deleteFile(String name)
       throws IOException;
  public abstract long fileLength(String name)
       throws IOException;
  public abstract IndexOutput createOutput(String name)
       throws IOException;
  public void sync(String name) throws IOException {}
  public abstract IndexInput openInput(String name)
    throws IOException;
  public IndexInput openInput(String name, int bufferSize) throws IOException {
    return openInput(name);
  }
  public Lock makeLock(String name) {
      return lockFactory.makeLock(name);
  }
  public void clearLock(String name) throws IOException {
    if (lockFactory != null) {
      lockFactory.clearLock(name);
    }
  }
  public abstract void close()
       throws IOException;
  public void setLockFactory(LockFactory lockFactory) {
    assert lockFactory != null;
    this.lockFactory = lockFactory;
    lockFactory.setLockPrefix(this.getLockID());
  }
  public LockFactory getLockFactory() {
      return this.lockFactory;
  }
  public String getLockID() {
      return this.toString();
  }
  public static void copy(Directory src, Directory dest, boolean closeDirSrc) throws IOException {
    final String[] files = src.listAll();
    IndexFileNameFilter filter = IndexFileNameFilter.getFilter();
    byte[] buf = new byte[BufferedIndexOutput.BUFFER_SIZE];
    for (int i = 0; i < files.length; i++) {
      if (!filter.accept(null, files[i]))
        continue;
      IndexOutput os = null;
      IndexInput is = null;
      try {
        os = dest.createOutput(files[i]);
        is = src.openInput(files[i]);
        long len = is.length();
        long readCount = 0;
        while (readCount < len) {
          int toRead = readCount + BufferedIndexOutput.BUFFER_SIZE > len ? (int)(len - readCount) : BufferedIndexOutput.BUFFER_SIZE;
          is.readBytes(buf, 0, toRead);
          os.writeBytes(buf, toRead);
          readCount += toRead;
        }
      } finally {
        try {
          if (os != null)
            os.close();
        } finally {
          if (is != null)
            is.close();
        }
      }
    }
    if(closeDirSrc)
      src.close();
  }
  protected final void ensureOpen() throws AlreadyClosedException {
    if (!isOpen)
      throw new AlreadyClosedException("this Directory is closed");
  }
}
