package org.apache.batik.dom;
import java.io.Serializable;
import org.apache.batik.dom.events.DOMMutationEvent;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.ElementTraversal;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.events.MutationEvent;
public abstract class AbstractElement
    extends    AbstractParentChildNode
    implements Element, ElementTraversal {
    protected NamedNodeMap attributes;
    protected TypeInfo typeInfo;
    protected AbstractElement() {
    }
    protected AbstractElement(String name, AbstractDocument owner) {
        ownerDocument = owner;
        if (owner.getStrictErrorChecking() && !DOMUtilities.isValidName(name)) {
            throw createDOMException(DOMException.INVALID_CHARACTER_ERR,
                   "xml.name",
                   new Object[] { name });
        }
    }
    public short getNodeType() {
        return ELEMENT_NODE;
    }
    public boolean hasAttributes() {
        return attributes != null && attributes.getLength() != 0;
    }
    public NamedNodeMap getAttributes() {
        return (attributes == null)
            ? attributes = createAttributes()
            : attributes;
    }
    public String getTagName() {
        return getNodeName();
    }
    public boolean hasAttribute( String name ) {
        return attributes != null && attributes.getNamedItem( name ) != null;
    }
    public String getAttribute(String name) {
        if ( attributes == null ) {
          return "";
        }
        Attr attr = (Attr)attributes.getNamedItem( name );
        return ( attr == null ) ? "" : attr.getValue();
    }
    public void setAttribute(String name, String value) throws DOMException {
        if (attributes == null) {
            attributes = createAttributes();
        }
        Attr attr = getAttributeNode(name);
        if (attr == null) {
            attr = getOwnerDocument().createAttribute(name);
            attr.setValue(value);
            attributes.setNamedItem(attr);
        } else {
            attr.setValue(value);
        }
    }
    public void removeAttribute(String name) throws DOMException {
        if (!hasAttribute(name)) {
                  return;
        }
        attributes.removeNamedItem(name);
    }
    public Attr getAttributeNode(String name) {
        if (attributes == null) {
            return null;
        }
        return (Attr)attributes.getNamedItem(name);
    }
    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        if (newAttr == null) {
            return null;
        }
        if (attributes == null) {
            attributes = createAttributes();
        }
        return (Attr)attributes.setNamedItemNS(newAttr);
    }
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        if (oldAttr == null) {
            return null;
        }
        if (attributes == null) {
            throw createDOMException(DOMException.NOT_FOUND_ERR,
                   "attribute.missing",
                   new Object[] { oldAttr.getName() });
        }
        String nsURI = oldAttr.getNamespaceURI();
        return (Attr)attributes.removeNamedItemNS(nsURI,
                                                  (nsURI==null
                                                   ? oldAttr.getNodeName()
                                                   : oldAttr.getLocalName()));
    }
    public void normalize() {
        super.normalize();
        if (attributes != null) {
            NamedNodeMap map = getAttributes();
            for (int i = map.getLength() - 1; i >= 0; i--) {
                map.item(i).normalize();
            }
        }
    }
    public boolean hasAttributeNS( String namespaceURI, String localName ) {
        if ( namespaceURI != null && namespaceURI.length() == 0 ) {
            namespaceURI = null;
        }
        return attributes != null &&
                attributes.getNamedItemNS( namespaceURI, localName ) != null;
    }
    public String getAttributeNS( String namespaceURI, String localName ) {
        if ( attributes == null ) {
            return "";
        }
        if ( namespaceURI != null && namespaceURI.length() == 0 ) {
            namespaceURI = null;
        }
        Attr attr = (Attr)attributes.getNamedItemNS( namespaceURI, localName );
        return ( attr == null ) ? "" : attr.getValue();
    }
    public void setAttributeNS(String namespaceURI,
                               String qualifiedName,
                               String value) throws DOMException {
        if (attributes == null) {
            attributes = createAttributes();
        }
        if (namespaceURI != null && namespaceURI.length() == 0) {
            namespaceURI = null;
        }
        Attr attr = getAttributeNodeNS(namespaceURI, qualifiedName);
        if (attr == null) {
            attr = getOwnerDocument().createAttributeNS(namespaceURI,
                                                        qualifiedName);
            attr.setValue(value);
            attributes.setNamedItemNS(attr);
        } else {
            attr.setValue(value);
        }
    }
    public void removeAttributeNS(String namespaceURI,
                                  String localName) throws DOMException {
        if (namespaceURI != null && namespaceURI.length() == 0) {
            namespaceURI = null;
        }
        if (!hasAttributeNS(namespaceURI, localName)) {
                  return;
        }
        attributes.removeNamedItemNS(namespaceURI, localName);
    }
    public Attr getAttributeNodeNS(String namespaceURI,
                                   String localName) {
        if (namespaceURI != null && namespaceURI.length() == 0) {
            namespaceURI = null;
        }
        if (attributes == null) {
            return null;
        }
        return (Attr)attributes.getNamedItemNS(namespaceURI, localName);
    }
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        if (newAttr == null) {
            return null;
        }
        if (attributes == null) {
            attributes = createAttributes();
        }
        return (Attr)attributes.setNamedItemNS(newAttr);
    }
    public TypeInfo getSchemaTypeInfo() {
        if (typeInfo == null) {
            typeInfo = new ElementTypeInfo();
        }
        return typeInfo;
    }
    public void setIdAttribute(String name, boolean isId) throws DOMException {
        AbstractAttr a = (AbstractAttr) getAttributeNode(name);
        if (a == null) {
            throw createDOMException(DOMException.NOT_FOUND_ERR,
                                     "attribute.missing",
                                     new Object[] { name });
        }
        if (a.isReadonly()) {
            throw createDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "readonly.node",
                                     new Object[] { name });
        }
        a.isIdAttr = isId;
    }
    public void setIdAttributeNS( String ns, String ln, boolean isId )
            throws DOMException {
        if ( ns != null && ns.length() == 0 ) {
            ns = null;
        }
        AbstractAttr a = (AbstractAttr)getAttributeNodeNS( ns, ln );
        if (a == null) {
            throw createDOMException(DOMException.NOT_FOUND_ERR,
                                     "attribute.missing",
                                     new Object[] { ns, ln });
        }
        if (a.isReadonly()) {
            throw createDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "readonly.node",
                                     new Object[] { a.getNodeName() });
        }
        a.isIdAttr = isId;
    }
    public void setIdAttributeNode( Attr attr, boolean isId )
            throws DOMException {
        AbstractAttr a = (AbstractAttr)attr;
        if (a.isReadonly()) {
            throw createDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "readonly.node",
                                     new Object[] { a.getNodeName() });
        }
        a.isIdAttr = isId;
    }
    protected Attr getIdAttribute() {
        NamedNodeMap nnm = getAttributes();
        if ( nnm == null ) {
            return null;
        }
        int len = nnm.getLength();
        for (int i = 0; i < len; i++) {
            AbstractAttr a = (AbstractAttr)nnm.item(i);
            if (a.isId()) {
                return a;
            }
        }
        return null;
    }
    protected String getId() {
        Attr a = getIdAttribute();
        if (a != null) {
            String id = a.getNodeValue();
            if (id.length() > 0) {
                return id;
            }
        }
        return null;
    }
    protected void nodeAdded(Node node) {
        invalidateElementsByTagName(node);
    }
    protected void nodeToBeRemoved(Node node) {
        invalidateElementsByTagName(node);
    }
    private void invalidateElementsByTagName(Node node) {
        if (node.getNodeType() != ELEMENT_NODE) {
            return;
        }
        AbstractDocument ad = getCurrentDocument();
        String ns = node.getNamespaceURI();
        String nm = node.getNodeName();
        String ln = (ns == null) ? node.getNodeName() : node.getLocalName();
        for (Node n = this; n != null; n = n.getParentNode()) {
            switch (n.getNodeType()) {
            case ELEMENT_NODE:      
            case DOCUMENT_NODE:
                ElementsByTagName l = ad.getElementsByTagName(n, nm);
                if (l != null) {
                    l.invalidate();
                }
                l = ad.getElementsByTagName(n, "*");
                if (l != null) {
                    l.invalidate();
                }
                ElementsByTagNameNS lns = ad.getElementsByTagNameNS(n, ns, ln);
                if (lns != null) {
                    lns.invalidate();
                }
                lns = ad.getElementsByTagNameNS(n, "*", ln);
                if (lns != null) {
                    lns.invalidate();
                }
                lns = ad.getElementsByTagNameNS(n, ns, "*");
                if (lns != null) {
                    lns.invalidate();
                }
                lns = ad.getElementsByTagNameNS(n, "*", "*");
                if (lns != null) {
                    lns.invalidate();
                }
            }
        }
        Node c = node.getFirstChild();
        while (c != null) {
            invalidateElementsByTagName(c);
            c = c.getNextSibling();
        }
    }
    protected NamedNodeMap createAttributes() {
        return new NamedNodeHashMap();
    }
    protected Node export(Node n, AbstractDocument d) {
        super.export(n, d);
        AbstractElement ae = (AbstractElement)n;
        if (attributes != null) {
            NamedNodeMap map = attributes;
            for (int i = map.getLength() - 1; i >= 0; i--) {
                AbstractAttr aa = (AbstractAttr)map.item(i);
                if (aa.getSpecified()) {
                    Attr attr = (Attr)aa.deepExport(aa.cloneNode(false), d);
                    if (aa instanceof AbstractAttrNS) {
                        ae.setAttributeNodeNS(attr);
                    } else {
                        ae.setAttributeNode(attr);
                    }
                }
            }
        }
        return n;
    }
    protected Node deepExport(Node n, AbstractDocument d) {
        super.deepExport(n, d);
        AbstractElement ae = (AbstractElement)n;
        if (attributes != null) {
            NamedNodeMap map = attributes;
            for (int i = map.getLength() - 1; i >= 0; i--) {
                AbstractAttr aa = (AbstractAttr)map.item(i);
                if (aa.getSpecified()) {
                    Attr attr = (Attr)aa.deepExport(aa.cloneNode(false), d);
                    if (aa instanceof AbstractAttrNS) {
                        ae.setAttributeNodeNS(attr);
                    } else {
                        ae.setAttributeNode(attr);
                    }
                }
            }
        }
        return n;
    }
    protected Node copyInto(Node n) {
        super.copyInto(n);
        AbstractElement ae = (AbstractElement)n;
        if (attributes != null) {
            NamedNodeMap map = attributes;
            for (int i = map.getLength() - 1; i >= 0; i--) {
                AbstractAttr aa = (AbstractAttr)map.item(i).cloneNode(true);
                if (aa instanceof AbstractAttrNS) {
                    ae.setAttributeNodeNS(aa);
                } else {
                    ae.setAttributeNode(aa);
                }
            }
        }
        return n;
    }
    protected Node deepCopyInto(Node n) {
        super.deepCopyInto(n);
        AbstractElement ae = (AbstractElement)n;
        if (attributes != null) {
            NamedNodeMap map = attributes;
            for (int i = map.getLength() - 1; i >= 0; i--) {
                AbstractAttr aa = (AbstractAttr)map.item(i).cloneNode(true);
                if (aa instanceof AbstractAttrNS) {
                    ae.setAttributeNodeNS(aa);
                } else {
                    ae.setAttributeNode(aa);
                }
            }
        }
        return n;
    }
    protected void checkChildType(Node n, boolean replace) {
        switch (n.getNodeType()) {
        case ELEMENT_NODE:                
        case PROCESSING_INSTRUCTION_NODE:
        case COMMENT_NODE:
        case TEXT_NODE:
        case CDATA_SECTION_NODE:
        case ENTITY_REFERENCE_NODE:
        case DOCUMENT_FRAGMENT_NODE:
            break;
        default:
            throw createDOMException
                      (DOMException.HIERARCHY_REQUEST_ERR,
                       "child.type",
                       new Object[] { new Integer(getNodeType()),
                                      getNodeName(),
                                      new Integer(n.getNodeType()),
                                      n.getNodeName() });
        }
    }
    public void fireDOMAttrModifiedEvent(String name, Attr node, String oldv,
                                         String newv, short change) {
        switch (change) {
        case MutationEvent.ADDITION:
            if (((AbstractAttr)node).isId())
                ownerDocument.addIdEntry(this, newv);
            attrAdded(node, newv);
            break;
        case MutationEvent.MODIFICATION:
            if (((AbstractAttr)node).isId())
                ownerDocument.updateIdEntry(this, oldv, newv);
            attrModified(node, oldv, newv);
            break;
        default: 
            if (((AbstractAttr)node).isId())
                ownerDocument.removeIdEntry(this, oldv);
            attrRemoved(node, oldv);
        }
        AbstractDocument doc = getCurrentDocument();
        if (doc.getEventsEnabled() && !oldv.equals(newv)) {
            DOMMutationEvent ev
                      = (DOMMutationEvent) doc.createEvent("MutationEvents");
            ev.initMutationEventNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                                         "DOMAttrModified",
                                         true,    
                                         false,   
                                         node,    
                                         oldv,    
                                         newv,    
                                         name,    
                                         change); 
            dispatchEvent(ev);
        }
    }
    protected void attrAdded(Attr node, String newv) {
    }
    protected void attrModified(Attr node, String oldv, String newv) {
    }
    protected void attrRemoved(Attr node, String oldv) {
    }
    public Element getFirstElementChild() {
        Node n = getFirstChild();
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) n;
            }
            n = n.getNextSibling();
        }
        return null;
    }
    public Element getLastElementChild() {
        Node n = getLastChild();
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) n;
            }
            n = n.getPreviousSibling();
        }
        return null;
    }
    public Element getNextElementSibling() {
        Node n = getNextSibling();
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) n;
            }
            n = n.getNextSibling();
        }
        return null;
    }
    public Element getPreviousElementSibling() {
        Node n = getPreviousSibling();
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) n;
            }
            n = n.getPreviousSibling();
        }
        return (Element) n;
    }
    public int getChildElementCount() {
        getChildNodes();
        return childNodes.elementChildren;
    }
    public class NamedNodeHashMap implements NamedNodeMap, Serializable {
        protected static final int INITIAL_CAPACITY = 3;
        protected Entry[] table;
        protected int count;
        public NamedNodeHashMap() {
                  table = new Entry[INITIAL_CAPACITY];
        }
        public Node getNamedItem( String name ) {
            if ( name == null ) {
                return null;
            }
            return get( null, name );
        }
        public Node setNamedItem( Node arg ) throws DOMException {
            if ( arg == null ) {
                return null;
            }
            checkNode( arg );
            return setNamedItem( null, arg.getNodeName(), arg );
        }
        public Node removeNamedItem( String name ) throws DOMException {
            return removeNamedItemNS( null, name );
        }
        public Node item( int index ) {
            if ( index < 0 || index >= count ) {
                return null;
            }
            int j = 0;
            for ( int i = 0; i < table.length; i++ ) {
                Entry e = table[ i ];
                if ( e == null ) {
                    continue;
                }
                do {
                    if ( j++ == index ) {
                        return e.value;
                    }
                    e = e.next;
                } while ( e != null );
            }
            return null;
        }
        public int getLength() {
            return count;
        }
        public Node getNamedItemNS( String namespaceURI, String localName ) {
            if ( namespaceURI != null && namespaceURI.length() == 0 ) {
                namespaceURI = null;
            }
            return get( namespaceURI, localName );
        }
        public Node setNamedItemNS( Node arg ) throws DOMException {
            if ( arg == null ) {
                return null;
            }
            String nsURI = arg.getNamespaceURI();
            return setNamedItem( nsURI,
                    ( nsURI == null )
                            ? arg.getNodeName()
                            : arg.getLocalName(), arg );
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
            if ( namespaceURI != null && namespaceURI.length() == 0 ) {
                namespaceURI = null;
            }
            AbstractAttr n = (AbstractAttr)remove( namespaceURI, localName );
            if ( n == null ) {
                throw createDOMException( DOMException.NOT_FOUND_ERR,
                        "attribute.missing",
                        new Object[]{localName} );
            }
            n.setOwnerElement( null );
            fireDOMAttrModifiedEvent( n.getNodeName(), n, n.getNodeValue(), "",
                    MutationEvent.REMOVAL );
            return n;
        }
        public Node setNamedItem( String ns, String name, Node arg )
                throws DOMException {
            if ( ns != null && ns.length() == 0 ) {
                ns = null;
            }
            ( (AbstractAttr)arg ).setOwnerElement( AbstractElement.this );
            AbstractAttr result = (AbstractAttr)put( ns, name, arg );
            if ( result != null ) {
                result.setOwnerElement( null );
                fireDOMAttrModifiedEvent( name,
                        result,
                        result.getNodeValue(),
                        "",
                        MutationEvent.REMOVAL );
            }
            fireDOMAttrModifiedEvent( name,
                    (Attr)arg,
                    "",
                    arg.getNodeValue(),
                    MutationEvent.ADDITION );
            return result;
    }
        protected void checkNode( Node arg ) {
            if ( isReadonly() ) {
                throw createDOMException
                        ( DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                "readonly.node.map",
                                new Object[]{} );
            }
            if ( getOwnerDocument() != arg.getOwnerDocument() ) {
                throw createDOMException( DOMException.WRONG_DOCUMENT_ERR,
                        "node.from.wrong.document",
                        new Object[]{new Integer( arg.getNodeType() ),
                                arg.getNodeName()} );
            }
            if ( arg.getNodeType() == ATTRIBUTE_NODE &&
                    ( (Attr)arg ).getOwnerElement() != null ) {
                throw createDOMException( DOMException.WRONG_DOCUMENT_ERR,
                        "inuse.attribute",
                        new Object[]{arg.getNodeName()} );
            }
        }
        protected Node get( String ns, String nm ) {
            int hash = hashCode( ns, nm ) & 0x7FFFFFFF;
            int index = hash % table.length;
            for ( Entry e = table[ index ]; e != null; e = e.next ) {
                if ( ( e.hash == hash ) && e.match( ns, nm ) ) {
                    return e.value;
                }
            }
            return null;
        }
        protected Node put( String ns, String nm, Node value ) {
            int hash = hashCode( ns, nm ) & 0x7FFFFFFF;
            int index = hash % table.length;
            for ( Entry e = table[ index ]; e != null; e = e.next ) {
                if ( ( e.hash == hash ) && e.match( ns, nm ) ) {
                    Node old = e.value;
                    e.value = value;
                    return old;
                }
            }
            int len = table.length;
            if ( count++ >= ( len - ( len >> 2 ) ) ) {
                rehash();
                index = hash % table.length;
            }
            Entry e = new Entry( hash, ns, nm, value, table[ index ] );
            table[ index ] = e;
            return null;
        }
        protected Node remove( String ns, String nm ) {
            int hash = hashCode( ns, nm ) & 0x7FFFFFFF;
            int index = hash % table.length;
            Entry p = null;
            for ( Entry e = table[ index ]; e != null; e = e.next ) {
                if ( ( e.hash == hash ) && e.match( ns, nm ) ) {
                    Node result = e.value;
                    if ( p == null ) {
                        table[ index ] = e.next;
                    } else {
                        p.next = e.next;
                    }
                    count--;
                    return result;
                }
                p = e;
            }
            return null;
        }
        protected void rehash () {
            Entry[] oldTable = table;
            table = new Entry[oldTable.length * 2 + 1];
            for (int i = oldTable.length-1; i >= 0; i--) {
                for (Entry old = oldTable[i]; old != null;) {
                    Entry e = old;
                    old = old.next;
                    int index = e.hash % table.length;
                    e.next = table[index];
                    table[index] = e;
                }
            }
        }
        protected int hashCode(String ns, String nm) {
            int result = (ns == null) ? 0 : ns.hashCode();
            return result ^ nm.hashCode();
        }
    }
    protected static class Entry implements Serializable {
        public int hash;       
        public String namespaceURI;
        public String name;
        public Node value;
        public Entry next;
        public Entry(int hash, String ns, String nm, Node value, Entry next) {
            this.hash = hash;
            this.namespaceURI = ns;
            this.name = nm;
            this.value = value;
            this.next = next;
        }
        public boolean match(String ns, String nm) {
            if (namespaceURI != null) {
                if (!namespaceURI.equals(ns)) {
                    return false;
                }
            } else if (ns != null) {
                return false;
            }
            return name.equals(nm);
        }
    }
    public class ElementTypeInfo implements TypeInfo {
        public String getTypeNamespace() {
            return null;
        }
        public String getTypeName() {
            return null;
        }
        public boolean isDerivedFrom(String ns, String name, int method) {
            return false;
        }
    }
}
