package org.apache.xerces.impl.xs.util;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.apache.xerces.util.SymbolHash;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObject;
public class XSNamedMapImpl extends AbstractMap implements XSNamedMap {
    public static final XSNamedMapImpl EMPTY_MAP = new XSNamedMapImpl(new XSObject[0], 0);
    final String[] fNamespaces;
    final int fNSNum;
    final SymbolHash[] fMaps;
    XSObject[] fArray = null;
    int fLength = -1;
    private Set fEntrySet = null;
    public XSNamedMapImpl(String namespace, SymbolHash map) {
        fNamespaces = new String[] {namespace};
        fMaps = new SymbolHash[] {map};
        fNSNum = 1;
    }
    public XSNamedMapImpl(String[] namespaces, SymbolHash[] maps, int num) {
        fNamespaces = namespaces;
        fMaps = maps;
        fNSNum = num;
    }
    public XSNamedMapImpl(XSObject[] array, int length) {
        if (length == 0) {
            fNamespaces = null;
            fMaps = null;
            fNSNum = 0;
            fArray = array;
            fLength = 0;
            return;
        }
        fNamespaces = new String[]{array[0].getNamespace()};
        fMaps = null;
        fNSNum = 1;
        fArray = array;
        fLength = length;
    }
    public synchronized int getLength() {
        if (fLength == -1) {
            fLength = 0;
            for (int i = 0; i < fNSNum; i++) {
                fLength += fMaps[i].getLength();
            }
        }
        return fLength;
    }
    public XSObject itemByName(String namespace, String localName) {
        for (int i = 0; i < fNSNum; i++) {
            if (isEqual(namespace, fNamespaces[i])) {
                if (fMaps != null) {
                    return (XSObject)fMaps[i].get(localName);
                }
                XSObject ret;
                for (int j = 0; j < fLength; j++) {
                    ret = fArray[j];
                    if (ret.getName().equals(localName)) {
                        return ret;
                    }
                }
                return null;
            }
        }
        return null;
    }
    public synchronized XSObject item(int index) {
        if (fArray == null) {
            getLength();
            fArray = new XSObject[fLength];
            int pos = 0;
            for (int i = 0; i < fNSNum; i++) {
                pos += fMaps[i].getValues(fArray, pos);
            }
        }
        if (index < 0 || index >= fLength) {
            return null;
        }
        return fArray[index];
    }
    static boolean isEqual(String one, String two) {
        return (one != null) ? one.equals(two) : (two == null);
    }
    public boolean containsKey(Object key) {
        return (get(key) != null);
    }
    public Object get(Object key) {
        if (key instanceof QName) {
            final QName name = (QName) key;
            String namespaceURI = name.getNamespaceURI();
            if (XMLConstants.NULL_NS_URI.equals(namespaceURI)) {
                namespaceURI = null;
            }
            String localPart = name.getLocalPart();
            return itemByName(namespaceURI, localPart);
        }
        return null;
    }
    public int size() {
        return getLength();
    }
    public synchronized Set entrySet() {
        if (fEntrySet == null) {
            final int length = getLength();
            final XSNamedMapEntry[] entries = new XSNamedMapEntry[length];
            for (int i = 0; i < length; ++i) {
                XSObject xso = item(i);
                entries[i] = new XSNamedMapEntry(new QName(xso.getNamespace(), xso.getName()), xso);
            }
            fEntrySet = new AbstractSet() {
                public Iterator iterator() {
                    return new Iterator() {
                        private int index = 0;
                        public boolean hasNext() {
                            return (index < length);
                        }
                        public Object next() {
                            if (index < length) {
                                return entries[index++];
                            }
                            throw new NoSuchElementException();
                        }
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                public int size() {
                    return length;
                }
            };
        }
        return fEntrySet;
    }
    private static final class XSNamedMapEntry implements Map.Entry {
        private final QName key;
        private final XSObject value;
        public XSNamedMapEntry(QName key, XSObject value) {
            this.key = key;
            this.value = value;
        }
        public Object getKey() {
            return key;
        }
        public Object getValue() {
            return value;
        }
        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }
        public boolean equals(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry e = (Map.Entry) o;
                Object otherKey = e.getKey();
                Object otherValue = e.getValue();
                return (key == null ? otherKey == null : key.equals(otherKey)) &&
                    (value == null ? otherValue == null : value.equals(otherValue));
            }
            return false;
        }
        public int hashCode() {
            return (key == null ? 0 : key.hashCode()) 
                ^ (value == null ? 0 : value.hashCode());
        }
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(String.valueOf(key));
            buffer.append('=');
            buffer.append(String.valueOf(value));
            return buffer.toString();
        }
    }
} 
