package org.apache.batik.dom.traversal;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
public class DOMNodeIterator implements NodeIterator {
    protected static final short INITIAL = 0;
    protected static final short INVALID = 1;
    protected static final short FORWARD = 2;
    protected static final short BACKWARD = 3;
    protected AbstractDocument document;
    protected Node root;
    protected int whatToShow;
    protected NodeFilter filter;
    protected boolean expandEntityReferences;
    protected short state;
    protected Node referenceNode;
    public DOMNodeIterator(AbstractDocument doc, Node n, int what,
                           NodeFilter nf, boolean exp) {
        document = doc;
        root = n;
        whatToShow = what;
        filter = nf;
        expandEntityReferences = exp;
        referenceNode = root;
    }
    public Node getRoot() {
        return root;
    }
    public int getWhatToShow() {
        return whatToShow;
    }
    public NodeFilter getFilter() {
        return filter;
    }
    public boolean getExpandEntityReferences() {
        return expandEntityReferences;
    }
    public Node nextNode() {
        switch (state) {
        case INVALID:
            throw document.createDOMException
                (DOMException.INVALID_STATE_ERR,
                 "detached.iterator",  null);
        case BACKWARD:
        case INITIAL:
            state = FORWARD;
            return referenceNode;
        case FORWARD:
        }
        for (;;) {
            unfilteredNextNode();
            if (referenceNode == null) {
                return null;
            }
            if ((whatToShow & (1 << referenceNode.getNodeType() - 1)) != 0) {
                if (filter == null ||
                    filter.acceptNode(referenceNode) == NodeFilter.FILTER_ACCEPT) {
                    return referenceNode;
                }
            }
        }
    }
    public Node previousNode() {
        switch (state) {
        case INVALID:
            throw document.createDOMException
                (DOMException.INVALID_STATE_ERR,
                 "detached.iterator",  null);
        case FORWARD:
        case INITIAL:
            state = BACKWARD;
            return referenceNode;
        case BACKWARD:
        }
        for (;;) {
            unfilteredPreviousNode();
            if (referenceNode == null) {
                return referenceNode;
            }
            if ((whatToShow & (1 << referenceNode.getNodeType() - 1)) != 0) {
                if (filter == null ||
                    filter.acceptNode(referenceNode) == NodeFilter.FILTER_ACCEPT) {
                    return referenceNode;
                }
            }
        }
    }
    public void detach() {
        state = INVALID;
        document.detachNodeIterator(this);
    }
    public void nodeToBeRemoved(Node removedNode) {
        if (state == INVALID) {
            return;
        }
        Node node;
        for (node = referenceNode;
             node != null && node != root;
             node = node.getParentNode()) {
            if (node == removedNode) {
                break;
            }
        }
        if (node == null || node == root) {
            return;
        }
        if (state == BACKWARD) {
            if (node.getNodeType() != Node.ENTITY_REFERENCE_NODE ||
                expandEntityReferences) {
                Node n = node.getFirstChild();
                if (n != null) {
                    referenceNode = n;
                    return;
                }
            }
            Node n = node.getNextSibling();
            if (n != null) {
                referenceNode = n;
                return;
            }
            n = node;
            while ((n = n.getParentNode()) != null && n != root) {
                Node t = n.getNextSibling();
                if (t != null) {
                    referenceNode = t;
                    return;
                }
            }
            referenceNode = null;
        } else {
            Node n = node.getPreviousSibling();
            if (n == null) {
                referenceNode = node.getParentNode();
                return;
            }
            if (n.getNodeType() != Node.ENTITY_REFERENCE_NODE ||
                expandEntityReferences) {
                Node t;
                while ((t = n.getLastChild()) != null) {
                    n = t;
                }
            }
            referenceNode = n;
        }
    }
    protected void unfilteredNextNode() {
        if (referenceNode == null) {
            return;
        }
        if (referenceNode.getNodeType() != Node.ENTITY_REFERENCE_NODE ||
            expandEntityReferences) {
            Node n = referenceNode.getFirstChild();
            if (n != null) {
                referenceNode = n;
                return;
            }
        }
        Node n = referenceNode.getNextSibling();
        if (n != null) {
            referenceNode = n;
            return;
        }
        n = referenceNode;
        while ((n = n.getParentNode()) != null && n != root) {
            Node t = n.getNextSibling();
            if (t != null) {
                referenceNode = t;
                return;
            }
        }
        referenceNode = null;
    }
    protected void unfilteredPreviousNode() {
        if (referenceNode == null) {
            return;
        }
        if (referenceNode == root) {
            referenceNode = null;
            return;
        }
        Node n = referenceNode.getPreviousSibling();
        if (n == null) {
            referenceNode = referenceNode.getParentNode();
            return;
        }
        if (n.getNodeType() != Node.ENTITY_REFERENCE_NODE ||
            expandEntityReferences) {
            Node t;
            while ((t = n.getLastChild()) != null) {
                n = t;
            }
        }
        referenceNode = n;
    }
}
