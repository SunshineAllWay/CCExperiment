package org.apache.xerces.dom3.as;
public interface ASObject {
    public static final short AS_ELEMENT_DECLARATION    = 1;
    public static final short AS_ATTRIBUTE_DECLARATION  = 2;
    public static final short AS_NOTATION_DECLARATION   = 3;
    public static final short AS_ENTITY_DECLARATION     = 4;
    public static final short AS_CONTENTMODEL           = 5;
    public static final short AS_MODEL                  = 6;
    public short getAsNodeType();
    public ASModel getOwnerASModel();
    public void setOwnerASModel(ASModel ownerASModel);
    public String getNodeName();
    public void setNodeName(String nodeName);
    public String getPrefix();
    public void setPrefix(String prefix);
    public String getLocalName();
    public void setLocalName(String localName);
    public String getNamespaceURI();
    public void setNamespaceURI(String namespaceURI);
    public ASObject cloneASObject(boolean deep);
}
