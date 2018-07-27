package org.apache.lucene.util.cache;
import org.apache.lucene.util.LuceneTestCase;
public class BaseTestLRU extends LuceneTestCase {
  protected void testCache(Cache<Integer,Object> cache, int n) throws Exception {
    Object dummy = new Object();
    for (int i = 0; i < n; i++) {
      cache.put(Integer.valueOf(i), dummy);
    }
    for (int i = 0; i < n; i+=2) {
      assertNotNull(cache.get(Integer.valueOf(i)));
    }
    for (int i = n; i < n + (n / 2); i++) {
      cache.put(Integer.valueOf(i), dummy);
    }
    for (int i = 0; i < n; i+=4) {
      assertNotNull(cache.get(Integer.valueOf(i)));
    }
    for (int i = n; i < n + (n * 3 / 4); i++) {
      cache.put(Integer.valueOf(i), dummy);
    }
    for (int i = 0; i < n; i+=4) {
      assertNotNull(cache.get(Integer.valueOf(i)));
    }
  }
}
