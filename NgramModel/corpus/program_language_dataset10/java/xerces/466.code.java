package org.apache.xerces.impl.xs.opti;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;
public class AttrImpl extends NodeImpl 
                      implements Attr {
    Element element;
    String value;
    public AttrImpl() {
        nodeType = Node.ATTRIBUTE_NODE;
    }
    public AttrImpl(Element element, String prefix, String localpart, String rawname, String uri, String value) {
        super(prefix, localpart, rawname, uri, Node.ATTRIBUTE_NODE);
        this.element = element;
        this.value = value;
    }
    public String getName() {
        return rawname;
    }
    public boolean getSpecified() {
        return true;
    }
    public String getValue() {
        return value;
    }
    public String getNodeValue() {
        return getValue();
    }
    public Element getOwnerElement() {
        return element;
    }
    public Document getOwnerDocument() {
        return element.getOwnerDocument();
    }
    public void setValue(String value) throws DOMException {
        this.value = value;
    }
    public boolean isId(){
        return false;
    }
    public TypeInfo getSchemaTypeInfo(){
      return null;
    }
    public String toString() {
        return getName() + "=" + "\"" + getValue() + "\"";
    }
}