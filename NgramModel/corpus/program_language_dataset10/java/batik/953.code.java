package org.apache.batik.gvt;
import java.util.List;
public class GVTTreeWalker {
    protected GraphicsNode gvtRoot;
    protected GraphicsNode treeRoot;
    protected GraphicsNode currentNode;
    public GVTTreeWalker(GraphicsNode treeRoot) {
        this.gvtRoot     = treeRoot.getRoot();
        this.treeRoot    = treeRoot;
        this.currentNode = treeRoot;
    }
    public GraphicsNode getRoot() {
        return treeRoot;
    }
    public GraphicsNode getGVTRoot() {
        return gvtRoot;
    }
    public void setCurrentGraphicsNode(GraphicsNode node) {
        if (node.getRoot() != gvtRoot) {
            throw new IllegalArgumentException
                ("The node "+node+" is not part of the document "+gvtRoot);
        }
        currentNode = node;
    }
    public GraphicsNode getCurrentGraphicsNode() {
        return currentNode;
    }
    public GraphicsNode previousGraphicsNode() {
        GraphicsNode result = getPreviousGraphicsNode(currentNode);
        if (result != null) {
            currentNode = result;
        }
        return result;
    }
    public GraphicsNode nextGraphicsNode() {
        GraphicsNode result = getNextGraphicsNode(currentNode);
        if (result != null) {
            currentNode = result;
        }
        return result;
    }
    public GraphicsNode parentGraphicsNode() {
        if (currentNode == treeRoot) return null;
        GraphicsNode result = currentNode.getParent();
        if (result != null) {
            currentNode = result;
        }
        return result;
    }
    public GraphicsNode getNextSibling() {
        GraphicsNode result = getNextSibling(currentNode);
        if (result != null) {
            currentNode = result;
        }
        return result;
    }
    public GraphicsNode getPreviousSibling() {
        GraphicsNode result = getPreviousSibling(currentNode);
        if (result != null) {
            currentNode = result;
        }
        return result;
    }
    public GraphicsNode firstChild() {
        GraphicsNode result = getFirstChild(currentNode);
        if (result != null) {
            currentNode = result;
        }
        return result;
    }
    public GraphicsNode lastChild() {
        GraphicsNode result = getLastChild(currentNode);
        if (result != null) {
            currentNode = result;
        }
        return result;
    }
    protected GraphicsNode getNextGraphicsNode(GraphicsNode node) {
        if (node == null) {
            return null;
        }
        GraphicsNode n = getFirstChild(node);
        if (n != null) {
            return n;
        }
        n = getNextSibling(node);
        if (n != null) {
            return n;
        }
        n = node;
        while ((n = n.getParent()) != null && n != treeRoot) {
            GraphicsNode t = getNextSibling(n);
            if (t != null) {
                return t;
            }
        }
        return null;
    }
    protected GraphicsNode getPreviousGraphicsNode(GraphicsNode node) {
        if (node == null) {
            return null;
        }
        if (node == treeRoot) {
            return null;
        }
        GraphicsNode n = getPreviousSibling(node);
        if (n == null) {
            return node.getParent();
        }
        GraphicsNode t;
        while ((t = getLastChild(n)) != null) {
            n = t;
        }
        return n;
    }
    protected static GraphicsNode getLastChild(GraphicsNode node) {
        if (!(node instanceof CompositeGraphicsNode)) {
            return null;
        }
        CompositeGraphicsNode parent = (CompositeGraphicsNode)node;
        List children = parent.getChildren();
        if (children == null) {
            return null;
        }
        if (children.size() >= 1) {
            return (GraphicsNode)children.get(children.size()-1);
        } else {
            return null;
        }
    }
    protected static GraphicsNode getPreviousSibling(GraphicsNode node) {
        CompositeGraphicsNode parent = node.getParent();
        if (parent == null) {
            return null;
        }
        List children = parent.getChildren();
        if (children == null) {
            return null;
        }
        int index = children.indexOf(node);
        if (index-1 >= 0) {
            return (GraphicsNode)children.get(index-1);
        } else {
            return null;
        }
    }
    protected static GraphicsNode getFirstChild(GraphicsNode node) {
        if (!(node instanceof CompositeGraphicsNode)) {
            return null;
        }
        CompositeGraphicsNode parent = (CompositeGraphicsNode)node;
        List children = parent.getChildren();
        if (children == null) {
            return null;
        }
        if (children.size() >= 1) {
            return (GraphicsNode)children.get(0);
        } else {
            return null;
        }
    }
    protected static GraphicsNode getNextSibling(GraphicsNode node) {
        CompositeGraphicsNode parent = node.getParent();
        if (parent == null) {
            return null;
        }
        List children = parent.getChildren();
        if (children == null) {
            return null;
        }
        int index = children.indexOf(node);
        if (index+1 < children.size()) {
            return (GraphicsNode)children.get(index+1);
        } else {
            return null;
        }
    }
}
