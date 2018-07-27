package org.apache.batik.dom;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.util.XMLConstants;
import org.apache.batik.xml.XMLUtilities;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
public abstract class AbstractText
    extends    AbstractCharacterData
    implements Text {
    public Text splitText(int offset) throws DOMException {
        if (isReadonly()) {
            throw createDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "readonly.node",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName() });
        }
        String v = getNodeValue();
        if (offset < 0 || offset >= v.length()) {
            throw createDOMException(DOMException.INDEX_SIZE_ERR,
                                     "offset",
                                     new Object[] { new Integer(offset) });
        }
        Node n = getParentNode();
        if (n == null) {
            throw createDOMException(DOMException.INDEX_SIZE_ERR,
                                     "need.parent",
                                     new Object[] {});
        }
        String t1 = v.substring(offset);
        Text t = createTextNode(t1);
        Node ns = getNextSibling();
        if (ns != null) {
            n.insertBefore(t, ns);
        } else {
            n.appendChild(t);
        }
        setNodeValue(v.substring(0, offset));
        return t;
    }
    protected Node getPreviousLogicallyAdjacentTextNode(Node n) {
        Node p = n.getPreviousSibling();
        Node parent = n.getParentNode();
        while (p == null
                && parent != null
                && parent.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
            p = parent;
            parent = p.getParentNode();
            p = p.getPreviousSibling();
        }
        while (p != null && p.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
            p = p.getLastChild();
        }
        if (p == null) {
            return null;
        }
        int nt = p.getNodeType();
        if (nt == Node.TEXT_NODE || nt == Node.CDATA_SECTION_NODE) {
            return p;
        }
        return null;
    }
    protected Node getNextLogicallyAdjacentTextNode(Node n) {
        Node p = n.getNextSibling();
        Node parent = n.getParentNode();
        while (p == null
                && parent != null
                && parent.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
            p = parent;
            parent = p.getParentNode();
            p = p.getNextSibling();
        }
        while (p != null && p.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
            p = p.getFirstChild();
        }
        if (p == null) {
            return null;
        }
        int nt = p.getNodeType();
        if (nt == Node.TEXT_NODE || nt == Node.CDATA_SECTION_NODE) {
            return p;
        }
        return null;
    }
    public String getWholeText() {
        StringBuffer sb = new StringBuffer();
        for (Node n = this;
                n != null;
                n = getPreviousLogicallyAdjacentTextNode(n)) {
            sb.insert(0, n.getNodeValue());
        }
        for (Node n = getNextLogicallyAdjacentTextNode(this);
                n != null;
                n = getNextLogicallyAdjacentTextNode(n)) {
            sb.append(n.getNodeValue());
        }
        return sb.toString();
    }
    public boolean isElementContentWhitespace() {
        int len = nodeValue.length();
        for (int i = 0; i < len; i++) {
            if (!XMLUtilities.isXMLSpace(nodeValue.charAt(i))) {
                return false;
            }
        }
        Node p = getParentNode();
        if (p.getNodeType() == Node.ELEMENT_NODE) {
            String sp = XMLSupport.getXMLSpace((Element) p);
            return !sp.equals(XMLConstants.XML_PRESERVE_VALUE);
        }
        return true;
    }
    public Text replaceWholeText(String s) throws DOMException {
        for (Node n = getPreviousLogicallyAdjacentTextNode(this);
                n != null;
                n = getPreviousLogicallyAdjacentTextNode(n)) {
            AbstractNode an = (AbstractNode) n;
            if (an.isReadonly()) {
                throw createDOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                     "readonly.node",
                     new Object[] { new Integer(n.getNodeType()),
                                    n.getNodeName() });
            }
        }
        for (Node n = getNextLogicallyAdjacentTextNode(this);
                n != null;
                n = getNextLogicallyAdjacentTextNode(n)) {
            AbstractNode an = (AbstractNode) n;
            if (an.isReadonly()) {
                throw createDOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                     "readonly.node",
                     new Object[] { new Integer(n.getNodeType()),
                                    n.getNodeName() });
            }
        }
        Node parent = getParentNode();
        for (Node n = getPreviousLogicallyAdjacentTextNode(this);
                n != null;
                n = getPreviousLogicallyAdjacentTextNode(n)) {
            parent.removeChild(n);
        }
        for (Node n = getNextLogicallyAdjacentTextNode(this);
                n != null;
                n = getNextLogicallyAdjacentTextNode(n)) {
            parent.removeChild(n);
        }
        if (isReadonly()) {
            Text t = createTextNode(s);
            parent.replaceChild(t, this);
            return t;
        }
        setNodeValue(s);
        return this;
    }
    public String getTextContent() {
        if (isElementContentWhitespace()) {
            return "";
        }
        return getNodeValue();
    }
    protected abstract Text createTextNode(String text);
}
