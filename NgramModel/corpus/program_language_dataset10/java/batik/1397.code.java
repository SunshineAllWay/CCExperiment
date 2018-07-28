package org.apache.batik.util;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
public class SoftReferenceCache {
    protected final Map map = new HashMap();
    protected SoftReferenceCache() { }
    public synchronized void flush() {
        map.clear();
        this.notifyAll();
    }
    protected final synchronized boolean isPresentImpl(Object key) {
        if (!map.containsKey(key))
            return false;
        Object o = map.get(key);
        if (o == null)
            return true;
        SoftReference sr = (SoftReference)o;
        o = sr.get();
        if (o != null)
            return true;
        clearImpl(key);
        return false;
    }
    protected final synchronized boolean isDoneImpl(Object key) {
        Object o = map.get(key);
        if (o == null) return false;
        SoftReference sr = (SoftReference)o;
        o = sr.get();
        if (o != null)
            return true;
        clearImpl(key);
        return false;
    }
    protected final synchronized Object requestImpl(Object key) {
        if (map.containsKey(key)) {
            Object o = map.get(key);
            while(o == null) {
                try {
                    wait();
                }
                catch (InterruptedException ie) { }
                if (!map.containsKey(key))
                    break;
                o = map.get(key);
            }
            if (o != null) {
                SoftReference sr = (SoftReference)o;
                o = sr.get();
                if (o != null)
                    return o;
            }
        }
        map.put(key, null);
        return null;
    }
    protected final synchronized void clearImpl(Object key) {
        map.remove(key);
        this.notifyAll();
    }
    protected final synchronized void putImpl(Object key, Object object) {
        if (map.containsKey(key)) {
            SoftReference ref = new SoftRefKey(object, key);
            map.put(key, ref);
            this.notifyAll();
        }
    }
    class SoftRefKey extends CleanerThread.SoftReferenceCleared {
        Object key;
        public SoftRefKey(Object o, Object key) {
            super(o);
            this.key = key;
        }
        public void cleared() {
            SoftReferenceCache cache = SoftReferenceCache.this;
            if (cache == null) return; 
            synchronized (cache) {
                if (!cache.map.containsKey(key))
                    return;
                Object o = cache.map.remove(key);
                if (this == o) {
                    cache.notifyAll();
                } else {
                    cache.map.put(key, o);
                }
            }
        }
    }
}
