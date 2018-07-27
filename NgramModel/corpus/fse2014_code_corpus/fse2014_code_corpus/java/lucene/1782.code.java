package org.apache.lucene.store;
import java.io.IOException;
import java.util.HashSet;
public class SingleInstanceLockFactory extends LockFactory {
  private HashSet<String> locks = new HashSet<String>();
  @Override
  public Lock makeLock(String lockName) {
    return new SingleInstanceLock(locks, lockName);
  }
  @Override
  public void clearLock(String lockName) throws IOException {
    synchronized(locks) {
      if (locks.contains(lockName)) {
        locks.remove(lockName);
      }
    }
  }
}
class SingleInstanceLock extends Lock {
  String lockName;
  private HashSet<String> locks;
  public SingleInstanceLock(HashSet<String> locks, String lockName) {
    this.locks = locks;
    this.lockName = lockName;
  }
  @Override
  public boolean obtain() throws IOException {
    synchronized(locks) {
      return locks.add(lockName);
    }
  }
  @Override
  public void release() {
    synchronized(locks) {
      locks.remove(lockName);
    }
  }
  @Override
  public boolean isLocked() {
    synchronized(locks) {
      return locks.contains(lockName);
    }
  }
  @Override
  public String toString() {
    return super.toString() + ": " + lockName;
  }
}
