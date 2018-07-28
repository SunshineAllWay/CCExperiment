package org.apache.batik.dom.svg12;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.util.XBLConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
public abstract class XBLOMElement extends SVGOMElement
                                   implements XBLConstants {
    protected String prefix;
    protected XBLOMElement() {
    }
    protected XBLOMElement(String prefix, AbstractDocument owner) {
        ownerDocument = owner;
        setPrefix(prefix);
    }
    public String getNodeName() {
        if (prefix == null || prefix.equals("")) {
            return getLocalName();
        }
        return prefix + ':' + getLocalName();
    }
    public String getNamespaceURI() {
        return XBL_NAMESPACE_URI;
    }
    public void setPrefix(String prefix) throws DOMException {
        if (isReadonly()) {
            throw createDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "readonly.node",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName() });
        }
        if (prefix != null &&
            !prefix.equals("") &&
            !DOMUtilities.isValidName(prefix)) {
            throw createDOMException(DOMException.INVALID_CHARACTER_ERR,
                                     "prefix",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName(),
                                                    prefix });
        }
        this.prefix = prefix;
    }
    protected Node export(Node n, AbstractDocument d) {
        super.export(n, d);
        XBLOMElement e = (XBLOMElement)n;
        e.prefix = prefix;
        return n;
    }
    protected Node deepExport(Node n, AbstractDocument d) {
        super.deepExport(n, d);
        XBLOMElement e = (XBLOMElement)n;
        e.prefix = prefix;
        return n;
    }
    protected Node copyInto(Node n) {
        super.copyInto(n);
        XBLOMElement e = (XBLOMElement)n;
        e.prefix = prefix;
        return n;
    }
    protected Node deepCopyInto(Node n) {
        super.deepCopyInto(n);
        XBLOMElement e = (XBLOMElement)n;
        e.prefix = prefix;
        return n;
    }
}
