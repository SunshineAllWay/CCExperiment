package org.apache.batik.dom.svg12;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
public class XBLOMHandlerGroupElement extends XBLOMElement {
    protected XBLOMHandlerGroupElement() {
    }
    public XBLOMHandlerGroupElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return XBL_HANDLER_GROUP_TAG;
    }
    protected Node newNode() {
        return new XBLOMHandlerGroupElement();
    }
}
