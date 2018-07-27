package org.apache.batik.dom.svg12;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
public class XBLOMContentElement extends XBLOMElement {
    protected XBLOMContentElement() {
    }
    public XBLOMContentElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return XBL_CONTENT_TAG;
    }
    protected Node newNode() {
        return new XBLOMContentElement();
    }
}
