package org.apache.batik.util;
import java.util.Iterator;
import java.util.NoSuchElementException;
public class DoublyIndexedTable {
    protected int initialCapacity;
    protected Entry[] table;
    protected int count;
    public DoublyIndexedTable() {
        this(16);
    }
    public DoublyIndexedTable(int c) {
        initialCapacity = c;
        table = new Entry[c];
    }
    public DoublyIndexedTable(DoublyIndexedTable other) {
        initialCapacity = other.initialCapacity;
        table = new Entry[other.table.length];
        for (int i = 0; i < other.table.length; i++) {
            Entry newE = null;
            Entry e = other.table[i];
            while (e != null) {
                newE = new Entry(e.hash, e.key1, e.key2, e.value, newE);
                e = e.next;
            }
            table[i] = newE;
        }
        count = other.count;
    }
    public int size() {
        return count;
    }
    public Object put(Object o1, Object o2, Object value) {
        int hash  = hashCode(o1, o2) & 0x7FFFFFFF;
        int index = hash % table.length;
        for (Entry e = table[index]; e != null; e = e.next) {
            if ((e.hash == hash) && e.match(o1, o2)) {
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
        Entry e = new Entry(hash, o1, o2, value, table[index]);
        table[index] = e;
        return null;
    }
    public Object get(Object o1, Object o2) {
        int hash  = hashCode(o1, o2) & 0x7FFFFFFF;
        int index = hash % table.length;
        for (Entry e = table[index]; e != null; e = e.next) {
            if ((e.hash == hash) && e.match(o1, o2)) {
                return e.value;
            }
        }
        return null;
    }
    public Object remove(Object o1, Object o2) {
        int hash  = hashCode(o1, o2) & 0x7FFFFFFF;
        int index = hash % table.length;
        Entry e = table[index];
        if (e == null) {
            return null;
        }
        if (e.hash == hash && e.match(o1, o2)) {
            table[index] = e.next;
            count--;
            return e.value;
        }
        Entry prev = e;
        for (e = e.next; e != null; prev = e, e = e.next) {
            if (e.hash == hash && e.match(o1, o2)) {
                prev.next = e.next;
                count--;
                return e.value;
            }
        }
        return null;
    }
    public Object[] getValuesArray() {
        Object[] values = new Object[count];
        int i = 0;
        for (int index = 0; index < table.length; index++) {
            for (Entry e = table[index]; e != null; e = e.next) {
                values[i++] = e.value;
            }
        }
        return values;
    }
    public void clear() {
        table = new Entry[initialCapacity];
        count = 0;
    }
    public Iterator iterator() {
        return new TableIterator();
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
    protected int hashCode(Object o1, Object o2) {
        int result = (o1 == null) ? 0 : o1.hashCode();
        return result ^ ((o2 == null) ? 0 : o2.hashCode());
    }
    public static class Entry {
        protected int hash;
        protected Object key1;
        protected Object key2;
        protected Object value;
        protected Entry next;
        public Entry(int hash, Object key1, Object key2, Object value,
                     Entry next) {
            this.hash  = hash;
            this.key1  = key1;
            this.key2  = key2;
            this.value = value;
            this.next  = next;
        }
        public Object getKey1() {
            return key1;
        }
        public Object getKey2() {
            return key2;
        }
        public Object getValue() {
            return value;
        }
        protected boolean match(Object o1, Object o2) {
            if (key1 != null) {
                if (!key1.equals(o1)) {
                    return false;
                }
            } else if (o1 != null) {
                return false;
            }
            if (key2 != null) {
                return key2.equals(o2);
            }
            return o2 == null;
        }
    }
    protected class TableIterator implements Iterator {
        private int nextIndex;
        private Entry nextEntry;
        private boolean finished;
        public TableIterator() {
            while (nextIndex < table.length) {
                nextEntry = table[nextIndex];
                if (nextEntry != null) {
                    break;
                }
                nextIndex++;
            }
            finished = nextEntry == null;
        }
        public boolean hasNext() {
            return !finished;
        }
        public Object next() {
            if (finished) {
                throw new NoSuchElementException();
            }
            Entry ret = nextEntry;
            findNext();
            return ret;
        }
        protected void findNext() {
            nextEntry = nextEntry.next;
            if (nextEntry == null) {
                nextIndex++;
                while (nextIndex < table.length) {
                    nextEntry = table[nextIndex];
                    if (nextEntry != null) {
                        break;
                    }
                    nextIndex++;
                }
            }
            finished = nextEntry == null;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
