package org.apache.batik.util;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
public class SoftDoublyIndexedTable {
    protected static final int INITIAL_CAPACITY = 11;
    protected Entry[] table;
    protected int count;
    protected ReferenceQueue referenceQueue = new ReferenceQueue();
    public SoftDoublyIndexedTable() {
        table = new Entry[INITIAL_CAPACITY];
    }
    public SoftDoublyIndexedTable(int c) {
        table = new Entry[c];
    }
    public int size() {
        return count;
    }
    public Object get( Object o1, Object o2 ) {
        int hash = hashCode( o1, o2 ) & 0x7FFFFFFF;
        int index = hash % table.length;
        for ( Entry e = table[ index ]; e != null; e = e.next ) {
            if ( ( e.hash == hash ) && e.match( o1, o2 ) ) {
                return e.get();
            }
        }
        return null;
    }
    public Object put( Object o1, Object o2, Object value ) {
        removeClearedEntries();
        int hash = hashCode( o1, o2 ) & 0x7FFFFFFF;
        int index = hash % table.length;
        Entry e = table[ index ];
        if ( e != null ) {
            if ( ( e.hash == hash ) && e.match( o1, o2 ) ) {
                Object old = e.get();
                table[ index ] = new Entry( hash, o1, o2, value, e.next );
                return old;
            }
            Entry o = e;
            e = e.next;
            while ( e != null ) {
                if ( ( e.hash == hash ) && e.match( o1, o2 ) ) {
                    Object old = e.get();
                    e = new Entry( hash, o1, o2, value, e.next );
                    o.next = e;
                    return old;
                }
                o = e;
                e = e.next;
            }
        }
        int len = table.length;
        if ( count++ >= ( len - ( len >> 2 ) ) ) {
            rehash();
            index = hash % table.length;
        }
        table[ index ] = new Entry( hash, o1, o2, value, table[ index ] );
        return null;
    }
    public void clear() {
        table = new Entry[INITIAL_CAPACITY];
        count = 0;
        referenceQueue = new ReferenceQueue();
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
    protected int hashCode(Object o1, Object o2) {
        int result = (o1 == null) ? 0 : o1.hashCode();
        return result ^ ((o2 == null) ? 0 : o2.hashCode());
    }
    protected void removeClearedEntries() {
        Entry e;
        while ((e = (Entry)referenceQueue.poll()) != null) {
            int index = e.hash % table.length;
            Entry t = table[index];
            if (t == e) {
                table[index] = e.next;
            } else {
                loop: for (;t!=null;) {
                    Entry c = t.next;
                    if (c == e) {
                        t.next = e.next;
                        break loop;
                    }
                    t = c;
                }
            }
            count--;
        }
    }
    protected class Entry extends SoftReference {
        public int hash;
        public Object key1;
        public Object key2;
        public Entry next;
        public Entry( int hash, Object key1, Object key2, Object value, Entry next ) {
            super( value, referenceQueue );
            this.hash = hash;
            this.key1 = key1;
            this.key2 = key2;
            this.next = next;
        }
        public boolean match( Object o1, Object o2 ) {
            if ( key1 != null ) {
                if ( !key1.equals( o1 ) ) {
                    return false;
                }
            } else if ( o1 != null ) {
                return false;
            }
            if ( key2 != null ) {
                return key2.equals( o2 );
            }
            return o2 == null;
        }
    }
}
