package org.apache.tools.ant.taskdefs.optional.junit;
import java.util.Vector;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
public final class DOMUtil {
    private DOMUtil() {
    }
    public interface NodeFilter {
        boolean accept(Node node);
    }
    public static NodeList listChildNodes(Node parent, NodeFilter filter, boolean recurse) {
        NodeListImpl matches = new NodeListImpl();
        NodeList children = parent.getChildNodes();
        if (children != null) {
            final int len = children.getLength();
            for (int i = 0; i < len; i++) {
                Node child = children.item(i);
                if (filter.accept(child)) {
                    matches.addElement(child);
                }
                if (recurse) {
                    NodeList recmatches = listChildNodes(child, filter, recurse);
                    final int reclength = recmatches.getLength();
                    for (int j = 0; j < reclength; j++) {
                        matches.addElement(recmatches.item(i));
                    }
                }
            }
        }
        return matches;
    }
    public static class NodeListImpl extends Vector implements NodeList {
        private static final long serialVersionUID = 3175749150080946423L;
        public int getLength() {
            return size();
        }
        public Node item(int i) {
            try {
                return (Node) elementAt(i);
            } catch (ArrayIndexOutOfBoundsException e) {
                return null; 
            }
        }
    }
    public static String getNodeAttribute(Node node, String name) {
        if (node instanceof Element) {
            Element element = (Element) node;
            return element.getAttribute(name);
        }
        return null;
    }
    public static Element getChildByTagName (Node parent, String tagname) {
        if (parent == null) {
            return null;
        }
        NodeList childList = parent.getChildNodes();
        final int len = childList.getLength();
        for (int i = 0; i < len; i++) {
            Node child = childList.item(i);
            if (child != null && child.getNodeType() == Node.ELEMENT_NODE
                && child.getNodeName().equals(tagname)) {
                return (Element) child;
            }
        }
        return null;
    }
    public static Node importNode(Node parent, Node child) {
        Node copy = null;
        final Document doc = parent.getOwnerDocument();
        switch (child.getNodeType()) {
        case Node.CDATA_SECTION_NODE:
            copy = doc.createCDATASection(((CDATASection) child).getData());
            break;
        case Node.COMMENT_NODE:
            copy = doc.createComment(((Comment) child).getData());
            break;
        case Node.DOCUMENT_FRAGMENT_NODE:
            copy = doc.createDocumentFragment();
            break;
        case Node.ELEMENT_NODE:
            final Element elem = doc.createElement(((Element) child).getTagName());
            copy = elem;
            final NamedNodeMap attributes = child.getAttributes();
            if (attributes != null) {
                final int size = attributes.getLength();
                for (int i = 0; i < size; i++) {
                    final Attr attr = (Attr) attributes.item(i);
                    elem.setAttribute(attr.getName(), attr.getValue());
                }
            }
            break;
        case Node.ENTITY_REFERENCE_NODE:
            copy = doc.createEntityReference(child.getNodeName());
            break;
        case Node.PROCESSING_INSTRUCTION_NODE:
            final ProcessingInstruction pi = (ProcessingInstruction) child;
            copy = doc.createProcessingInstruction(pi.getTarget(), pi.getData());
            break;
        case Node.TEXT_NODE:
            copy = doc.createTextNode(((Text) child).getData());
            break;
        default:
            throw new IllegalStateException("Invalid node type: " + child.getNodeType());
        }
        try {
            final NodeList children = child.getChildNodes();
            if (children != null) {
                final int size = children.getLength();
                for (int i = 0; i < size; i++) {
                    final Node newChild = children.item(i);
                    if (newChild != null) {
                        importNode(copy, newChild);
                    }
                }
            }
        } catch (DOMException ignored) {
        }
        parent.appendChild(copy);
        return copy;
    }
}
