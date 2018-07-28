package org.apache.batik.dom;
import org.w3c.dom.Node;
public abstract class AbstractChildNode extends AbstractNode {
    protected Node parentNode;
    protected Node previousSibling;
    protected Node nextSibling;
    public Node getParentNode() {
        return parentNode;
    }
    public void setParentNode(Node v) {
        parentNode = v;
    }
    public void setPreviousSibling(Node v) {
        previousSibling = v;
    }
    public Node getPreviousSibling() {
        return previousSibling;
    }
    public void setNextSibling(Node v) {
        nextSibling = v;
    }
    public Node getNextSibling() {
        return nextSibling;
    }
}
