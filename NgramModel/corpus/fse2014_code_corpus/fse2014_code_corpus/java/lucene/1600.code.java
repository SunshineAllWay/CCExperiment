package org.apache.lucene.index;
import java.io.IOException;
import java.io.Closeable;
public abstract class TermEnum implements Closeable {
  public abstract boolean next() throws IOException;
  public abstract Term term();
  public abstract int docFreq();
  public abstract void close() throws IOException;
}
