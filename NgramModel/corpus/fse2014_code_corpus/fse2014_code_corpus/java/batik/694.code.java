package org.apache.batik.dom.util;
import org.w3c.dom.Element;
import org.apache.batik.util.CleanerThread;
public class DocumentDescriptor {
    protected static final int INITIAL_CAPACITY = 101;
    protected Entry[] table;
    protected int count;
    public DocumentDescriptor() {
        table = new Entry[INITIAL_CAPACITY];
    }
    public int getNumberOfElements() {
        synchronized (this) {
                  return count;
        }
    }
    public int getLocationLine(Element elt) {
        synchronized (this) {
            int hash = elt.hashCode() & 0x7FFFFFFF;
            int index = hash % table.length;
            for (Entry e = table[index]; e != null; e = e.next) {
                if (e.hash != hash)
                    continue;
                Object o = e.get();
                if (o == elt)
                    return e.locationLine;
            }
        }
        return 0;
    }
    public int getLocationColumn(Element elt) {
        synchronized (this) {
            int hash = elt.hashCode() & 0x7FFFFFFF;
            int index = hash % table.length;
            for (Entry e = table[index]; e != null; e = e.next) {
                if (e.hash != hash)
                    continue;
                Object o = e.get();
                if (o == elt)
                    return e.locationColumn;
            }
        }
        return 0;
    }
    public void setLocation(Element elt, int line, int col) {
        synchronized (this) {
            int hash  = elt.hashCode() & 0x7FFFFFFF;
            int index = hash % table.length;
            for (Entry e = table[index]; e != null; e = e.next) {
                if (e.hash != hash)
                    continue;
                Object o = e.get();
                if (o == elt)
                    e.locationLine = line;
            }
            int len = table.length;
            if (count++ >= (len - ( len >> 2 ))) {
                rehash();
                index = hash % table.length;
            }
            Entry e = new Entry(hash, elt, line, col, table[index]);
            table[index] = e;
        }
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
    protected void removeEntry(Entry e) {
        synchronized (this) {
            int hash = e.hash;
            int index = hash % table.length;
            Entry curr = table[index];
            Entry prev = null;
            while (curr != e) {
                prev = curr;
                curr = curr.next;
            }
            if (curr == null) return; 
            if (prev == null)
                table[index] = curr.next;
            else
                prev.next = curr.next;
            count--;
        }
    }
    protected class Entry extends CleanerThread.WeakReferenceCleared {
      public int hash;
      public int locationLine;
      public int locationColumn;
      public Entry next;
        public Entry(int hash,
                     Element element,
                     int locationLine,
                     int locationColumn,
                     Entry next) {
            super(element);
            this.hash           = hash;
            this.locationLine   = locationLine;
            this.locationColumn = locationColumn;
            this.next           = next;
        }
        public void cleared() {
            removeEntry(this);
        }
    }
}
