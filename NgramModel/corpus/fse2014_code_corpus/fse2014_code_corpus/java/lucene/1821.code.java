package org.apache.lucene.util.cache;
import java.io.Closeable;
public abstract class Cache<K,V> implements Closeable {
  static class SynchronizedCache<K,V> extends Cache<K,V> {
    private Object mutex;
    private Cache<K,V> cache;
    SynchronizedCache(Cache<K,V> cache) {
      this.cache = cache;
      this.mutex = this;
    }
    SynchronizedCache(Cache<K,V> cache, Object mutex) {
      this.cache = cache;
      this.mutex = mutex;
    }
    @Override
    public void put(K key, V value) {
      synchronized(mutex) {cache.put(key, value);}
    }
    @Override
    public V get(Object key) {
      synchronized(mutex) {return cache.get(key);}
    }
    @Override
    public boolean containsKey(Object key) {
      synchronized(mutex) {return cache.containsKey(key);}
    }
    @Override
    public void close() {
      synchronized(mutex) {cache.close();}
    }
    @Override
    Cache<K,V> getSynchronizedCache() {
      return this;
    }
  }
  public static <K,V> Cache<K,V> synchronizedCache(Cache<K,V> cache) {
    return cache.getSynchronizedCache();
  }
  Cache<K,V> getSynchronizedCache() {
    return new SynchronizedCache<K,V>(this);
  }
  public abstract void put(K key, V value);
  public abstract V get(Object key);
  public abstract boolean containsKey(Object key);
  public abstract void close();
}
