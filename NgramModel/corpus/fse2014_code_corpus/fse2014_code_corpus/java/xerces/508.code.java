package org.apache.xerces.impl.xs.util;
import org.apache.xerces.util.SymbolHash;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSTypeDefinition;
public final class XSNamedMap4Types extends XSNamedMapImpl {
    private final short fType;
    public XSNamedMap4Types(String namespace, SymbolHash map, short type) {
        super(namespace, map);
        fType = type;
    }
    public XSNamedMap4Types(String[] namespaces, SymbolHash[] maps, int num, short type) {
        super(namespaces, maps, num);
        fType = type;
    }
    public synchronized int getLength() {
        if (fLength == -1) {
            int length = 0;
            for (int i = 0; i < fNSNum; i++) {
                length += fMaps[i].getLength();
            }
            int pos = 0;
            XSObject[] array = new XSObject[length];
            for (int i = 0; i < fNSNum; i++) {
                pos += fMaps[i].getValues(array, pos);
            }
            fLength = 0;
            fArray = new XSObject[length];
            XSTypeDefinition type;
            for (int i = 0; i < length; i++) {
                type = (XSTypeDefinition)array[i];
                if (type.getTypeCategory() == fType) {
                    fArray[fLength++] = type;
                }
            }
        }
        return fLength;
    }
    public XSObject itemByName(String namespace, String localName) {
        for (int i = 0; i < fNSNum; i++) {
            if (isEqual(namespace, fNamespaces[i])) {
                XSTypeDefinition type = (XSTypeDefinition)fMaps[i].get(localName);
                if (type != null && type.getTypeCategory() == fType) {
                    return type;
                }
                return null;
            }
        }
        return null;
    }
    public synchronized XSObject item(int index) {
        if (fArray == null) {
            getLength();
        }
        if (index < 0 || index >= fLength) {
            return null;
        }
        return fArray[index];
    }
} 
