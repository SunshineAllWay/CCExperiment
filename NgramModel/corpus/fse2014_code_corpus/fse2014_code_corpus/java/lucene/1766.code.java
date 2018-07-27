package org.apache.lucene.store;
import java.io.IOException;
public abstract class LockFactory {
  protected String lockPrefix = null;
  public void setLockPrefix(String lockPrefix) {
    this.lockPrefix = lockPrefix;
  }
  public String getLockPrefix() {
    return this.lockPrefix;
  }
  public abstract Lock makeLock(String lockName);
  abstract public void clearLock(String lockName) throws IOException;
}
