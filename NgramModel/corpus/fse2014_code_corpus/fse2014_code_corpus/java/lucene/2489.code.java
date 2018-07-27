package org.apache.solr.search;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.io.IOException;
import java.net.URL;
public class LRUCache<K,V> implements SolrCache<K,V> {
  private static class CumulativeStats {
    AtomicLong lookups = new AtomicLong();
    AtomicLong hits = new AtomicLong();
    AtomicLong inserts = new AtomicLong();
    AtomicLong evictions = new AtomicLong();
  }
  private CumulativeStats stats;
  private long lookups;
  private long hits;
  private long inserts;
  private long evictions;
  private long warmupTime = 0;
  private Map<K,V> map;
  private String name;
  private int autowarmCount;
  private State state;
  private CacheRegenerator regenerator;
  private String description="LRU Cache";
  public Object init(Map args, Object persistence, CacheRegenerator regenerator) {
    state=State.CREATED;
    this.regenerator = regenerator;
    name = (String)args.get("name");
    String str = (String)args.get("size");
    final int limit = str==null ? 1024 : Integer.parseInt(str);
    str = (String)args.get("initialSize");
    final int initialSize = Math.min(str==null ? 1024 : Integer.parseInt(str), limit);
    str = (String)args.get("autowarmCount");
    autowarmCount = str==null ? 0 : Integer.parseInt(str);
    description = "LRU Cache(maxSize=" + limit + ", initialSize=" + initialSize;
    if (autowarmCount>0) {
      description += ", autowarmCount=" + autowarmCount
              + ", regenerator=" + regenerator;
    }
    description += ')';
    map = new LinkedHashMap<K,V>(initialSize, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry eldest) {
          if (size() > limit) {
            evictions++;
            stats.evictions.incrementAndGet();
            return true;
          }
          return false;
        }
      };
    if (persistence==null) {
      persistence = new CumulativeStats();
    }
    stats = (CumulativeStats)persistence;
    return persistence;
  }
  public String name() {
    return name;
  }
  public int size() {
    synchronized(map) {
      return map.size();
    }
  }
  public V put(K key, V value) {
    synchronized (map) {
      if (state == State.LIVE) {
        stats.inserts.incrementAndGet();
      }
      inserts++;
      return map.put(key,value);
    }
  }
  public V get(K key) {
    synchronized (map) {
      V val = map.get(key);
      if (state == State.LIVE) {
        lookups++;
        stats.lookups.incrementAndGet();
        if (val!=null) {
          hits++;
          stats.hits.incrementAndGet();
        }
      }
      return val;
    }
  }
  public void clear() {
    synchronized(map) {
      map.clear();
    }
  }
  public void setState(State state) {
    this.state = state;
  }
  public State getState() {
    return state;
  }
  public void warm(SolrIndexSearcher searcher, SolrCache<K,V> old) throws IOException {
    if (regenerator==null) return;
    long warmingStartTime = System.currentTimeMillis();
    LRUCache<K,V> other = (LRUCache<K,V>)old;
    if (autowarmCount != 0) {
      Object[] keys,vals = null;
      synchronized (other.map) {
        int sz = other.map.size();
        if (autowarmCount!=-1) sz = Math.min(sz,autowarmCount);
        keys = new Object[sz];
        vals = new Object[sz];
        Iterator<Map.Entry<K, V>> iter = other.map.entrySet().iterator();
        int skip = other.map.size() - sz;
        for (int i=0; i<skip; i++) iter.next();
        for (int i=0; i<sz; i++) {
          Map.Entry<K,V> entry = iter.next();
          keys[i]=entry.getKey();
          vals[i]=entry.getValue();
        }
      }
      for (int i=0; i<keys.length; i++) {
        try {
          boolean continueRegen = regenerator.regenerateItem(searcher, this, old, keys[i], vals[i]);
          if (!continueRegen) break;
        }
        catch (Throwable e) {
          SolrException.log(log,"Error during auto-warming of key:" + keys[i], e);
        }
      }
    }
    warmupTime = System.currentTimeMillis() - warmingStartTime;
  }
  public void close() {
  }
  public String getName() {
    return LRUCache.class.getName();
  }
  public String getVersion() {
    return SolrCore.version;
  }
  public String getDescription() {
    return description;
  }
  public Category getCategory() {
    return Category.CACHE;
  }
  public String getSourceId() {
    return "$Id: LRUCache.java 890250 2009-12-14 09:42:00Z shalin $";
  }
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/search/LRUCache.java $";
  }
  public URL[] getDocs() {
    return null;
  }
  private static String calcHitRatio(long lookups, long hits) {
    if (lookups==0) return "0.00";
    if (lookups==hits) return "1.00";
    int hundredths = (int)(hits*100/lookups);   
    if (hundredths < 10) return "0.0" + hundredths;
    return "0." + hundredths;
  }
  public NamedList getStatistics() {
    NamedList lst = new SimpleOrderedMap();
    synchronized (map) {
      lst.add("lookups", lookups);
      lst.add("hits", hits);
      lst.add("hitratio", calcHitRatio(lookups,hits));
      lst.add("inserts", inserts);
      lst.add("evictions", evictions);
      lst.add("size", map.size());
    }
    lst.add("warmupTime", warmupTime);
    long clookups = stats.lookups.get();
    long chits = stats.hits.get();
    lst.add("cumulative_lookups", clookups);
    lst.add("cumulative_hits", chits);
    lst.add("cumulative_hitratio", calcHitRatio(clookups,chits));
    lst.add("cumulative_inserts", stats.inserts.get());
    lst.add("cumulative_evictions", stats.evictions.get());
    return lst;
  }
  public String toString() {
    return name + getStatistics().toString();
  }
}
