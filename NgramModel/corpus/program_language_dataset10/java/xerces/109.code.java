package org.apache.html.dom;
import org.apache.xerces.dom.DeepNodeListImpl;
import org.apache.xerces.dom.ElementImpl;
import org.apache.xerces.dom.NodeImpl;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class NameNodeListImpl 
    extends DeepNodeListImpl
    implements NodeList {
    public NameNodeListImpl(NodeImpl rootNode, String tagName) {
	super( rootNode, tagName );
    }  
    protected Node nextMatchingElementAfter(Node current) {
        Node next;
        while (current != null) {
            if (current.hasChildNodes()) {
                current = (current.getFirstChild());
            }
            else if (current != rootNode && null != (next = current.getNextSibling())) {
                current = next;
            }
            else {
                next = null;
                for (; current != rootNode; 
                     current = current.getParentNode()) {
                    next = current.getNextSibling();
                    if (next != null)
                        break;
                }
                current = next;
            }
            if (current != rootNode && current != null
                && current.getNodeType() ==  Node.ELEMENT_NODE  ) {
                String name = ((ElementImpl) current).getAttribute( "name" );
                if ( name.equals("*") || name.equals(tagName))
                    return current;
            }
        }
        return null;
    } 
} 
