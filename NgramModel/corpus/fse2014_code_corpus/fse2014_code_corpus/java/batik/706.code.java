package org.apache.batik.dom.xbl;
import org.apache.batik.dom.AbstractNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class GenericXBLManager implements XBLManager {
    protected boolean isProcessing;
    public void startProcessing() {
        isProcessing = true;
    }
    public void stopProcessing() {
        isProcessing = false;
    }
    public boolean isProcessing() {
        return isProcessing;
    }
    public Node getXblParentNode(Node n) {
        return n.getParentNode();
    }
    public NodeList getXblChildNodes(Node n) {
        return n.getChildNodes();
    }
    public NodeList getXblScopedChildNodes(Node n) {
        return n.getChildNodes();
    }
    public Node getXblFirstChild(Node n) {
        return n.getFirstChild();
    }
    public Node getXblLastChild(Node n) {
        return n.getLastChild();
    }
    public Node getXblPreviousSibling(Node n) {
        return n.getPreviousSibling();
    }
    public Node getXblNextSibling(Node n) {
        return n.getNextSibling();
    }
    public Element getXblFirstElementChild(Node n) {
        Node m = n.getFirstChild();
        while (m != null && m.getNodeType() != Node.ELEMENT_NODE) {
            m = m.getNextSibling();
        }
        return (Element) m;
    }
    public Element getXblLastElementChild(Node n) {
        Node m = n.getLastChild();
        while (m != null && m.getNodeType() != Node.ELEMENT_NODE) {
            m = m.getPreviousSibling();
        }
        return (Element) m;
    }
    public Element getXblPreviousElementSibling(Node n) {
        Node m = n;
        do {
            m = m.getPreviousSibling();
        } while (m != null && m.getNodeType() != Node.ELEMENT_NODE);
        return (Element) m;
    }
    public Element getXblNextElementSibling(Node n) {
        Node m = n;
        do {
            m = m.getNextSibling();
        } while (m != null && m.getNodeType() != Node.ELEMENT_NODE);
        return (Element) m;
    }
    public Element getXblBoundElement(Node n) {
        return null;
    }
    public Element getXblShadowTree(Node n) {
        return null;
    }
    public NodeList getXblDefinitions(Node n) {
        return AbstractNode.EMPTY_NODE_LIST;
    }
}
