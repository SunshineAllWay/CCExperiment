package org.apache.tools.ant.util;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
public final class VectorSet extends Vector {
    private final HashSet set = new HashSet();
    public VectorSet() { super(); }
    public VectorSet(int initialCapacity) { super(initialCapacity); }
    public VectorSet(int initialCapacity, int capacityIncrement) {
        super(initialCapacity, capacityIncrement);
    }
    public VectorSet(Collection c) {
        if (c != null) {
            for (Iterator i = c.iterator(); i.hasNext(); ) {
                add(i.next());
            }
        }
    }
    public synchronized boolean add(Object o) {
        if (!set.contains(o)) {
            doAdd(size(), o);
            return true;
        }
        return false;
    }
    public void add(int index, Object o) {
        doAdd(index, o);
    }
    private synchronized void doAdd(int index, Object o) {
        if (set.add(o)) {
            int count = size();
            ensureCapacity(count + 1);
            if (index != count) {
                System.arraycopy(elementData, index, elementData, index + 1,
                                 count - index);
            }
            elementData[index] = o;
            elementCount++;
        }
    }
    public synchronized void addElement(Object o) {
        doAdd(size(), o);
    }
    public synchronized boolean addAll(Collection c) {
        boolean changed = false;
        for (Iterator i = c.iterator(); i.hasNext(); ) {
            changed |= add(i.next());
        }
        return changed;
    }
    public synchronized boolean addAll(int index, Collection c) {
        boolean changed = false;
        for (Iterator i = c.iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (!set.contains(o)) {
                doAdd(index++, o);
                changed = true;
            }
        }
        return changed;
    }
    public synchronized void clear() {
        super.clear();
        set.clear();
    }
    public Object clone() {
        VectorSet vs = (VectorSet) super.clone();
        vs.set.addAll(set);
        return vs;
    }
    public synchronized boolean contains(Object o) {
        return set.contains(o);
    }
    public synchronized boolean containsAll(Collection c) {
        return set.containsAll(c);
    }
    public void insertElementAt(Object o, int index) {
        doAdd(index, o);
    }
    public synchronized Object remove(int index) {
        Object o = get(index);
        remove(o);
        return o;
    }
    public boolean remove(Object o) {
        return doRemove(o);
    }
    private synchronized boolean doRemove(Object o) {
        if (set.remove(o)) {
            int index = indexOf(o);
            if (index < elementData.length - 1) {
                System.arraycopy(elementData, index + 1, elementData, index,
                                 elementData.length - index - 1);
            }
            elementCount--;
            return true;
        }
        return false;
    }
    public synchronized boolean removeAll(Collection c) {
        boolean changed = false;
        for (Iterator i = c.iterator(); i.hasNext(); ) {
            changed |= remove(i.next());
        }
        return changed;
    }
    public synchronized void removeAllElements() {
        set.clear();
        super.removeAllElements();
    }
    public boolean removeElement(Object o) {
        return doRemove(o);
    }
    public synchronized void removeElementAt(int index) {
        remove(get(index));
    }
    public synchronized void removeRange(final int fromIndex, int toIndex) {
        while (toIndex > fromIndex) {
            remove(--toIndex);
        }
    }
    public synchronized boolean retainAll(Collection c) {
        LinkedList l = new LinkedList();
        for (Iterator i = iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (!c.contains(o)) {
                l.addLast(o);
            }
        }
        if (!l.isEmpty()) {
            removeAll(l);
            return true;
        }
        return false;
    }
    public synchronized Object set(int index, Object o) {
        Object orig = get(index);
        if (set.add(o)) {
            elementData[index] = o;
            set.remove(orig);
        } else {
            int oldIndexOfO = indexOf(o);
            remove(o);
            remove(orig);
            add(oldIndexOfO > index ? index : index - 1, o);
        }
        return orig;
    }
    public void setElementAt(Object o, int index) {
        set(index, o);
    }
}