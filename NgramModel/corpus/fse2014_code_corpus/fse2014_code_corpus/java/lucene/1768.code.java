package org.apache.lucene.store;
import java.io.IOException;
public class LockReleaseFailedException extends IOException {
  public LockReleaseFailedException(String message) {
    super(message);
  }
}
