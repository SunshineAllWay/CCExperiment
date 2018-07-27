package org.apache.tools.ant.util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;
public class CollectionUtils {
    public static final List EMPTY_LIST =
        Collections.unmodifiableList(new ArrayList(0));
    public static boolean equals(Vector v1, Vector v2) {
        if (v1 == v2) {
            return true;
        }
        if (v1 == null || v2 == null) {
            return false;
        }
        return v1.equals(v2);
    }
    public static boolean equals(Dictionary d1, Dictionary d2) {
        if (d1 == d2) {
            return true;
        }
        if (d1 == null || d2 == null) {
            return false;
        }
        if (d1.size() != d2.size()) {
            return false;
        }
        Enumeration e1 = d1.keys();
        while (e1.hasMoreElements()) {
            Object key = e1.nextElement();
            Object value1 = d1.get(key);
            Object value2 = d2.get(key);
            if (value2 == null || !value1.equals(value2)) {
                return false;
            }
        }
        return true;
    }
    public static String flattenToString(Collection c) {
        Iterator iter = c.iterator();
        boolean first = true;
        StringBuffer sb = new StringBuffer();
        while (iter.hasNext()) {
            if (!first) {
                sb.append(",");
            }
            sb.append(String.valueOf(iter.next()));
            first = false;
        }
        return sb.toString();
    }
    public static void putAll(Dictionary m1, Dictionary m2) {
        for (Enumeration it = m2.keys(); it.hasMoreElements();) {
            Object key = it.nextElement();
            m1.put(key, m2.get(key));
        }
    }
    public static final class EmptyEnumeration implements Enumeration {
        public EmptyEnumeration() {
        }
        public boolean hasMoreElements() {
            return false;
        }
        public Object nextElement() throws NoSuchElementException {
            throw new NoSuchElementException();
        }
    }
    public static Enumeration append(Enumeration e1, Enumeration e2) {
        return new CompoundEnumeration(e1, e2);
    }
    public static Enumeration asEnumeration(final Iterator iter) {
        return new Enumeration() {
            public boolean hasMoreElements() {
                return iter.hasNext();
            }
            public Object nextElement() {
                return iter.next();
            }
        };
    }
    public static Iterator asIterator(final Enumeration e) {
        return new Iterator() {
            public boolean hasNext() {
                return e.hasMoreElements();
            }
            public Object next() {
                return e.nextElement();
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    public static Collection asCollection(final Iterator iter) {
        List l = new ArrayList();
        while (iter.hasNext()) {
            l.add(iter.next());
        }
        return l;
    }
    private static final class CompoundEnumeration implements Enumeration {
        private final Enumeration e1, e2;
        public CompoundEnumeration(Enumeration e1, Enumeration e2) {
            this.e1 = e1;
            this.e2 = e2;
        }
        public boolean hasMoreElements() {
            return e1.hasMoreElements() || e2.hasMoreElements();
        }
        public Object nextElement() throws NoSuchElementException {
            if (e1.hasMoreElements()) {
                return e1.nextElement();
            } else {
                return e2.nextElement();
            }
        }
    }
    public static int frequency(Collection c, Object o) {
        int freq = 0;
        if (c != null) {
            for (Iterator i = c.iterator(); i.hasNext(); ) {
                Object test = i.next();
                if (o == null ? test == null : o.equals(test)) {
                    freq++;
                }
            }
        }
        return freq;
    }
}
