package org.apache.batik.dom;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.XMLSupport;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
public abstract class AbstractElementNS extends AbstractElement {
    protected String namespaceURI;
    protected AbstractElementNS() {
    }
    protected AbstractElementNS(String nsURI, String qname,
                                AbstractDocument owner)
        throws DOMException {
        super(qname, owner);
        if (nsURI != null && nsURI.length() == 0) {
            nsURI = null;
        }
        namespaceURI = nsURI;
        String prefix = DOMUtilities.getPrefix(qname);
        if (prefix != null) {
            if (nsURI == null ||
                ("xml".equals(prefix) &&
                 !XMLSupport.XML_NAMESPACE_URI.equals(nsURI))) {
                throw createDOMException
                    (DOMException.NAMESPACE_ERR,
                     "namespace.uri",
                     new Object[] { new Integer(getNodeType()),
                                    getNodeName(),
                                    nsURI });
            }
        }
    }
    public String getNamespaceURI() {
        return namespaceURI;
    }
    protected Node export(Node n, AbstractDocument d) {
        super.export(n, d);
        AbstractElementNS ae = (AbstractElementNS)n;
        ae.namespaceURI = namespaceURI;
        return n;
    }
    protected Node deepExport(Node n, AbstractDocument d) {
        super.deepExport(n, d);
        AbstractElementNS ae = (AbstractElementNS)n;
        ae.namespaceURI = namespaceURI;
        return n;
    }
    protected Node copyInto(Node n) {
        super.copyInto(n);
        AbstractElementNS ae = (AbstractElementNS)n;
        ae.namespaceURI = namespaceURI;
        return n;
    }
    protected Node deepCopyInto(Node n) {
        super.deepCopyInto(n);
        AbstractElementNS ae = (AbstractElementNS)n;
        ae.namespaceURI = namespaceURI;
        return n;
    }
}
