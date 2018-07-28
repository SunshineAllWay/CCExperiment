package org.apache.lucene.store;
import java.io.FileNotFoundException;
public class NoSuchDirectoryException extends FileNotFoundException {
  public NoSuchDirectoryException(String message) {
    super(message);
  }
}
