package org.apache.lucene.search;
import java.io.IOException;
public abstract class FilteredDocIdSet extends DocIdSet {
  private final DocIdSet _innerSet;
  public FilteredDocIdSet(DocIdSet innerSet) {
    _innerSet = innerSet;
  }
  @Override
  public boolean isCacheable() {
    return _innerSet.isCacheable();
  }
  protected abstract boolean match(int docid) throws IOException;
  @Override
  public DocIdSetIterator iterator() throws IOException {
    return new FilteredDocIdSetIterator(_innerSet.iterator()) {
      @Override
      protected boolean match(int docid) throws IOException {
        return FilteredDocIdSet.this.match(docid);
      }
    };
  }
}
