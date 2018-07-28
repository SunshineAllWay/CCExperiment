package org.apache.lucene.util.cache;
@Deprecated
public class TestSimpleLRUCache extends BaseTestLRU {
  public void testLRUCache() throws Exception {
    final int n = 100;
    testCache(new SimpleLRUCache<Integer,Object>(n), n);
  }
}
