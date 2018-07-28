package org.apache.batik.dom.xbl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public interface XBLManager {
    void startProcessing();
    void stopProcessing();
    boolean isProcessing();
    Node getXblParentNode(Node n);
    NodeList getXblChildNodes(Node n);
    NodeList getXblScopedChildNodes(Node n);
    Node getXblFirstChild(Node n);
    Node getXblLastChild(Node n);
    Node getXblPreviousSibling(Node n);
    Node getXblNextSibling(Node n);
    Element getXblFirstElementChild(Node n);
    Element getXblLastElementChild(Node n);
    Element getXblPreviousElementSibling(Node n);
    Element getXblNextElementSibling(Node n);
    Element getXblBoundElement(Node n);
    Element getXblShadowTree(Node n);
    NodeList getXblDefinitions(Node n);
}
