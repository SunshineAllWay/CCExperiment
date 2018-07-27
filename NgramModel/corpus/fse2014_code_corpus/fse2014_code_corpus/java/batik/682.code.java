package org.apache.batik.dom.svg12;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.DOMUtilities;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
public class XBLOMDefinitionElement extends XBLOMElement {
    protected XBLOMDefinitionElement() {
    }
    public XBLOMDefinitionElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return XBL_DEFINITION_TAG;
    }
    protected Node newNode() {
        return new XBLOMDefinitionElement();
    }
    public String getElementNamespaceURI() {
        String qname = getAttributeNS(null, XBL_ELEMENT_ATTRIBUTE);
        String prefix = DOMUtilities.getPrefix(qname);
        String ns = lookupNamespaceURI(prefix);
        if (ns == null) {
            throw createDOMException
                        (DOMException.NAMESPACE_ERR,
                         "prefix",
                         new Object[] { new Integer(getNodeType()),
                                        getNodeName(),
                                        prefix });
        }
        return ns;
    }
    public String getElementLocalName() {
        String qname = getAttributeNS(null, "element");
        return DOMUtilities.getLocalName(qname);
    }
}
