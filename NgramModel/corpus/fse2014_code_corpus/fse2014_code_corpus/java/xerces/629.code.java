package org.apache.xerces.util;
public class SymbolTable {
    protected static final int TABLE_SIZE = 101;
    protected Entry[] fBuckets = null;
    protected int fTableSize;
    protected transient int fCount;
    protected int fThreshold;
    protected float fLoadFactor;
    public SymbolTable(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal Load: " + loadFactor);
        }
        if (initialCapacity == 0) {
            initialCapacity = 1;
        }
        fLoadFactor = loadFactor;
        fTableSize = initialCapacity;
        fBuckets = new Entry[fTableSize];
        fThreshold = (int)(fTableSize * loadFactor);
        fCount = 0;
    }
    public SymbolTable(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }
    public SymbolTable() {
        this(TABLE_SIZE, 0.75f);
    }
    public String addSymbol(String symbol) {
        int bucket = hash(symbol) % fTableSize;
        for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            if (entry.symbol.equals(symbol)) {
                return entry.symbol;
            }
        }
        if (fCount >= fThreshold) {
            rehash();
            bucket = hash(symbol) % fTableSize;
        } 
        Entry entry = new Entry(symbol, fBuckets[bucket]);
        fBuckets[bucket] = entry;
        ++fCount;
        return entry.symbol;
    } 
    public String addSymbol(char[] buffer, int offset, int length) {
        int bucket = hash(buffer, offset, length) % fTableSize;
        OUTER: for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            if (length == entry.characters.length) {
                for (int i = 0; i < length; i++) {
                    if (buffer[offset + i] != entry.characters[i]) {
                        continue OUTER;
                    }
                }
                return entry.symbol;
            }
        }
        if (fCount >= fThreshold) {
            rehash();
            bucket = hash(buffer, offset, length) % fTableSize;
        } 
        Entry entry = new Entry(buffer, offset, length, fBuckets[bucket]);
        fBuckets[bucket] = entry;
        ++fCount;
        return entry.symbol;
    } 
    public int hash(String symbol) {
        return symbol.hashCode() & 0x7FFFFFFF;
    } 
    public int hash(char[] buffer, int offset, int length) {
        int code = 0;
        for (int i = 0; i < length; ++i) {
            code = code * 31 + buffer[offset + i];
        }
        return code & 0x7FFFFFFF;
    } 
    protected void rehash() {
        int oldCapacity = fBuckets.length;
        Entry[] oldTable = fBuckets;
        int newCapacity = oldCapacity * 2 + 1;
        Entry[] newTable = new Entry[newCapacity];
        fThreshold = (int)(newCapacity * fLoadFactor);
        fBuckets = newTable;
        fTableSize = fBuckets.length;
        for (int i = oldCapacity ; i-- > 0 ;) {
            for (Entry old = oldTable[i] ; old != null ; ) {
                Entry e = old;
                old = old.next;
                int index = hash(e.characters, 0, e.characters.length) % newCapacity;
                e.next = newTable[index];
                newTable[index] = e;
            }
        }
    }
    public boolean containsSymbol(String symbol) {
        int bucket = hash(symbol) % fTableSize;
        int length = symbol.length();
        OUTER: for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            if (length == entry.characters.length) {
                for (int i = 0; i < length; i++) {
                    if (symbol.charAt(i) != entry.characters[i]) {
                        continue OUTER;
                    }
                }
                return true;
            }
        }
        return false;
    } 
    public boolean containsSymbol(char[] buffer, int offset, int length) {
        int bucket = hash(buffer, offset, length) % fTableSize;
        OUTER: for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            if (length == entry.characters.length) {
                for (int i = 0; i < length; i++) {
                    if (buffer[offset + i] != entry.characters[i]) {
                        continue OUTER;
                    }
                }
                return true;
            }
        }
        return false;
    } 
    protected static final class Entry {
        public final String symbol;
        public final char[] characters;
        public Entry next;
        public Entry(String symbol, Entry next) {
            this.symbol = symbol.intern();
            characters = new char[symbol.length()];
            symbol.getChars(0, characters.length, characters, 0);
            this.next = next;
        }
        public Entry(char[] ch, int offset, int length, Entry next) {
            characters = new char[length];
            System.arraycopy(ch, offset, characters, 0, length);
            symbol = new String(characters).intern();
            this.next = next;
        }
    } 
} 
