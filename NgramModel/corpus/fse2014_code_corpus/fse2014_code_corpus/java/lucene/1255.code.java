package org.apache.lucene.search;
import java.io.IOException;
import junit.framework.Assert;
import org.apache.lucene.index.IndexReader;
public class RemoteCachingWrapperFilterHelper extends RemoteCachingWrapperFilter {
  private boolean shouldHaveCache;
  public RemoteCachingWrapperFilterHelper(Filter filter, boolean shouldHaveCache) {
    super(filter);
    this.shouldHaveCache = shouldHaveCache;
  }
  public void shouldHaveCache(boolean shouldHaveCache) {
    this.shouldHaveCache = shouldHaveCache;
  }
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    Filter cachedFilter = FilterManager.getInstance().getFilter(filter);
    Assert.assertNotNull("Filter should not be null", cachedFilter);
    if (!shouldHaveCache) {
      Assert.assertSame("First time filter should be the same ", filter, cachedFilter);
    } else {
      Assert.assertNotSame("We should have a cached version of the filter", filter, cachedFilter);
    }
    if (filter instanceof CachingWrapperFilterHelper) {
      ((CachingWrapperFilterHelper)cachedFilter).setShouldHaveCache(shouldHaveCache);
    }
    return cachedFilter.getDocIdSet(reader);
  }
}
