package org.apache.batik.dom;
import org.apache.batik.dom.util.DOMUtilities;
import org.w3c.dom.DOMException;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
public abstract class AbstractEntityReference
    extends    AbstractParentChildNode
    implements EntityReference {
    protected String nodeName;
    protected AbstractEntityReference() {
    }
    protected AbstractEntityReference(String name, AbstractDocument owner)
        throws DOMException {
        ownerDocument = owner;
        if (owner.getStrictErrorChecking() && !DOMUtilities.isValidName(name)) {
            throw createDOMException(DOMException.INVALID_CHARACTER_ERR,
                                     "xml.name",
                                     new Object[] { name });
        }
        nodeName = name;
    }
    public short getNodeType() {
        return ENTITY_REFERENCE_NODE;
    }
    public void setNodeName(String v) {
        nodeName = v;
    }
    public String getNodeName() {
        return nodeName;
    }
    protected Node export(Node n, AbstractDocument d) {
        super.export(n, d);
        AbstractEntityReference ae = (AbstractEntityReference)n;
        ae.nodeName = nodeName;
        return n;
    }
    protected Node deepExport(Node n, AbstractDocument d) {
        super.deepExport(n, d);
        AbstractEntityReference ae = (AbstractEntityReference)n;
        ae.nodeName = nodeName;
        return n;
    }
    protected Node copyInto(Node n) {
        super.copyInto(n);
        AbstractEntityReference ae = (AbstractEntityReference)n;
        ae.nodeName = nodeName;
        return n;
    }
    protected Node deepCopyInto(Node n) {
        super.deepCopyInto(n);
        AbstractEntityReference ae = (AbstractEntityReference)n;
        ae.nodeName = nodeName;
        return n;
    }
    protected void checkChildType(Node n, boolean replace) {
        switch (n.getNodeType()) {
        case ELEMENT_NODE:
        case PROCESSING_INSTRUCTION_NODE:
        case COMMENT_NODE:
        case TEXT_NODE:
        case CDATA_SECTION_NODE:
        case ENTITY_REFERENCE_NODE:
        case DOCUMENT_FRAGMENT_NODE:
            break;
        default:
            throw createDOMException
                (DOMException.HIERARCHY_REQUEST_ERR,
                 "child.type",
                 new Object[] { new Integer(getNodeType()),
                                getNodeName(),
                                new Integer(n.getNodeType()),
                                n.getNodeName() });
        }
    }
}
