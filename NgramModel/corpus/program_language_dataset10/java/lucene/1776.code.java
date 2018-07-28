package org.apache.lucene.store;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.lucene.util.ThreadInterruptedException;
public class RAMDirectory extends Directory implements Serializable {
  private static final long serialVersionUID = 1l;
  HashMap<String,RAMFile> fileMap = new HashMap<String,RAMFile>();
  final AtomicLong sizeInBytes = new AtomicLong();
  public RAMDirectory() {
    setLockFactory(new SingleInstanceLockFactory());
  }
  public RAMDirectory(Directory dir) throws IOException {
    this(dir, false);
  }
  private RAMDirectory(Directory dir, boolean closeDir) throws IOException {
    this();
    Directory.copy(dir, this, closeDir);
  }
  @Override
  public synchronized final String[] listAll() {
    ensureOpen();
    Set<String> fileNames = fileMap.keySet();
    String[] result = new String[fileNames.size()];
    int i = 0;
    for(final String fileName: fileNames) 
      result[i++] = fileName;
    return result;
  }
  @Override
  public final boolean fileExists(String name) {
    ensureOpen();
    RAMFile file;
    synchronized (this) {
      file = fileMap.get(name);
    }
    return file != null;
  }
  @Override
  public final long fileModified(String name) throws IOException {
    ensureOpen();
    RAMFile file;
    synchronized (this) {
      file = fileMap.get(name);
    }
    if (file==null)
      throw new FileNotFoundException(name);
    return file.getLastModified();
  }
  @Override
  public void touchFile(String name) throws IOException {
    ensureOpen();
    RAMFile file;
    synchronized (this) {
      file = fileMap.get(name);
    }
    if (file==null)
      throw new FileNotFoundException(name);
    long ts2, ts1 = System.currentTimeMillis();
    do {
      try {
        Thread.sleep(0, 1);
      } catch (InterruptedException ie) {
        throw new ThreadInterruptedException(ie);
      }
      ts2 = System.currentTimeMillis();
    } while(ts1 == ts2);
    file.setLastModified(ts2);
  }
  @Override
  public final long fileLength(String name) throws IOException {
    ensureOpen();
    RAMFile file;
    synchronized (this) {
      file = fileMap.get(name);
    }
    if (file==null)
      throw new FileNotFoundException(name);
    return file.getLength();
  }
  public synchronized final long sizeInBytes() {
    ensureOpen();
    return sizeInBytes.get();
  }
  @Override
  public synchronized void deleteFile(String name) throws IOException {
    ensureOpen();
    RAMFile file = fileMap.get(name);
    if (file!=null) {
        fileMap.remove(name);
        file.directory = null;
        sizeInBytes.addAndGet(-file.sizeInBytes);
    } else
      throw new FileNotFoundException(name);
  }
  @Override
  public IndexOutput createOutput(String name) throws IOException {
    ensureOpen();
    RAMFile file = new RAMFile(this);
    synchronized (this) {
      RAMFile existing = fileMap.get(name);
      if (existing!=null) {
        sizeInBytes.addAndGet(-existing.sizeInBytes);
        existing.directory = null;
      }
      fileMap.put(name, file);
    }
    return new RAMOutputStream(file);
  }
  @Override
  public IndexInput openInput(String name) throws IOException {
    ensureOpen();
    RAMFile file;
    synchronized (this) {
      file = fileMap.get(name);
    }
    if (file == null)
      throw new FileNotFoundException(name);
    return new RAMInputStream(file);
  }
  @Override
  public void close() {
    isOpen = false;
    fileMap = null;
  }
}
