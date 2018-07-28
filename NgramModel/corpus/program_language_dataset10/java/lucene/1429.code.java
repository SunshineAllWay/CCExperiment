package org.apache.lucene.analysis;
import java.io.Reader;
public abstract class CharStream extends Reader {
  public abstract int correctOffset(int currentOff);
}
