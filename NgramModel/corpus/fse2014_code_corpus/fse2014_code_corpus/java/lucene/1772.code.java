package org.apache.lucene.store;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
public class NativeFSLockFactory extends FSLockFactory {
  private volatile boolean tested = false;
  private synchronized void acquireTestLock() {
    if (tested) return;
    tested = true;
    if (!lockDir.exists()) {
      if (!lockDir.mkdirs())
        throw new RuntimeException("Cannot create directory: " +
                              lockDir.getAbsolutePath());
    } else if (!lockDir.isDirectory()) {
      throw new RuntimeException("Found regular file where directory expected: " + 
                            lockDir.getAbsolutePath());
    }
    String randomLockName = "lucene-" + Long.toString(new Random().nextInt(), Character.MAX_RADIX) + "-test.lock";
    Lock l = makeLock(randomLockName);
    try {
      l.obtain();
      l.release();
    } catch (IOException e) {
      RuntimeException e2 = new RuntimeException("Failed to acquire random test lock; please verify filesystem for lock directory '" + lockDir + "' supports locking");
      e2.initCause(e);
      throw e2;
    }    
  }
  public NativeFSLockFactory() throws IOException {
    this((File) null);
  }
  public NativeFSLockFactory(String lockDirName) throws IOException {
    this(new File(lockDirName));
  }
  public NativeFSLockFactory(File lockDir) throws IOException {
    setLockDir(lockDir);
  }
  @Override
  public synchronized Lock makeLock(String lockName) {
    acquireTestLock();
    if (lockPrefix != null)
      lockName = lockPrefix + "-" + lockName;
    return new NativeFSLock(lockDir, lockName);
  }
  @Override
  public void clearLock(String lockName) throws IOException {
    if (lockDir.exists()) {
      if (lockPrefix != null) {
        lockName = lockPrefix + "-" + lockName;
      }
      File lockFile = new File(lockDir, lockName);
      if (lockFile.exists() && !lockFile.delete()) {
        throw new IOException("Cannot delete " + lockFile);
      }
    }
  }
}
class NativeFSLock extends Lock {
  private RandomAccessFile f;
  private FileChannel channel;
  private FileLock lock;
  private File path;
  private File lockDir;
  private static HashSet<String> LOCK_HELD = new HashSet<String>();
  public NativeFSLock(File lockDir, String lockFileName) {
    this.lockDir = lockDir;
    path = new File(lockDir, lockFileName);
  }
  private synchronized boolean lockExists() {
    return lock != null;
  }
  @Override
  public synchronized boolean obtain() throws IOException {
    if (lockExists()) {
      return false;
    }
    if (!lockDir.exists()) {
      if (!lockDir.mkdirs())
        throw new IOException("Cannot create directory: " +
                              lockDir.getAbsolutePath());
    } else if (!lockDir.isDirectory()) {
      throw new IOException("Found regular file where directory expected: " + 
                            lockDir.getAbsolutePath());
    }
    String canonicalPath = path.getCanonicalPath();
    boolean markedHeld = false;
    try {
      synchronized(LOCK_HELD) {
        if (LOCK_HELD.contains(canonicalPath)) {
          return false;
        } else {
          LOCK_HELD.add(canonicalPath);
          markedHeld = true;
        }
      }
      try {
        f = new RandomAccessFile(path, "rw");
      } catch (IOException e) {
        failureReason = e;
        f = null;
      }
      if (f != null) {
        try {
          channel = f.getChannel();
          try {
            lock = channel.tryLock();
          } catch (IOException e) {
            failureReason = e;
          } finally {
            if (lock == null) {
              try {
                channel.close();
              } finally {
                channel = null;
              }
            }
          }
        } finally {
          if (channel == null) {
            try {
              f.close();
            } finally {
              f = null;
            }
          }
        }
      }
    } finally {
      if (markedHeld && !lockExists()) {
        synchronized(LOCK_HELD) {
          if (LOCK_HELD.contains(canonicalPath)) {
            LOCK_HELD.remove(canonicalPath);
          }
        }
      }
    }
    return lockExists();
  }
  @Override
  public synchronized void release() throws IOException {
    if (lockExists()) {
      try {
        lock.release();
      } finally {
        lock = null;
        try {
          channel.close();
        } finally {
          channel = null;
          try {
            f.close();
          } finally {
            f = null;
            synchronized(LOCK_HELD) {
              LOCK_HELD.remove(path.getCanonicalPath());
            }
          }
        }
      }
      if (!path.delete())
        throw new LockReleaseFailedException("failed to delete " + path);
    } else {
      boolean obtained = false;
      try {
        if (!(obtained = obtain())) {
          throw new LockReleaseFailedException(
              "Cannot forcefully unlock a NativeFSLock which is held by another indexer component: "
                  + path);
        }
      } finally {
        if (obtained) {
          release();
        }
      }
    }
  }
  @Override
  public synchronized boolean isLocked() {
    if (lockExists()) return true;
    if (!path.exists()) return false;
    try {
      boolean obtained = obtain();
      if (obtained) release();
      return !obtained;
    } catch (IOException ioe) {
      return false;
    }    
  }
  @Override
  public String toString() {
    return "NativeFSLock@" + path;
  }
}
