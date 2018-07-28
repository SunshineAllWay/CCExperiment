package org.apache.batik.dom.events;
import org.apache.batik.dom.util.IntTable;
import org.apache.batik.dom.util.HashTable;
import org.w3c.dom.events.EventListener;
public class EventListenerList {
    protected int n;
    protected Entry head;
    protected IntTable counts = new IntTable();
    protected Entry[] listeners;
    protected HashTable listenersNS = new HashTable();
    public void addListener(String namespaceURI,
                            Object group,
                            EventListener listener) {
        for (Entry e = head; e != null; e = e.next) {
            if ((namespaceURI != null && namespaceURI.equals(e.namespaceURI)
                        || namespaceURI == null && e.namespaceURI == null)
                    && e.listener == listener) {
                return;
            }
        }
        head = new Entry(listener, namespaceURI, group, head);
        counts.inc(namespaceURI);
        n++;
        listeners = null;
        listenersNS.remove(namespaceURI);
    }
    public void removeListener(String namespaceURI,
                               EventListener listener) {
        if (head == null) {
            return;
        } else if (head != null
                && (namespaceURI != null && namespaceURI.equals(head.namespaceURI)
                    || namespaceURI == null && head.namespaceURI == null)
                && listener == head.listener) {
            head = head.next;
        } else {
            Entry e;
            Entry prev = head;
            for (e = head.next; e != null; e = e.next) {
                if ((namespaceURI != null && namespaceURI.equals(e.namespaceURI)
                            || namespaceURI == null && e.namespaceURI == null)
                        && e.listener == listener) {
                    prev.next = e.next;
                    break;
                }
                prev = e;
            }
            if (e == null) {
                return;
            }
        }
        counts.dec(namespaceURI);
        n--;
        listeners = null;
        listenersNS.remove(namespaceURI);
    }
    public Entry[] getEventListeners() {
        if (listeners != null) {
            return listeners;
        }
        listeners = new Entry[n];
        int i = 0;
        for (Entry e = head; e != null; e = e.next) {
            listeners[i++] = e;
        }
        return listeners;
    }
    public Entry[] getEventListeners(String namespaceURI) {
        if (namespaceURI == null) {
            return getEventListeners();
        }
        Entry[] ls = (Entry[]) listenersNS.get(namespaceURI);
        if (ls != null) {
            return ls;
        }
        int count = counts.get(namespaceURI);
        if (count == 0) {
            return null;
        }
        ls = new Entry[count];
        listenersNS.put(namespaceURI, ls);
        int i = 0;
        for (Entry e = head; i < count; e = e.next) {
            if (namespaceURI.equals(e.namespaceURI)) {
                ls[i++] = e;
            }
        }
        return ls;
    }
    public boolean hasEventListener(String namespaceURI) {
        if (namespaceURI == null) {
            return n != 0;
        }
        return counts.get(namespaceURI) != 0;
    }
    public int size() {
        return n;
    }
    public class Entry {
        protected EventListener listener;
        protected String namespaceURI;
        protected Object group;
        protected boolean mark;
        protected Entry next;
        public Entry(EventListener listener,
                     String namespaceURI,
                     Object group,
                     Entry next) {
            this.listener = listener;
            this.namespaceURI = namespaceURI;
            this.group = group;
            this.next = next;
        }
        public EventListener getListener() {
            return listener;
        }
        public Object getGroup() {
            return group;
        }
        public String getNamespaceURI() {
            return namespaceURI;
        }
    }
}
