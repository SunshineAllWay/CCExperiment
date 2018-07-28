package org.apache.xerces.dom3.as;
import org.w3c.dom.DOMException;
public interface ASNamedObjectMap {
    public int getLength();
    public ASObject getNamedItem(String name);
    public ASObject getNamedItemNS(String namespaceURI, 
                                   String localName);
    public ASObject item(int index);
    public ASObject removeNamedItem(String name)
                                    throws DOMException;
    public ASObject removeNamedItemNS(String namespaceURI, 
                                      String localName)
                                      throws DOMException;
    public ASObject setNamedItem(ASObject newASObject)
                                 throws DOMException;
    public ASObject setNamedItemNS(ASObject newASObject)
                                   throws DOMException;
}
