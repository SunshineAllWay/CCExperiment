package org.apache.batik.dom;
import org.apache.batik.dom.events.NodeEventTarget;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
public interface ExtendedNode extends Node, NodeEventTarget {
    void setNodeName(String v);
    boolean isReadonly();
    void setReadonly(boolean v);
    void setOwnerDocument(Document doc);
    void setParentNode(Node v);
    void setPreviousSibling(Node n);
    void setNextSibling(Node n);
    void setSpecified(boolean v);
}
