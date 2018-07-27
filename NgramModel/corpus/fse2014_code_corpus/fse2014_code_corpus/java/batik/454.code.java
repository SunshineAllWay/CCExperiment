package org.apache.batik.dom;
import java.io.Serializable;
import org.apache.batik.dom.events.DOMMutationEvent;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.MutationEvent;
public abstract class AbstractParentNode extends AbstractNode {
    protected ChildNodes childNodes;
    public NodeList getChildNodes() {
        return (childNodes == null)
            ? childNodes = new ChildNodes()
            : childNodes;
    }
    public Node getFirstChild() {
        return (childNodes == null) ? null : childNodes.firstChild;
    }
    public Node getLastChild() {
        return (childNodes == null) ? null : childNodes.lastChild;
    }
    public Node insertBefore(Node newChild, Node refChild)
        throws DOMException {
        if ((refChild != null) && ((childNodes == null) ||
                                   (refChild.getParentNode() != this)))
            throw createDOMException
                (DOMException.NOT_FOUND_ERR,
                 "child.missing",
                 new Object[] { new Integer(refChild.getNodeType()),
                                refChild.getNodeName() });
        checkAndRemove(newChild, false);
        if (newChild.getNodeType() == DOCUMENT_FRAGMENT_NODE) {
            Node n = newChild.getFirstChild();
            while (n != null) {
                Node ns = n.getNextSibling();
                insertBefore(n, refChild);
                n = ns;
            }
            return newChild;
        } else {
            if (childNodes == null) {
                childNodes = new ChildNodes();
            }
            ExtendedNode n = childNodes.insert((ExtendedNode)newChild,
                                               (ExtendedNode)refChild);
            n.setParentNode(this);
            nodeAdded(n);
            fireDOMNodeInsertedEvent(n);
            fireDOMSubtreeModifiedEvent();
            return n;
        }
    }
    public Node replaceChild(Node newChild, Node oldChild)
        throws DOMException {
        if ((childNodes == null) || (oldChild.getParentNode() != this) )
            throw createDOMException
                (DOMException.NOT_FOUND_ERR,
                 "child.missing",
                 new Object[] { new Integer(oldChild.getNodeType()),
                                oldChild.getNodeName() });
        checkAndRemove(newChild, true);
        if (newChild.getNodeType() == DOCUMENT_FRAGMENT_NODE) {
            Node n  = newChild.getLastChild();
            if (n == null)
                return newChild;
            Node ps = n.getPreviousSibling();
            replaceChild(n, oldChild);
            Node ns = n;
            n  = ps;
            while (n != null) {
                ps = n.getPreviousSibling();
                insertBefore(n, ns);
                ns = n;
                n = ps;
            }
            return newChild;
        }
        fireDOMNodeRemovedEvent(oldChild);
        getCurrentDocument().nodeToBeRemoved(oldChild);
        nodeToBeRemoved(oldChild);
        ExtendedNode n = (ExtendedNode)newChild;
        ExtendedNode o = childNodes.replace(n, (ExtendedNode)oldChild);
        n.setParentNode(this);
        o.setParentNode(null);
        nodeAdded(n);
        fireDOMNodeInsertedEvent(n);
        fireDOMSubtreeModifiedEvent();
        return n;
    }
    public Node removeChild(Node oldChild) throws DOMException {
        if (childNodes == null || oldChild.getParentNode() != this) {
            throw createDOMException
                (DOMException.NOT_FOUND_ERR,
                 "child.missing",
                 new Object[] { new Integer(oldChild.getNodeType()),
                                oldChild.getNodeName() });
        }
        if (isReadonly()) {
            throw createDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "readonly.node",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName() });
        }
        fireDOMNodeRemovedEvent(oldChild);
        getCurrentDocument().nodeToBeRemoved(oldChild);
        nodeToBeRemoved(oldChild);
        ExtendedNode result = childNodes.remove((ExtendedNode)oldChild);
        result.setParentNode(null);
        fireDOMSubtreeModifiedEvent();
        return result;
    }
    public Node appendChild(Node newChild) throws DOMException {
        checkAndRemove(newChild, false);
        if (newChild.getNodeType() == DOCUMENT_FRAGMENT_NODE) {
            Node n = newChild.getFirstChild();
            while (n != null) {
                Node ns = n.getNextSibling();
                appendChild(n);
                n = ns;
            }
            return newChild;
        } else {
            if (childNodes == null)
                childNodes = new ChildNodes();
            ExtendedNode n = childNodes.append((ExtendedNode)newChild);
            n.setParentNode(this);
            nodeAdded(n);
            fireDOMNodeInsertedEvent(n);
            fireDOMSubtreeModifiedEvent();
            return n;
        }
    }
    public boolean hasChildNodes() {
        return childNodes != null && childNodes.getLength() != 0;
    }
    public void normalize() {
        Node p = getFirstChild();
        if (p != null) {
            p.normalize();
            Node n = p.getNextSibling();
            while (n != null) {
                if (p.getNodeType() == TEXT_NODE &&
                    n.getNodeType() == TEXT_NODE) {
                    String s = p.getNodeValue() + n.getNodeValue();
                    AbstractText at = (AbstractText)p;
                    at.setNodeValue(s);
                    removeChild(n);
                    n = p.getNextSibling();
                } else {
                    n.normalize();
                    p = n;
                    n = n.getNextSibling();
                }
            }
        }
    }
    public NodeList getElementsByTagName(String name) {
        if (name == null) {
            return EMPTY_NODE_LIST;
        }
        AbstractDocument ad = getCurrentDocument();
        ElementsByTagName result = ad.getElementsByTagName(this, name);
        if (result == null) {
            result = new ElementsByTagName(name);
            ad.putElementsByTagName(this, name, result);
        }
        return result;
    }
    public NodeList getElementsByTagNameNS(String namespaceURI,
                                           String localName) {
        if (localName == null) {
            return EMPTY_NODE_LIST;
        }
        if (namespaceURI != null && namespaceURI.length() == 0) {
            namespaceURI = null;
        }
        AbstractDocument ad = getCurrentDocument();
        ElementsByTagNameNS result =
            ad.getElementsByTagNameNS(this, namespaceURI,
                                      localName);
        if (result == null) {
            result = new ElementsByTagNameNS(namespaceURI, localName);
            ad.putElementsByTagNameNS(this, namespaceURI, localName, result);
        }
        return result;
    }
    public String getTextContent() {
        StringBuffer sb = new StringBuffer();
        for (Node n = getFirstChild(); n != null; n = n.getNextSibling()) {
            switch (n.getNodeType()) {
                case COMMENT_NODE:
                case PROCESSING_INSTRUCTION_NODE:
                    break;
                default:
                    sb.append(((AbstractNode) n).getTextContent());
            }
        }
        return sb.toString();
    }
    public void fireDOMNodeInsertedIntoDocumentEvent() {
        AbstractDocument doc = getCurrentDocument();
        if (doc.getEventsEnabled()) {
            super.fireDOMNodeInsertedIntoDocumentEvent();
            for (Node n = getFirstChild(); n != null; n = n.getNextSibling()) {
                ((AbstractNode)n).fireDOMNodeInsertedIntoDocumentEvent();
            }
        }
    }
    public void fireDOMNodeRemovedFromDocumentEvent() {
        AbstractDocument doc = getCurrentDocument();
        if (doc.getEventsEnabled()) {
            super.fireDOMNodeRemovedFromDocumentEvent();
            for (Node n = getFirstChild(); n != null; n = n.getNextSibling()) {
                ((AbstractNode)n).fireDOMNodeRemovedFromDocumentEvent();
            }
        }
    }
    protected void nodeAdded(Node n) {
    }
    protected void nodeToBeRemoved(Node n) {
    }
    protected Node deepExport(Node n, AbstractDocument d) {
        super.deepExport(n, d);
        for (Node p = getFirstChild(); p != null; p = p.getNextSibling()) {
            Node t = ((AbstractNode)p).deepExport(p.cloneNode(false), d);
            n.appendChild(t);
        }
        return n;
    }
    protected Node deepCopyInto(Node n) {
        super.deepCopyInto(n);
        for (Node p = getFirstChild(); p != null; p = p.getNextSibling()) {
            Node t = p.cloneNode(true);
            n.appendChild(t);
        }
        return n;
    }
    protected void fireDOMSubtreeModifiedEvent() {
        AbstractDocument doc = getCurrentDocument();
        if (doc.getEventsEnabled()) {
            DOMMutationEvent ev
                = (DOMMutationEvent) doc.createEvent("MutationEvents");
            ev.initMutationEventNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                                   "DOMSubtreeModified",
                                   true,   
                                   false,  
                                   null,   
                                   null,   
                                   null,   
                                   null,   
                                   MutationEvent.MODIFICATION);
            dispatchEvent(ev);
        }
    }
    protected void fireDOMNodeInsertedEvent(Node node) {
        AbstractDocument doc = getCurrentDocument();
        if (doc.getEventsEnabled()) {
            DOMMutationEvent ev
                = (DOMMutationEvent) doc.createEvent("MutationEvents");
            ev.initMutationEventNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                                   "DOMNodeInserted",
                                   true,   
                                   false,  
                                   this,   
                                   null,   
                                   null,   
                                   null,   
                                   MutationEvent.ADDITION);
            AbstractNode n = (AbstractNode)node;
            n.dispatchEvent(ev);
            n.fireDOMNodeInsertedIntoDocumentEvent();
        }
    }
    protected void fireDOMNodeRemovedEvent(Node node) {
        AbstractDocument doc = getCurrentDocument();
        if (doc.getEventsEnabled()) {
            DOMMutationEvent ev
                = (DOMMutationEvent) doc.createEvent("MutationEvents");
            ev.initMutationEventNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                                   "DOMNodeRemoved",
                                   true,   
                                   false,  
                                   this,   
                                   null,   
                                   null,   
                                   null,   
                                   MutationEvent.REMOVAL);
            AbstractNode n = (AbstractNode)node;
            n.dispatchEvent(ev);
            n.fireDOMNodeRemovedFromDocumentEvent();
        }
    }
    protected void checkAndRemove(Node n, boolean replace) {
        checkChildType(n, replace);
        if (isReadonly())
            throw createDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "readonly.node",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName() });
        if (n.getOwnerDocument() != getCurrentDocument())
            throw createDOMException(DOMException.WRONG_DOCUMENT_ERR,
                                     "node.from.wrong.document",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName() });
        if (this == n)
            throw createDOMException
                (DOMException.HIERARCHY_REQUEST_ERR,
                 "add.self", new Object[] { getNodeName() });
        Node np = n.getParentNode();
        if (np == null)
            return;  
        for (Node pn = this; pn != null; pn = pn.getParentNode()) {
            if (pn == n)
                throw createDOMException
                    (DOMException.HIERARCHY_REQUEST_ERR,
                     "add.ancestor",
                     new Object[] { new Integer(getNodeType()),
                                    getNodeName() });
        }
        np.removeChild(n);
    }
    protected class ElementsByTagName implements NodeList {
        protected Node[] table;
        protected int size = -1;
        protected String name;
        public ElementsByTagName(String n) {
            name = n;
        }
        public Node item(int index) {
            if (size == -1) {
                initialize();
            }
            if (table == null || index < 0 || index >= size) {
                return null;
            }
            return table[index];
        }
        public int getLength() {
            if (size == -1) {
                initialize();
            }
            return size;
        }
        public void invalidate() {
            size = -1;
        }
        protected void append(Node n) {
            if (table == null) {
                table = new Node[11];
            } else if (size == table.length - 1) {
                Node[] t = new Node[table.length * 2 + 1];
                System.arraycopy( table, 0, t, 0, size );
                table = t;
            }
            table[size++] = n;
        }
        protected void initialize() {
            size = 0;
            for (Node n = AbstractParentNode.this.getFirstChild();
                 n != null;
                 n = n.getNextSibling()) {
                initialize(n);
            }
        }
        private void initialize(Node node) {
            if (node.getNodeType() == ELEMENT_NODE) {
                String nm = node.getNodeName();
                if (name.equals("*") || name.equals(nm)) {
                    append(node);
                }
            }
            for (Node n = node.getFirstChild();
                 n != null;
                 n = n.getNextSibling()) {
                initialize(n);
            }
        }
    }
    protected class ElementsByTagNameNS implements NodeList {
        protected Node[] table;
        protected int size = -1;
        protected String namespaceURI;
        protected String localName;
        public ElementsByTagNameNS(String ns, String ln) {
            namespaceURI = ns;
            localName = ln;
        }
        public Node item(int index) {
            if (size == -1) {
                initialize();
            }
            if (table == null || index < 0 || index > size) {
                return null;
            }
            return table[index];
        }
        public int getLength() {
            if (size == -1) {
                initialize();
            }
            return size;
        }
        public void invalidate() {
            size = -1;
        }
        protected void append(Node n) {
            if (table == null) {
                table = new Node[11];
            } else if (size == table.length - 1) {
                Node[] t = new Node[table.length * 2 + 1];
                System.arraycopy( table, 0, t, 0, size );
                table = t;
            }
            table[size++] = n;
        }
        protected void initialize() {
            size = 0;
            for (Node n = AbstractParentNode.this.getFirstChild();
                 n != null;
                 n = n.getNextSibling()) {
                initialize(n);
            }
        }
        private void initialize(Node node) {
            if (node.getNodeType() == ELEMENT_NODE) {
                String ns = node.getNamespaceURI();
                String nm = (ns == null)
                    ? node.getNodeName()
                    : node.getLocalName();
                if (nsMatch(namespaceURI, node.getNamespaceURI()) &&
                    (localName.equals("*") || localName.equals(nm))) {
                    append(node);
                }
            }
            for (Node n = node.getFirstChild();
                 n != null;
                 n = n.getNextSibling()) {
                initialize(n);
            }
        }
        private boolean nsMatch(String s1, String s2) {
            if (s1 == null && s2 == null) {
                return true;
            }
            if (s1 == null || s2 == null) {
                return false;
            }
            if (s1.equals("*")) {
                return true;
            }
            return s1.equals(s2);
        }
    }
    protected class ChildNodes implements NodeList, Serializable {
        protected ExtendedNode firstChild;
        protected ExtendedNode lastChild;
        protected int children;
        protected int elementChildren;
        public ChildNodes() {
        }
        public Node item(int index) {
            if (index < 0 || index >= children) {
                return null;
            }
            if (index < (children >> 1)) {
                Node n = firstChild;
                for (int i = 0; i < index; i++) {
                    n = n.getNextSibling();
                }
                return n;
            } else {
                Node n = lastChild;
                for (int i = children - 1; i > index; i--) {
                    n = n.getPreviousSibling();
                }
                return n;
            }
        }
        public int getLength() {
            return children;
        }
        public ExtendedNode append(ExtendedNode n) {
            if (lastChild == null) {
                firstChild = n;
            } else {
              lastChild.setNextSibling(n);
              n.setPreviousSibling(lastChild);
            }
            lastChild = n;
            children++;
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                elementChildren++;
            }
            return n;
        }
        public ExtendedNode insert(ExtendedNode n, ExtendedNode r) {
            if (r == null) {
                return append(n);
            }
            if (r == firstChild) {
                firstChild.setPreviousSibling(n);
                n.setNextSibling(firstChild);
                firstChild = n;
                children++;
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    elementChildren++;
                }
                return n;
            }
            if (r == lastChild) {
                ExtendedNode ps = (ExtendedNode)r.getPreviousSibling();
                ps.setNextSibling(n);
                r.setPreviousSibling(n);
                n.setNextSibling(r);
                n.setPreviousSibling(ps);
                children++;
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    elementChildren++;
                }
                return n;
            }
            ExtendedNode ps = (ExtendedNode)r.getPreviousSibling();
            if ((ps.getNextSibling() == r) &&
                (ps.getParentNode() == r.getParentNode())) {
                ps.setNextSibling(n);
                n.setPreviousSibling(ps);
                n.setNextSibling(r);
                r.setPreviousSibling(n);
                children++;
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    elementChildren++;
                }
                return n;
            }
            throw createDOMException
                (DOMException.NOT_FOUND_ERR,
                 "child.missing",
                 new Object[] { new Integer(r.getNodeType()),
                                r.getNodeName() });
        }
        public ExtendedNode replace(ExtendedNode n, ExtendedNode o) {
            if (o == firstChild) {
                ExtendedNode t = (ExtendedNode)firstChild.getNextSibling();
                n.setNextSibling(t);
                if (o == lastChild) {
                    lastChild = n;
                } else {
                    t.setPreviousSibling(n);
                }
                firstChild.setNextSibling(null);
                firstChild = n;
                if (o.getNodeType() == Node.ELEMENT_NODE) {
                    elementChildren--;
                }
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    elementChildren++;
                }
                return o;
            }
            if (o == lastChild) {
                ExtendedNode t = (ExtendedNode)lastChild.getPreviousSibling();
                n.setPreviousSibling(t);
                t.setNextSibling(n);
                lastChild.setPreviousSibling(null);
                lastChild = n;
                if (o.getNodeType() == Node.ELEMENT_NODE) {
                    elementChildren--;
                }
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    elementChildren++;
                }
                return o;
            }
            ExtendedNode ps = (ExtendedNode)o.getPreviousSibling();
            ExtendedNode ns = (ExtendedNode)o.getNextSibling();
            if ((ps.getNextSibling()     == o) &&
                (ns.getPreviousSibling() == o) &&
                (ps.getParentNode()      == o.getParentNode()) &&
                (ns.getParentNode()      == o.getParentNode())) {
                ps.setNextSibling(n);
                n.setPreviousSibling(ps);
                n.setNextSibling(ns);
                ns.setPreviousSibling(n);
                o.setPreviousSibling(null);
                o.setNextSibling(null);
                if (o.getNodeType() == Node.ELEMENT_NODE) {
                    elementChildren--;
                }
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    elementChildren++;
                }
                return o;
            }
            throw createDOMException
                (DOMException.NOT_FOUND_ERR,
                 "child.missing",
                 new Object[] { new Integer(o.getNodeType()),
                                o.getNodeName() });
        }
        public ExtendedNode remove(ExtendedNode n) {
            if (n == firstChild) {
                if (n == lastChild) {
                    firstChild = null;
                    lastChild  = null;
                    children--;
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        elementChildren--;
                    }
                    return n;
                }
                firstChild = (ExtendedNode)firstChild.getNextSibling();
                firstChild.setPreviousSibling(null);
                n.setNextSibling(null);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    elementChildren--;
                }
                children--;
                return n;
            }
            if (n == lastChild) {
                lastChild = (ExtendedNode)lastChild.getPreviousSibling();
                lastChild.setNextSibling(null);
                n.setPreviousSibling(null);
                children--;
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    elementChildren--;
                }
                return n;
            }
            ExtendedNode ps = (ExtendedNode)n.getPreviousSibling();
            ExtendedNode ns = (ExtendedNode)n.getNextSibling();
            if ((ps.getNextSibling()     == n) &&
                (ns.getPreviousSibling() == n) &&
                (ps.getParentNode()      == n.getParentNode()) &&
                (ns.getParentNode()      == n.getParentNode())) {
                ps.setNextSibling(ns);
                ns.setPreviousSibling(ps);
                n.setPreviousSibling(null);
                n.setNextSibling(null);
                children--;
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    elementChildren--;
                }
                return n;
            }
            throw createDOMException
                (DOMException.NOT_FOUND_ERR,
                 "child.missing",
                 new Object[] { new Integer(n.getNodeType()),
                                n.getNodeName() });
        }
    }
}
