package org.apache.batik.dom;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.XMLSupport;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
public abstract class AbstractAttrNS extends AbstractAttr {
    protected String namespaceURI;
    protected AbstractAttrNS() {
    }
    protected AbstractAttrNS(String nsURI,
                             String qname,
                             AbstractDocument owner)
        throws DOMException {
        super(qname, owner);
        if (nsURI != null && nsURI.length() == 0) {
            nsURI = null;
        }
        namespaceURI = nsURI;
        String prefix = DOMUtilities.getPrefix(qname);
        if (!owner.getStrictErrorChecking()) {
            return;
        }
        if (prefix != null) {
            if (nsURI == null ||
                ("xml".equals(prefix) &&
                 !XMLSupport.XML_NAMESPACE_URI.equals(nsURI)) ||
                ("xmlns".equals(prefix) &&
                 !XMLSupport.XMLNS_NAMESPACE_URI.equals(nsURI))) {
                throw createDOMException
                    (DOMException.NAMESPACE_ERR,
                     "namespace.uri",
                     new Object[] { new Integer(getNodeType()),
                                    getNodeName(),
                                    nsURI });
            }
        } else if ("xmlns".equals(qname) &&
                   !XMLSupport.XMLNS_NAMESPACE_URI.equals(nsURI)) {
            throw createDOMException(DOMException.NAMESPACE_ERR,
                                     "namespace.uri",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName(),
                                                    nsURI });
        }
    }
    public String getNamespaceURI() {
        return namespaceURI;
    }
    protected Node export(Node n, AbstractDocument d) {
        super.export(n, d);
        AbstractAttrNS aa = (AbstractAttrNS)n;
        aa.namespaceURI = namespaceURI;
        return n;
    }
    protected Node deepExport(Node n, AbstractDocument d) {
        super.deepExport(n, d);
        AbstractAttrNS aa = (AbstractAttrNS)n;
        aa.namespaceURI = namespaceURI;
        return n;
    }
    protected Node copyInto(Node n) {
        super.copyInto(n);
        AbstractAttrNS aa = (AbstractAttrNS)n;
        aa.namespaceURI = namespaceURI;
        return n;
    }
    protected Node deepCopyInto(Node n) {
        super.deepCopyInto(n);
        AbstractAttrNS aa = (AbstractAttrNS)n;
        aa.namespaceURI = namespaceURI;
        return n;
    }
}
