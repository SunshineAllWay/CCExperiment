package org.apache.lucene.index;
import java.io.IOException;
public class CorruptIndexException extends IOException {
  public CorruptIndexException(String message) {
    super(message);
  }
}
