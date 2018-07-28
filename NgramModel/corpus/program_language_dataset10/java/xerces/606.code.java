package org.apache.xerces.util;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.w3c.dom.Node;
public final class DOMInputSource extends XMLInputSource {
    private Node fNode;
    public DOMInputSource() {
        this(null);
    }
    public DOMInputSource(Node node) {
        super(null, getSystemIdFromNode(node), null);
        fNode = node;
    }
    public DOMInputSource(Node node, String systemId) {
        super(null, systemId, null);
        fNode = node;
    }
    public Node getNode() {
        return fNode;
    }
    public void setNode(Node node) {
        fNode = node;
    }
    private static String getSystemIdFromNode(Node node) {
        if (node != null) {
            try {
                return node.getBaseURI();
            }
            catch (NoSuchMethodError e) {
                return null;
            }
            catch (Exception e) {
                return null;
            }
        }
        return null;
    }
} 
