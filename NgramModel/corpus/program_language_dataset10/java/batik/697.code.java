package org.apache.batik.dom.util;
import java.io.Serializable;
public class HashTable implements Serializable {
    protected static final int INITIAL_CAPACITY = 11;
    protected Entry[] table;
    protected int count;
    public HashTable() {
        table = new Entry[INITIAL_CAPACITY];
    }
    public HashTable( int c ) {
        table = new Entry[c];
    }
    public HashTable( HashTable t ) {
        count = t.count;
        table = new Entry[t.table.length];
        for ( int i = 0; i < table.length; i++ ) {
            Entry e = t.table[ i ];
            Entry n = null;
            if ( e != null ) {
                n = new Entry( e.hash, e.key, e.value, null );
                table[ i ] = n;
                e = e.next;
                while ( e != null ) {
                    n.next = new Entry( e.hash, e.key, e.value, null );
                    n = n.next;
                    e = e.next;
                }
            }
        }
    }
    public int size() {
        return count;
    }
    public Object get(Object key) {
        int hash  = key == null ? 0 : key.hashCode() & 0x7FFFFFFF;
        int index = hash % table.length;
        for (Entry e = table[index]; e != null; e = e.next) {
            if (e.hash == hash
                          && (e.key == null && key == null
                              || e.key != null && e.key.equals(key))) {
                return e.value;
            }
        }
        return null;
    }
    public Object put(Object key, Object value) {
        int hash  = key == null ? 0 : key.hashCode() & 0x7FFFFFFF;
        int index = hash % table.length;
        for (Entry e = table[index]; e != null; e = e.next) {
            if (e.hash == hash
                          && (e.key == null && key == null
                              || e.key != null && e.key.equals(key))) {
                Object old = e.value;
                e.value = value;
                return old;
            }
        }
        int len = table.length;
        if (count++ >= (len - ( len >> 2 ))) {
            rehash();
            index = hash % table.length;
        }
        Entry e = new Entry(hash, key, value, table[index]);
        table[index] = e;
        return null;
    }
    public Object remove( Object key ) {
        int hash = key == null ? 0 : key.hashCode() & 0x7FFFFFFF;
        int index = hash % table.length;
        Entry p = null;
        for ( Entry e = table[ index ]; e != null; e = e.next ) {
            if ( e.hash == hash
                    && ( e.key == null && key == null
                    || e.key != null && e.key.equals( key ) ) ) {
                Object result = e.value;
                if ( p == null ) {
                    table[ index ] = e.next;
                } else {
                    p.next = e.next;
                }
                count--;
                return result;
            }
            p = e;
        }
        return null;
    }
    public Object key( int index ) {
        if ( index < 0 || index >= count ) {
            return null;
        }
        int j = 0;
        for ( int i = 0; i < table.length; i++ ) {
            Entry e = table[ i ];
            if ( e == null ) {
                continue;
            }
            do {
                if ( j++ == index ) {
                    return e.key;
                }
                e = e.next;
            } while ( e != null );
        }
        return null;
    }
    public Object item( int index ) {
        if ( index < 0 || index >= count ) {
            return null;
        }
        int j = 0;
        for ( int i = 0; i < table.length; i++ ) {
            Entry e = table[ i ];
            if ( e == null ) {
                continue;
            }
            do {
                if ( j++ == index ) {
                    return e.value;
                }
                e = e.next;
            } while ( e != null );
        }
        return null;
    }
    public void clear() {
        for ( int i = 0; i < table.length; i++ ) {
            table[ i ] = null;
        }
        count = 0;
    }
    protected void rehash() {
        Entry[] oldTable = table;
        table = new Entry[oldTable.length * 2 + 1];
        for ( int i = oldTable.length - 1; i >= 0; i-- ) {
            for ( Entry old = oldTable[ i ]; old != null; ) {
                Entry e = old;
                old = old.next;
                int index = e.hash % table.length;
                e.next = table[ index ];
                table[ index ] = e;
            }
        }
    }
    protected static class Entry
            implements Serializable {
        public int hash;
        public Object key;
        public Object value;
        public Entry next;
        public Entry( int hash, Object key, Object value, Entry next ) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
}
