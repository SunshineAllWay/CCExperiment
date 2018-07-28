package org.apache.xerces.util;
public class SymbolHash {
    protected int fTableSize = 101;
    protected Entry[] fBuckets; 
    protected int fNum = 0;
    public SymbolHash() {
        fBuckets = new Entry[fTableSize];
    }
    public SymbolHash(int size) {
        fTableSize = size;
        fBuckets = new Entry[fTableSize];
    }
    public void put(Object key, Object value) {
        int bucket = (key.hashCode() & 0x7FFFFFFF) % fTableSize;
        Entry entry = search(key, bucket);
        if (entry != null) {
            entry.value = value;
        }
        else {
            entry = new Entry(key, value, fBuckets[bucket]);
            fBuckets[bucket] = entry;
            fNum++;
        }
    }
    public Object get(Object key) {
        int bucket = (key.hashCode() & 0x7FFFFFFF) % fTableSize;
        Entry entry = search(key, bucket);
        if (entry != null) {
            return entry.value;
        }
        return null;
    }
    public int getLength() {
        return fNum;
    }
    public int getValues(Object[] elements, int from) {
        for (int i=0, j=0; i<fTableSize && j<fNum; i++) {
            for (Entry entry = fBuckets[i]; entry != null; entry = entry.next) {
                elements[from+j] = entry.value;
                j++;
            }
        }
        return fNum;
    }
    public Object[] getEntries() {
        Object[] entries = new Object[fNum << 1];
        for (int i=0, j=0; i<fTableSize && j<fNum << 1; i++) {
            for (Entry entry = fBuckets[i]; entry != null; entry = entry.next) {
                entries[j] = entry.key;
                entries[++j] = entry.value; 
                j++;
            }
        }
        return entries;
    }
    public SymbolHash makeClone() {
        SymbolHash newTable = new SymbolHash(fTableSize);
        newTable.fNum = fNum;
        for (int i = 0; i < fTableSize; i++) {
            if (fBuckets[i] != null)
                newTable.fBuckets[i] = fBuckets[i].makeClone();
        }
        return newTable;
    }
    public void clear() {
        for (int i=0; i<fTableSize; i++) {
            fBuckets[i] = null;
        }
        fNum = 0;
    } 
    protected Entry search(Object key, int bucket) {
        for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            if (key.equals(entry.key))
                return entry;
        }
        return null;
    }
    protected static final class Entry {
        public Object key;
        public Object value;
        public Entry next;
        public Entry() {
            key = null;
            value = null;
            next = null;
        }
        public Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
        public Entry makeClone() {
            Entry entry = new Entry();
            entry.key = key;
            entry.value = value;
            if (next != null)
                entry.next = next.makeClone();
            return entry;
        }
    } 
} 
