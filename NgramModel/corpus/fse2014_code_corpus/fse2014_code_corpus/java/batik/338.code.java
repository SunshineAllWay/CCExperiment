package org.apache.batik.css.engine.value;
public class StringMap {
    protected static final int INITIAL_CAPACITY = 11;
    protected Entry[] table;
    protected int count;
    public StringMap() {
        table = new Entry[INITIAL_CAPACITY];
    }
    public StringMap( StringMap t ) {
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
    public Object get( String key ) {
        int hash = key.hashCode() & 0x7FFFFFFF;
        int index = hash % table.length;
        for ( Entry e = table[ index ]; e != null; e = e.next ) {
            if ( ( e.hash == hash ) && e.key == key ) {
                return e.value;
            }
        }
        return null;
    }
    public Object put( String key, Object value ) {
        int hash = key.hashCode() & 0x7FFFFFFF;
        int index = hash % table.length;
        for ( Entry e = table[ index ]; e != null; e = e.next ) {
            if ( ( e.hash == hash ) && e.key == key ) {
                Object old = e.value;
                e.value = value;
                return old;
            }
        }
        int len = table.length;
        if ( count++ >= ( len - ( len >> 2 ) ) ) {
            rehash();
            index = hash % table.length;
        }
        Entry e = new Entry( hash, key, value, table[ index ] );
        table[ index ] = e;
        return null;
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
    protected static class Entry {
        public int hash;
        public String key;
        public Object value;
        public Entry next;
        public Entry(int hash, String key, Object value, Entry next) {
            this.hash  = hash;
            this.key   = key;
            this.value = value;
            this.next  = next;
        }
    }
}
