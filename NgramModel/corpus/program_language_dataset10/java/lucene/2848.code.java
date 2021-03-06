package org.apache.solr.search;
import junit.framework.TestCase;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.ConcurrentLRUCache;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
public class TestFastLRUCache extends TestCase {
  public void testSimple() throws IOException {
    FastLRUCache sc = new FastLRUCache();
    Map l = new HashMap();
    l.put("size", "100");
    l.put("initialSize", "10");
    l.put("autowarmCount", "25");
    CacheRegenerator cr = new CacheRegenerator() {
      public boolean regenerateItem(SolrIndexSearcher newSearcher, SolrCache newCache,
                                    SolrCache oldCache, Object oldKey, Object oldVal) throws IOException {
        newCache.put(oldKey, oldVal);
        return true;
      }
    };
    Object o = sc.init(l, null, cr);
    sc.setState(SolrCache.State.LIVE);
    for (int i = 0; i < 101; i++) {
      sc.put(i + 1, "" + (i + 1));
    }
    assertEquals("25", sc.get(25));
    assertEquals(null, sc.get(110));
    NamedList nl = sc.getStatistics();
    assertEquals(2L, nl.get("lookups"));
    assertEquals(1L, nl.get("hits"));
    assertEquals(101L, nl.get("inserts"));
    assertEquals(null, sc.get(1));  
    FastLRUCache scNew = new FastLRUCache();
    scNew.init(l, o, cr);
    scNew.warm(null, sc);
    scNew.setState(SolrCache.State.LIVE);
    sc.close();
    scNew.put(103, "103");
    assertEquals("90", scNew.get(90));
    assertEquals(null, scNew.get(50));
    nl = scNew.getStatistics();
    assertEquals(2L, nl.get("lookups"));
    assertEquals(1L, nl.get("hits"));
    assertEquals(1L, nl.get("inserts"));
    assertEquals(0L, nl.get("evictions"));
    assertEquals(5L, nl.get("cumulative_lookups"));
    assertEquals(2L, nl.get("cumulative_hits"));
    assertEquals(102L, nl.get("cumulative_inserts"));
    scNew.close();
  }
  public void testOldestItems() {
    ConcurrentLRUCache<Integer, String> cache = new ConcurrentLRUCache<Integer, String>(100, 90);
    for (int i = 0; i < 50; i++) {
      cache.put(i + 1, "" + (i + 1));
    }
    cache.get(1);
    cache.get(3);
    Map<Integer, String> m = cache.getOldestAccessedItems(5);
    assertNotNull(m.get(7));
    assertNotNull(m.get(6));
    assertNotNull(m.get(5));
    assertNotNull(m.get(4));
    assertNotNull(m.get(2));
    cache.destroy();
  }
  void doPerfTest(int iter, int cacheSize, int maxKey) {
    long start = System.currentTimeMillis();
    int lowerWaterMark = cacheSize;
    int upperWaterMark = (int)(lowerWaterMark * 1.1);
    Random r = new Random(0);
    ConcurrentLRUCache cache = new ConcurrentLRUCache(upperWaterMark, lowerWaterMark, (upperWaterMark+lowerWaterMark)/2, upperWaterMark, false, false, null);
    boolean getSize=false;
    int minSize=0,maxSize=0;
    for (int i=0; i<iter; i++) {
      cache.put(r.nextInt(maxKey),"TheValue");
      int sz = cache.size();
      if (!getSize && sz >= cacheSize) {
        getSize = true;
        minSize = sz;
      } else {
        if (sz < minSize) minSize=sz;
        else if (sz > maxSize) maxSize=sz;
      }
    }
    cache.destroy();
    long end = System.currentTimeMillis();
    System.out.println("time=" + (end-start) + ", minSize="+minSize+",maxSize="+maxSize);
  }
  int useCache(SolrCache sc, int numGets, int maxKey, int seed) {
    int ret = 0;
    Random r = new Random(seed);
    for (int i=0; i<numGets; i++) {
      Integer k = r.nextInt(maxKey);
      Integer v = (Integer)sc.get(k);
      if (v == null) {
        sc.put(k, k);
        ret++;
      }
    }
    return ret;
  }
  void fillCache(SolrCache sc, int cacheSize, int maxKey) {
    Random r = new Random(0);
    for (int i=0; i<cacheSize; i++) {
      Integer kv = r.nextInt(maxKey);
      sc.put(kv,kv);
    }
  }
  void cachePerfTest(final SolrCache sc, final int nThreads, final int numGets, int cacheSize, final int maxKey) {
    Map l = new HashMap();
    l.put("size", ""+cacheSize);
    l.put("initialSize", ""+cacheSize);
    Object o = sc.init(l, null, null);
    sc.setState(SolrCache.State.LIVE);
    fillCache(sc, cacheSize, maxKey);
    long start = System.currentTimeMillis();
    Thread[] threads = new Thread[nThreads];
    final AtomicInteger puts = new AtomicInteger(0);
    for (int i=0; i<threads.length; i++) {
      final int seed=i;
      threads[i] = new Thread() {
        public void run() {
          int ret = useCache(sc, numGets/nThreads, maxKey, seed);
          puts.addAndGet(ret);
        }
      };
    }
    for (Thread thread : threads) {
      try {
        thread.start();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    long end = System.currentTimeMillis();
    System.out.println("time=" + (end-start) + " impl=" +sc.getClass().getSimpleName()
            +" nThreads= " + nThreads + " size="+cacheSize+" maxKey="+maxKey+" gets="+numGets
            +" hitRatio="+(1-(((double)puts.get())/numGets)));
  }
  void perfTestBoth(int nThreads, int numGets, int cacheSize, int maxKey) {
    cachePerfTest(new LRUCache(), nThreads, numGets, cacheSize, maxKey);
    cachePerfTest(new FastLRUCache(), nThreads, numGets, cacheSize, maxKey);
  }
}
