package org.apache.lucene.search;
import java.io.IOException;
public abstract class DocIdSetIterator {
  public static final int NO_MORE_DOCS = Integer.MAX_VALUE;
  public abstract int docID();
  public abstract int nextDoc() throws IOException;
  public abstract int advance(int target) throws IOException;
}
