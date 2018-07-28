package org.apache.batik.dom.util;
import java.io.Serializable;
public class IntTable implements Serializable {
    protected static final int INITIAL_CAPACITY = 11;
    protected Entry[] table;
    protected int count;
    public IntTable() {
        table = new Entry[INITIAL_CAPACITY];
    }
    public IntTable(int c) {
        table = new Entry[c];
    }
    public IntTable(IntTable t) {
        count = t.count;
        table = new Entry[t.table.length];
        for (int i = 0; i < table.length; i++) {
            Entry e = t.table[i];
            Entry n = null;
            if (e != null) {
                n = new Entry(e.hash, e.key, e.value, null);
                table[i] = n;
                e = e.next;
                while (e != null) {
                    n.next = new Entry(e.hash, e.key, e.value, null);
                    n = n.next;
                    e = e.next;
                }
            }
        }
    }
    public int size() {
        return count;
    }
    protected Entry find(Object key) {
        return null;
    }
    public int get(Object key) {
        int hash  = key == null ? 0 : key.hashCode() & 0x7FFFFFFF;
        int index = hash % table.length;
        for (Entry e = table[index]; e != null; e = e.next) {
            if (e.hash == hash
                    && (e.key == null && key == null
                        || e.key != null && e.key.equals(key))) {
                return e.value;
            }
        }
        return 0;
    }
    public int put(Object key, int value) {
        int hash  = key == null ? 0 : key.hashCode() & 0x7FFFFFFF;
        int index = hash % table.length;
        for (Entry e = table[index]; e != null; e = e.next) {
            if (e.hash == hash
                    && (e.key == null && key == null
                        || e.key != null && e.key.equals(key))) {
                int old = e.value;
                e.value = value;
                return old;
            }
        }
        int len = table.length;
        if (count++ >= (len - (len >> 2))) {
            rehash();
            index = hash % table.length;
        }
        table[index] = new Entry(hash, key, value, table[index]);
        return 0;
    }
    public int inc(Object key) {
        int hash  = key == null ? 0 : key.hashCode() & 0x7FFFFFFF;
        int index = hash % table.length;
        for (Entry e = table[index]; e != null; e = e.next) {
            if (e.hash == hash
                    && (e.key == null && key == null
                        || e.key != null && e.key.equals(key))) {
                return e.value++;
            }
        }
        int len = table.length;
        if (count++ >= (len - (len >> 2))) {
            rehash();
            index = hash % table.length;
        }
        table[index] = new Entry(hash, key, 1, table[index]);
        return 0;
    }
    public int dec(Object key) {
        int hash  = key == null ? 0 : key.hashCode() & 0x7FFFFFFF;
        int index = hash % table.length;
        for (Entry e = table[index]; e != null; e = e.next) {
            if (e.hash == hash
                    && (e.key == null && key == null
                        || e.key != null && e.key.equals(key))) {
                return e.value--;
            }
        }
        int len = table.length;
        if (count++ >= (len - (len >> 2))) {
            rehash();
            index = hash % table.length;
        }
        table[index] = new Entry(hash, key, -1, table[index]);
        return 0;
    }
    public int remove(Object key) {
        int hash  = key == null ? 0 : key.hashCode() & 0x7FFFFFFF;
        int index = hash % table.length;
        Entry p = null;
        for (Entry e = table[index]; e != null; e = e.next) {
            if (e.hash == hash
                    && (e.key == null && key == null
                        || e.key != null && e.key.equals(key))) {
                int result = e.value;
                if (p == null) {
                    table[index] = e.next;
                } else {
                    p.next = e.next;
                }
                count--;
                return result;
            }
            p = e;
        }
        return 0;
    }
    public void clear() {
        for (int i = 0; i < table.length; i++) {
            table[i] = null;
        }
        count = 0;
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
    protected static class Entry implements Serializable {
        public int hash;
        public Object key;
        public int value;
        public Entry next;
        public Entry(int hash, Object key, int value, Entry next) {
            this.hash  = hash;
            this.key   = key;
            this.value = value;
            this.next  = next;
        }
    }
}
