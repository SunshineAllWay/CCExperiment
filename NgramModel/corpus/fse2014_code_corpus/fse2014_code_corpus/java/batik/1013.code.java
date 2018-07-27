package org.apache.batik.gvt.font;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
public class AWTGlyphGeometryCache {
    protected static final int INITIAL_CAPACITY = 71;
    protected Entry[] table;
    protected int count;
    protected ReferenceQueue referenceQueue = new ReferenceQueue();
    public AWTGlyphGeometryCache() {
        table = new Entry[INITIAL_CAPACITY];
    }
    public AWTGlyphGeometryCache(int c) {
        table = new Entry[c];
    }
    public int size() {
        return count;
    }
    public Value get(char c) {
        int hash  = hashCode(c) & 0x7FFFFFFF;
        int index = hash % table.length;
        for (Entry e = table[index]; e != null; e = e.next) {
            if ((e.hash == hash) && e.match(c)) {
                return (Value)e.get();
            }
        }
        return null;
    }
    public Value put(char c, Value value) {
        removeClearedEntries();
        int hash  = hashCode(c) & 0x7FFFFFFF;
        int index = hash % table.length;
        Entry e = table[index];
        if (e != null) {
            if ((e.hash == hash) && e.match(c)) {
                Object old = e.get();
                table[index] = new Entry(hash, c, value, e.next);
                return (Value)old;
            }
            Entry o = e;
            e = e.next;
            while (e != null) {
                if ((e.hash == hash) && e.match(c)) {
                    Object old = e.get();
                    e = new Entry(hash, c, value, e.next);
                    o.next = e;
                    return (Value)old;
                }
                o = e;
                e = e.next;
            }
        }
        int len = table.length;
        if (count++ >= (len - (len >> 2))) {
            rehash();
            index = hash % table.length;
        }
        table[index] = new Entry(hash, c, value, table[index]);
        return null;
    }
    public void clear() {
        table = new Entry[INITIAL_CAPACITY];
        count = 0;
        referenceQueue = new ReferenceQueue();
    }
    protected void rehash () {
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
    protected int hashCode(char c) {
        return c;
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
    public static class Value {
        protected Shape outline;
        protected Rectangle2D gmB;
        protected Rectangle2D outlineBounds;
        public Value(Shape outline, Rectangle2D gmB) {
            this.outline = outline;
            this.outlineBounds = outline.getBounds2D();
            this.gmB = gmB;
        }
        public Shape getOutline() {
            return outline;
        }
        public Rectangle2D getBounds2D() {
            return gmB;
        }
        public Rectangle2D getOutlineBounds2D() {
            return outlineBounds;
        }
    }
    protected class Entry extends SoftReference {
        public int hash;
        public char c;
        public Entry next;
        public Entry(int hash, char c, Value value, Entry next) {
            super(value, referenceQueue);
            this.hash  = hash;
            this.c  = c;
            this.next  = next;
        }
        public boolean match(char o2) {
            return (c == o2);
        }
    }
}
