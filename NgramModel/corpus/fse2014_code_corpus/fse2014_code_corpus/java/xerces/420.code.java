package org.apache.xerces.impl.xs;
import java.util.AbstractList;
import org.apache.xerces.xs.StringList;
final class PSVIErrorList extends AbstractList implements StringList {
    private final String[] fArray;
    private final int fLength;
    private final int fOffset;
    public PSVIErrorList(String[] array, boolean even) {
        fArray = array;
        fLength = (fArray.length >> 1);
        fOffset = even ? 0 : 1;
    }
    public boolean contains(String item) {
        if (item == null) {
            for (int i = 0; i < fLength; ++i) {
                if (fArray[(i << 1) + fOffset] == null) {
                    return true;
                }
            }
        }
        else {
            for (int i = 0; i < fLength; ++i) {
                if (item.equals(fArray[(i << 1) + fOffset])) {
                    return true;
                }
            }
        }
        return false;
    }
    public int getLength() {
        return fLength;
    }
    public String item(int index) {
        if (index < 0 || index >= fLength) {
            return null;
        }
        return fArray[(index << 1) + fOffset];
    }
    public Object get(int index) {
        if (index >= 0 && index < fLength) {
            return fArray[(index << 1) + fOffset];
        }
        throw new IndexOutOfBoundsException("Index: " + index);
    }
    public int size() {
        return getLength();
    }
} 
