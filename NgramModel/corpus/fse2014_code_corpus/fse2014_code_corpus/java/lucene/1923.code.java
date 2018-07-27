package org.apache.lucene.search;
import java.io.IOException;
import java.util.WeakHashMap;
import junit.framework.Assert;
import org.apache.lucene.index.IndexReader;
public class CachingWrapperFilterHelper extends CachingWrapperFilter {
  private boolean shouldHaveCache = false;
  public CachingWrapperFilterHelper(Filter filter) {
    super(filter);
  }
  public void setShouldHaveCache(boolean shouldHaveCache) {
    this.shouldHaveCache = shouldHaveCache;
  }
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    if (cache == null) {
      cache = new WeakHashMap<IndexReader,DocIdSet>();
    }
    synchronized (cache) {  
      DocIdSet cached = cache.get(reader);
      if (shouldHaveCache) {
        Assert.assertNotNull("Cache should have data ", cached);
      } else {
        Assert.assertNull("Cache should be null " + cached , cached);
      }
      if (cached != null) {
        return cached;
      }
    }
    final DocIdSet bits = filter.getDocIdSet(reader);
    synchronized (cache) {  
      cache.put(reader, bits);
    }
    return bits;
  }
  @Override
  public String toString() {
    return "CachingWrapperFilterHelper("+filter+")";
  }
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CachingWrapperFilterHelper)) return false;
    return this.filter.equals(o);
  }
  @Override
  public int hashCode() {
    return this.filter.hashCode() ^ 0x5525aacb;
  }
}
