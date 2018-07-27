package org.apache.lucene.util;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
public final class DummyConcurrentLock implements Lock {
  public static final DummyConcurrentLock INSTANCE = new DummyConcurrentLock();
  public void lock() {}
  public void lockInterruptibly() {}
  public boolean tryLock() {
    return true;
  }
  public boolean tryLock(long time, TimeUnit unit) {
    return true;
  }
  public void unlock() {}
  public Condition newCondition() {
    throw new UnsupportedOperationException();
  }
}
