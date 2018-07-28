package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
public class RemoteCachingWrapperFilter extends Filter {
  protected Filter filter;
  public RemoteCachingWrapperFilter(Filter filter) {
    this.filter = filter;
  }
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    Filter cachedFilter = FilterManager.getInstance().getFilter(filter);
    return cachedFilter.getDocIdSet(reader);
  }
}
