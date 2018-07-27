package org.apache.lucene.search;
import java.io.IOException;
import java.io.Serializable;
public abstract class FieldComparatorSource implements Serializable {
  public abstract FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
      throws IOException;
}
