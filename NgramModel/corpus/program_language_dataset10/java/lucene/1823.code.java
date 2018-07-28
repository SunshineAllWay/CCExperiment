package org.apache.lucene.util.cache;
import java.util.LinkedHashMap;
import java.util.Map;
@Deprecated
public class SimpleLRUCache<K,V> extends SimpleMapCache<K,V> {
  private final static float LOADFACTOR = 0.75f;
  public SimpleLRUCache(final int cacheSize) {
    super(new LinkedHashMap<K,V>((int) Math.ceil(cacheSize / LOADFACTOR) + 1, LOADFACTOR, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > cacheSize;
      }
    });
  }
}
