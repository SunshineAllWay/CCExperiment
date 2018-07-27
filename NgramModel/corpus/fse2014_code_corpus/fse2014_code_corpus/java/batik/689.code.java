package org.apache.batik.dom.svg12;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
public class XBLOMXBLElement extends XBLOMElement {
    protected XBLOMXBLElement() {
    }
    public XBLOMXBLElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return XBL_XBL_TAG;
    }
    protected Node newNode() {
        return new XBLOMXBLElement();
    }
}
