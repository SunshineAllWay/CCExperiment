package org.apache.xerces.xs;
public interface XSObject {
    public short getType();
    public String getName();
    public String getNamespace();
    public XSNamespaceItem getNamespaceItem();
}
