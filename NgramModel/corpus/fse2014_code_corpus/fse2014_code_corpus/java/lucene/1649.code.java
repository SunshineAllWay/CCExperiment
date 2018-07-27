package org.apache.lucene.search;
import java.io.IOException;
public abstract class DocIdSet {
  public static final DocIdSet EMPTY_DOCIDSET = new DocIdSet() {
    private final DocIdSetIterator iterator = new DocIdSetIterator() {
      @Override
      public int advance(int target) throws IOException { return NO_MORE_DOCS; }
      @Override
      public int docID() { return NO_MORE_DOCS; }
      @Override
      public int nextDoc() throws IOException { return NO_MORE_DOCS; }
    };
    @Override
    public DocIdSetIterator iterator() {
      return iterator;
    }
    @Override
    public boolean isCacheable() {
      return true;
    }
  };
  public abstract DocIdSetIterator iterator() throws IOException;
  public boolean isCacheable() {
    return false;
  }
}
