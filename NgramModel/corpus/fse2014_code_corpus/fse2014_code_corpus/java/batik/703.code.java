package org.apache.batik.dom.util;
public class TriplyIndexedTable {
    protected static final int INITIAL_CAPACITY = 11;
    protected Entry[] table;
    protected int count;
    public TriplyIndexedTable() {
        table = new Entry[INITIAL_CAPACITY];
    }
    public TriplyIndexedTable(int c) {
        table = new Entry[c];
    }
    public int size() {
        return count;
    }
    public Object put(Object o1, Object o2, Object o3, Object value) {
        int hash  = hashCode(o1, o2, o3) & 0x7FFFFFFF;
        int index = hash % table.length;
        for (Entry e = table[index]; e != null; e = e.next) {
            if ((e.hash == hash) && e.match(o1, o2, o3)) {
                Object old = e.value;
                e.value = value;
                return old;
            }
        }
        int len = table.length;
        if (count++ >= (len - (len >> 2))) {
            rehash();
            index = hash % table.length;
        }
        Entry e = new Entry(hash, o1, o2, o3, value, table[index]);
        table[index] = e;
        return null;
    }
    public Object get(Object o1, Object o2, Object o3) {
        int hash  = hashCode(o1, o2, o3) & 0x7FFFFFFF;
        int index = hash % table.length;
        for (Entry e = table[index]; e != null; e = e.next) {
            if ((e.hash == hash) && e.match(o1, o2, o3)) {
                return e.value;
            }
        }
        return null;
    }
    protected void rehash() {
        Entry[] oldTable = table;
        table = new Entry[oldTable.length * 2 + 1];
        for (int i = oldTable.length-1; i >= 0; i--) {
            for (Entry old = oldTable[i]; old != null;) {
                Entry e = old;
                old = old.next;
                int index = e.hash % table.length;
                e.next = table[index];
                table[index] = e;
            }
        }
    }
    protected int hashCode(Object o1, Object o2, Object o3) {
        return (o1 == null ? 0 : o1.hashCode())
             ^ (o2 == null ? 0 : o2.hashCode())
             ^ (o3 == null ? 0 : o3.hashCode());
    }
    protected static class Entry {
        public int hash;
        public Object key1;
        public Object key2;
        public Object key3;
        public Object value;
        public Entry next;
        public Entry(int hash, Object key1, Object key2, Object key3,
                     Object value, Entry next) {
            this.hash  = hash;
            this.key1  = key1;
            this.key2  = key2;
            this.key3  = key3;
            this.value = value;
            this.next  = next;
        }
        public boolean match(Object o1, Object o2, Object o3) {
            if (key1 != null) {
                if (!key1.equals(o1)) {
                    return false;
                }
            } else if (o1 != null) {
                return false;
            }
            if (key2 != null) {
                if (!key2.equals(o2)) {
                    return false;
                }
            } else if (o2 != null) {
                return false;
            }
            if (key3 != null) {
                return key3.equals(o3);
            }
            return o3 == null;
        }
    }
}
