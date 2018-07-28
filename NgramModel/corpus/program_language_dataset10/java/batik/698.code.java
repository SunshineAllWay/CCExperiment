package org.apache.batik.dom.util;
public class HashTableStack {
    protected Link current = new Link(null);
    public HashTableStack() {
    }
    public void push() {
        current.pushCount++;
    }
    public void pop() {
        if (current.pushCount-- == 0) {
            current = current.next;
        }
    }
    public String put(String s, String v) {
        if (current.pushCount != 0) {
            current.pushCount--;
            current = new Link(current);
        }
        if (s.length() == 0) current.defaultStr = v;
        return (String)current.table.put(s, v);
    }
    public String get(String s) {
        if (s.length() == 0) return current.defaultStr;
        for (Link l = current; l != null; l = l.next) {
            String uri = (String)l.table.get(s);
            if (uri != null) {
                return uri;
            }
        }
        return null;
    }
    protected static class Link {
        public HashTable table;
        public Link next;
        public String defaultStr;
        public int pushCount = 0;
        public Link(Link n) {
            table = new HashTable();
            next  = n;
            if (next != null) 
                defaultStr = next.defaultStr;
        }
    }
}
