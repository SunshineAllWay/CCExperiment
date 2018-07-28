package org.apache.batik.dom.svg12;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.IdContainer;
import org.apache.batik.dom.xbl.XBLShadowTreeElement;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
public class XBLOMShadowTreeElement
        extends XBLOMElement
        implements XBLShadowTreeElement, IdContainer {
    protected XBLOMShadowTreeElement() {
    }
    public XBLOMShadowTreeElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return XBL_SHADOW_TREE_TAG;
    }
    protected Node newNode() {
        return new XBLOMShadowTreeElement();
    }
    public Element getElementById(String elementId) {
        return getElementById(elementId, this);
    }
    protected Element getElementById(String elementId, Node n) {
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element) n;
            if (e.getAttributeNS(null, "id").equals(elementId)) {
                return (Element) n;
            }
        }
        for (Node m = n.getFirstChild(); m != null; m = m.getNextSibling()) {
            Element result = getElementById(elementId, m);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    public Node getCSSParentNode() {
        return ownerDocument.getXBLManager().getXblBoundElement(this);
    }
}
