package org.apache.xerces.dom;
import java.util.ArrayList;
import java.util.Vector;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMImplementationList;
public class DOMImplementationListImpl implements DOMImplementationList {
    private final ArrayList fImplementations;
    public DOMImplementationListImpl() {
        fImplementations = new ArrayList();
    }
    public DOMImplementationListImpl(ArrayList params) {
        fImplementations = params;    
    }
    public DOMImplementationListImpl(Vector params) {
        fImplementations = new ArrayList(params);
    }
    public DOMImplementation item(int index) {
        final int length = getLength();
        if (index >= 0 && index < length) {
            return (DOMImplementation) fImplementations.get(index);
        }
        return null;
    }
    public int getLength() {
        return fImplementations.size();
    }
}
