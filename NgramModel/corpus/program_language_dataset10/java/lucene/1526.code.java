package org.apache.lucene.index;
public class FieldReaderException extends RuntimeException{
  public FieldReaderException() {
  }
  public FieldReaderException(Throwable cause) {
    super(cause);
  }
  public FieldReaderException(String message) {
    super(message);
  }
  public FieldReaderException(String message, Throwable cause) {
    super(message, cause);
  }
}