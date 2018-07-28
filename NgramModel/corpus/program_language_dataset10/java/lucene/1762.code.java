package org.apache.lucene.store;
import java.io.File;
public abstract class FSLockFactory extends LockFactory {
  protected File lockDir = null;
  protected final void setLockDir(File lockDir) {
    if (this.lockDir != null)
      throw new IllegalStateException("You can set the lock directory for this factory only once.");
    this.lockDir = lockDir;
  }
  public File getLockDir() {
    return lockDir;
  }
}
