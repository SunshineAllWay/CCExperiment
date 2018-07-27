package org.apache.cassandra.cache;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;
public class InstrumentedCache<K, V>
{
    public static final int DEFAULT_CONCURENCY_LEVEL = 64;
    private final ConcurrentLinkedHashMap<K, V> map;
    private final AtomicLong requests = new AtomicLong(0);
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong lastRequests = new AtomicLong(0);
    private final AtomicLong lastHits = new AtomicLong(0);
    private volatile boolean capacitySetManually;
    public InstrumentedCache(int capacity)
    {
        this(capacity, DEFAULT_CONCURENCY_LEVEL);
    }
    public InstrumentedCache(int capacity, int concurency)
    {
        map = new ConcurrentLinkedHashMap.Builder<K, V>()
                .weigher(Weighers.<V>singleton())
                .initialCapacity(capacity)
                .maximumWeightedCapacity(capacity)
                .concurrencyLevel(concurency)
                .build();
    }
    public void put(K key, V value)
    {
        map.put(key, value);
    }
    public V get(K key)
    {
        V v = map.get(key);
        requests.incrementAndGet();
        if (v != null)
            hits.incrementAndGet();
        return v;
    }
    public V getInternal(K key)
    {
        return map.get(key);
    }
    public void remove(K key)
    {
        map.remove(key);
    }
    public int getCapacity()
    {
        return map.capacity();
    }
    public boolean isCapacitySetManually()
    {
        return capacitySetManually;
    }
    public void updateCapacity(int capacity)
    {
        map.setCapacity(capacity);
    }
    public void setCapacity(int capacity)
    {
        updateCapacity(capacity);
        capacitySetManually = true;
    }
    public int getSize()
    {
        return map.size();
    }
    public long getHits()
    {
        return hits.get();
    }
    public long getRequests()
    {
        return requests.get();
    }
    public double getRecentHitRate()
    {
        long r = requests.get();
        long h = hits.get();
        try
        {
            return ((double)(h - lastHits.get())) / (r - lastRequests.get());
        }
        finally
        {
            lastRequests.set(r);
            lastHits.set(h);
        }
    }
    public void clear()
    {
        map.clear();
        requests.set(0);
        hits.set(0);
    }
    public Set<K> getKeySet()
    {
        return map.keySet();
    }
}
