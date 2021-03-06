package org.apache.xerces.stax.events;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.Namespace;
abstract class ElementImpl extends XMLEventImpl {
    private final QName fName;
    private final List fNamespaces;
    ElementImpl(final QName name, final boolean isStartElement, Iterator namespaces, final Location location) {
        super(isStartElement ? START_ELEMENT : END_ELEMENT, location);
        fName = name;
        if (namespaces != null && namespaces.hasNext()) {
            fNamespaces = new ArrayList();
            do {
                Namespace ns = (Namespace) namespaces.next();
                fNamespaces.add(ns);
            }
            while (namespaces.hasNext());
        }
        else {
            fNamespaces = Collections.EMPTY_LIST;
        }
    }
    public final QName getName() {
        return fName;
    }
    public final Iterator getNamespaces() {
        return createImmutableIterator(fNamespaces.iterator());
    }
    static Iterator createImmutableIterator(Iterator iter) {
        return new NoRemoveIterator(iter);
    }
    private static final class NoRemoveIterator implements Iterator {
        private final Iterator fWrapped;
        public NoRemoveIterator(Iterator wrapped) {
            fWrapped = wrapped;
        }
        public boolean hasNext() {
            return fWrapped.hasNext();
        }
        public Object next() {
            return fWrapped.next();
        }
        public void remove() {
            throw new UnsupportedOperationException("Attributes iterator is read-only.");
        }
    }
}
