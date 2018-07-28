package org.apache.tools.ant.util;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
public class LinkedHashtable extends Hashtable {
    private final LinkedHashMap map;
    public LinkedHashtable() {
        map = new LinkedHashMap();
    }
    public LinkedHashtable(int initialCapacity) {
        map = new LinkedHashMap(initialCapacity);
    }
    public LinkedHashtable(int initialCapacity, float loadFactor) {
        map = new LinkedHashMap(initialCapacity, loadFactor);
    }
    public LinkedHashtable(Map m) {
        map = new LinkedHashMap(m);
    }
    public synchronized void clear() {
        map.clear();
    }
    public boolean contains(Object value) {
        return containsKey(value);
    }
    public synchronized boolean containsKey(Object value) {
        return map.containsKey(value);
    }
    public synchronized boolean containsValue(Object value) {
        return map.containsValue(value);
    }
    public Enumeration elements() {
        return CollectionUtils.asEnumeration(values().iterator());
    }
    public synchronized Set entrySet() {
        return map.entrySet();
    }
    public synchronized boolean equals(Object o) {
        return map.equals(o);
    }
    public synchronized Object get(Object k) {
        return map.get(k);
    }
    public synchronized int hashCode() {
        return map.hashCode();
    }
    public synchronized boolean isEmpty() {
        return map.isEmpty();
    }
    public Enumeration keys() {
        return CollectionUtils.asEnumeration(keySet().iterator());
    }
    public synchronized Set keySet() {
        return map.keySet();
    }
    public synchronized Object put(Object k, Object v) {
        return map.put(k, v);
    }
    public synchronized void putAll(Map m) {
        map.putAll(m);
    }
    public synchronized Object remove(Object k) {
        return map.remove(k);
    }
    public synchronized int size() {
        return map.size();
    }
    public synchronized String toString() {
        return map.toString();
    }
    public synchronized Collection values() {
        return map.values();
    }
}
