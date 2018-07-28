package org.apache.batik.dom.svg;
import org.apache.batik.css.engine.CSSNavigableNode;
import org.apache.batik.dom.AbstractAttr;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.events.MutationEvent;
public abstract class AbstractElement
        extends org.apache.batik.dom.AbstractElement
        implements NodeEventTarget, CSSNavigableNode, SVGConstants {
    protected transient DoublyIndexedTable liveAttributeValues =
        new DoublyIndexedTable();
    protected AbstractElement() {
    }
    protected AbstractElement(String prefix, AbstractDocument owner) {
        ownerDocument = owner;
        setPrefix(prefix);
        initializeAttributes();
    }
    public Node getCSSParentNode() {
        return getXblParentNode();
    }
    public Node getCSSPreviousSibling() {
        return getXblPreviousSibling();
    }
    public Node getCSSNextSibling() {
        return getXblNextSibling();
    }
    public Node getCSSFirstChild() {
        return getXblFirstChild();
    }
    public Node getCSSLastChild() {
        return getXblLastChild();
    }
    public boolean isHiddenFromSelectors() {
        return false;
    }
    public void fireDOMAttrModifiedEvent(String name, Attr node, String oldv,
                                         String newv, short change) {
        super.fireDOMAttrModifiedEvent(name, node, oldv, newv, change);
        if (((SVGOMDocument) ownerDocument).isSVG12
                && (change == MutationEvent.ADDITION
                    || change == MutationEvent.MODIFICATION)) {
            if (node.getNamespaceURI() == null
                    && node.getNodeName().equals(SVG_ID_ATTRIBUTE)) {
                Attr a =
                    getAttributeNodeNS(XML_NAMESPACE_URI, SVG_ID_ATTRIBUTE);
                if (a == null) {
                    setAttributeNS(XML_NAMESPACE_URI, XML_ID_QNAME, newv);
                } else if (!a.getNodeValue().equals(newv)) {
                    a.setNodeValue(newv);
                }
            } else if (node.getNodeName().equals(XML_ID_QNAME)) {
                Attr a = getAttributeNodeNS(null, SVG_ID_ATTRIBUTE);
                if (a == null) {
                    setAttributeNS(null, SVG_ID_ATTRIBUTE, newv);
                } else if (!a.getNodeValue().equals(newv)) {
                    a.setNodeValue(newv);
                }
            }
        }
    }
    public LiveAttributeValue getLiveAttributeValue(String ns, String ln) {
        return (LiveAttributeValue)liveAttributeValues.get(ns, ln);
    }
    public void putLiveAttributeValue(String ns, String ln,
                                      LiveAttributeValue val) {
        liveAttributeValues.put(ns, ln, val);
    }
    protected AttributeInitializer getAttributeInitializer() {
        return null;
    }
    protected void initializeAttributes() {
        AttributeInitializer ai = getAttributeInitializer();
        if (ai != null) {
            ai.initializeAttributes(this);
        }
    }
    protected boolean resetAttribute(String ns, String prefix, String ln) {
        AttributeInitializer ai = getAttributeInitializer();
        if (ai == null) {
            return false;
        }
        return ai.resetAttribute(this, ns, prefix, ln);
    }
    protected NamedNodeMap createAttributes() {
        return new ExtendedNamedNodeHashMap();
    }
    public void setUnspecifiedAttribute(String nsURI, String name,
                                        String value) {
        if (attributes == null) {
            attributes = createAttributes();
        }
        ((ExtendedNamedNodeHashMap)attributes).
            setUnspecifiedAttribute(nsURI, name, value);
    }
    protected void attrAdded(Attr node, String newv) {
        LiveAttributeValue lav = getLiveAttributeValue(node);
        if (lav != null) {
            lav.attrAdded(node, newv);
        }
    }
    protected void attrModified(Attr node, String oldv, String newv) {
        LiveAttributeValue lav = getLiveAttributeValue(node);
        if (lav != null) {
            lav.attrModified(node, oldv, newv);
        }
    }
    protected void attrRemoved(Attr node, String oldv) {
        LiveAttributeValue lav = getLiveAttributeValue(node);
        if (lav != null) {
            lav.attrRemoved(node, oldv);
        }
    }
    private LiveAttributeValue getLiveAttributeValue(Attr node) {
        String ns = node.getNamespaceURI();
        return getLiveAttributeValue(ns, (ns == null)
                                     ? node.getNodeName()
                                     : node.getLocalName());
    }
    protected Node export(Node n, AbstractDocument d) {
        super.export(n, d);
        ((AbstractElement)n).initializeAttributes();
        super.export(n, d);
        return n;
    }
    protected Node deepExport(Node n, AbstractDocument d) {
        super.export(n, d);
        ((AbstractElement)n).initializeAttributes();
        super.deepExport(n, d);
        return n;
    }
    protected class ExtendedNamedNodeHashMap extends NamedNodeHashMap {
        public ExtendedNamedNodeHashMap() {
        }
        public void setUnspecifiedAttribute( String nsURI, String name,
                                             String value ) {
            Attr attr = getOwnerDocument().createAttributeNS( nsURI, name );
            attr.setValue( value );
            ( (AbstractAttr)attr ).setSpecified( false );
            setNamedItemNS( attr );
        }
        public Node removeNamedItemNS( String namespaceURI, String localName )
                throws DOMException {
            if ( isReadonly() ) {
                throw createDOMException
                        ( DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                "readonly.node.map",
                                new Object[]{} );
            }
            if ( localName == null ) {
                throw createDOMException( DOMException.NOT_FOUND_ERR,
                        "attribute.missing",
                        new Object[]{""} );
            }
            AbstractAttr n = (AbstractAttr)remove( namespaceURI, localName );
            if ( n == null ) {
                throw createDOMException( DOMException.NOT_FOUND_ERR,
                        "attribute.missing",
                        new Object[]{localName} );
            }
            n.setOwnerElement( null );
            String prefix = n.getPrefix();
            if ( !resetAttribute( namespaceURI, prefix, localName ) ) {
                fireDOMAttrModifiedEvent( n.getNodeName(), n,
                        n.getNodeValue(), "",
                        MutationEvent.REMOVAL );
            }
            return n;
        }
    }
}
