package org.apache.lucene.search;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.OpenBitSetDISI;
public class CachingWrapperFilter extends Filter {
  Filter filter;
  transient Map<IndexReader,DocIdSet> cache;
  private final ReentrantLock lock = new ReentrantLock();
  public CachingWrapperFilter(Filter filter) {
    this.filter = filter;
  }
  protected DocIdSet docIdSetToCache(DocIdSet docIdSet, IndexReader reader) throws IOException {
    if (docIdSet.isCacheable()) {
      return docIdSet;
    } else {
      final DocIdSetIterator it = docIdSet.iterator();
      return (it == null) ? DocIdSet.EMPTY_DOCIDSET : new OpenBitSetDISI(it, reader.maxDoc());
    }
  }
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    lock.lock();
    try {
      if (cache == null) {
        cache = new WeakHashMap<IndexReader,DocIdSet>();
      }
      final DocIdSet cached = cache.get(reader);
      if (cached != null) return cached;
    } finally {
      lock.unlock();
    }
    final DocIdSet docIdSet = docIdSetToCache(filter.getDocIdSet(reader), reader);
    if (docIdSet != null) {
      lock.lock();
      try {
        cache.put(reader, docIdSet);
      } finally {
        lock.unlock();
      }
    }
    return docIdSet;
  }
  @Override
  public String toString() {
    return "CachingWrapperFilter("+filter+")";
  }
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CachingWrapperFilter)) return false;
    return this.filter.equals(((CachingWrapperFilter)o).filter);
  }
  @Override
  public int hashCode() {
    return filter.hashCode() ^ 0x1117BF25;  
  }
}
