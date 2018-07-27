package org.apache.lucene.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.function.DocValues;
import java.io.IOException;
import java.io.Serializable;
public abstract class ValueSource implements Serializable {
  public abstract DocValues getValues(IndexReader reader) throws IOException;
  public abstract String description();
  @Override
  public String toString() {
    return description();
  }
  @Override
  public abstract boolean equals(Object o);
  @Override
  public abstract int hashCode();
}
