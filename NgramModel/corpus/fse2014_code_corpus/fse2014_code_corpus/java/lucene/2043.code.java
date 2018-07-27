package org.apache.lucene.util.cache;
public class TestDoubleBarrelLRUCache extends BaseTestLRU {
  public void testLRUCache() throws Exception {
    final int n = 100;
    testCache(new DoubleBarrelLRUCache<Integer,Object>(n), n);
  }
  private class CacheThread extends Thread {
    private final Object[] objs;
    private final Cache<Object,Object> c;
    private final long endTime;
    volatile boolean failed;
    public CacheThread(Cache<Object,Object> c,
                     Object[] objs, long endTime) {
      this.c = c;
      this.objs = objs;
      this.endTime = endTime;
    }
    @Override
    public void run() {
      try {
        long count = 0;
        long miss = 0;
        long hit = 0;
        final int limit = objs.length;
        while(true) {
          final Object obj = objs[(int) ((count/2) % limit)];
          Object v = c.get(obj);
          if (v == null) {
            c.put(obj, obj);
            miss++;
          } else {
            assert obj == v;
            hit++;
          }
          if ((++count % 10000) == 0) {
            if (System.currentTimeMillis() >= endTime)  {
              break;
            }
          }
        }
        addResults(miss, hit);
      } catch (Throwable t) {
        failed = true;
        throw new RuntimeException(t);
      }
    }
  }
  long totMiss, totHit;
  void addResults(long miss, long hit) {
    totMiss += miss;
    totHit += hit;
  }
  public void testThreadCorrectness() throws Exception {
    final int NUM_THREADS = 4;
    final int CACHE_SIZE = 512;
    final int OBJ_COUNT = 3*CACHE_SIZE;
    Cache<Object,Object> c = new DoubleBarrelLRUCache<Object,Object>(1024);
    Object[] objs = new Object[OBJ_COUNT];
    for(int i=0;i<OBJ_COUNT;i++) {
      objs[i] = new Object();
    }
    final CacheThread[] threads = new CacheThread[NUM_THREADS];
    final long endTime = System.currentTimeMillis()+1000L;
    for(int i=0;i<NUM_THREADS;i++) {
      threads[i] = new CacheThread(c, objs, endTime);
      threads[i].start();
    }
    for(int i=0;i<NUM_THREADS;i++) {
      threads[i].join();
      assert !threads[i].failed;
    }
  }
}
