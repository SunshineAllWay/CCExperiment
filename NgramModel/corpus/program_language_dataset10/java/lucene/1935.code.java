package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;
public class TestCachingWrapperFilter extends LuceneTestCase {
  public void testCachingWorks() throws Exception {
    Directory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
    writer.close();
    IndexReader reader = IndexReader.open(dir, true);
    MockFilter filter = new MockFilter();
    CachingWrapperFilter cacher = new CachingWrapperFilter(filter);
    cacher.getDocIdSet(reader);
    assertTrue("first time", filter.wasCalled());
    cacher.getDocIdSet(reader);
    filter.clear();
    cacher.getDocIdSet(reader);
    assertFalse("second time", filter.wasCalled());
    reader.close();
  }
  private static void assertDocIdSetCacheable(IndexReader reader, Filter filter, boolean shouldCacheable) throws IOException {
    final CachingWrapperFilter cacher = new CachingWrapperFilter(filter);
    final DocIdSet originalSet = filter.getDocIdSet(reader);
    final DocIdSet cachedSet = cacher.getDocIdSet(reader);
    assertTrue(cachedSet.isCacheable());
    assertEquals(shouldCacheable, originalSet.isCacheable());
    if (originalSet.isCacheable()) {
      assertEquals("Cached DocIdSet must be of same class like uncached, if cacheable", originalSet.getClass(), cachedSet.getClass());
    } else {
      assertTrue("Cached DocIdSet must be an OpenBitSet if the original one was not cacheable", cachedSet instanceof OpenBitSetDISI);
    }
  }
  public void testIsCacheAble() throws Exception {
    Directory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
    writer.close();
    IndexReader reader = IndexReader.open(dir, true);
    assertDocIdSetCacheable(reader, new QueryWrapperFilter(new TermQuery(new Term("test","value"))), false);
    assertDocIdSetCacheable(reader, NumericRangeFilter.newIntRange("test", Integer.valueOf(10000), Integer.valueOf(-10000), true, true), true);
    assertDocIdSetCacheable(reader, FieldCacheRangeFilter.newIntRange("test", Integer.valueOf(10), Integer.valueOf(20), true, true), true);
    assertDocIdSetCacheable(reader, new Filter() {
      @Override
      public DocIdSet getDocIdSet(IndexReader reader) {
        return new OpenBitSet();
      }
    }, true);
    reader.close();
  }
}
