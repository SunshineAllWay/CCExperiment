package org.apache.xerces.impl.xs.util;
import java.lang.reflect.Array;
import java.util.AbstractList;
import org.apache.xerces.xs.datatypes.ObjectList;
public final class ObjectListImpl extends AbstractList implements ObjectList {
    public static final ObjectListImpl EMPTY_LIST = new ObjectListImpl(new Object[0], 0);
    private final Object[] fArray;
    private final int fLength;
    public ObjectListImpl(Object[] array, int length) {
        fArray = array;
        fLength = length;
    }
    public int getLength() {
        return fLength;
    }
    public boolean contains(Object item) {
        if (item == null) {
            for (int i = 0; i < fLength; i++) {
                if (fArray[i] == null)
                    return true;
            }
        }
        else {
            for (int i = 0; i < fLength; i++) {
                if (item.equals(fArray[i]))
                    return true;
            }
        }
        return false;
    }
    public Object item(int index) {
        if (index < 0 || index >= fLength) {
            return null;
        }
        return fArray[index];
    }
    public Object get(int index) {
        if (index >= 0 && index < fLength) {
            return fArray[index];
        }
        throw new IndexOutOfBoundsException("Index: " + index);
    }
    public int size() {
        return getLength();
    }
    public Object[] toArray() {
        Object[] a = new Object[fLength];
        toArray0(a);
        return a;
    }
    public Object[] toArray(Object[] a) {
        if (a.length < fLength) {
            Class arrayClass = a.getClass();
            Class componentType = arrayClass.getComponentType();
            a = (Object[]) Array.newInstance(componentType, fLength);
        }
        toArray0(a);
        if (a.length > fLength) {
            a[fLength] = null;
        }
        return a;
    }
    private void toArray0(Object[] a) {
        if (fLength > 0) {
            System.arraycopy(fArray, 0, a, 0, fLength);
        }
    }
}
