package org.apache.batik.dom;
import org.w3c.dom.DOMException;
import org.w3c.dom.Entity;
import org.w3c.dom.Node;
public abstract class AbstractEntity
    extends    AbstractParentNode
    implements Entity {
    protected String nodeName;
    protected String publicId;
    protected String systemId;
    public short getNodeType() {
        return ENTITY_NODE;
    }
    public void setNodeName(String v) {
        nodeName = v;
    }
    public String getNodeName() {
        return nodeName;
    }
    public String getPublicId() {
        return publicId;
    }
    public void setPublicId(String id) {
        publicId = id;
    }
    public String getSystemId() {
        return systemId;
    }
    public void setSystemId(String id) {
        systemId = id;
    }
    public String getNotationName() {
        return getNodeName();
    }
    public void setNotationName(String name) {
        setNodeName(name);
    }
    public String getInputEncoding() {
        return null;
    }
    public String getXmlEncoding() {
        return null;
    }
    public String getXmlVersion() {
        return null;
    }
    protected Node export(Node n, AbstractDocument d) {
        super.export(n, d);
        AbstractEntity ae = (AbstractEntity)n;
        ae.nodeName = nodeName;
        ae.publicId = publicId;
        ae.systemId = systemId;
        return n;
    }
    protected Node deepExport(Node n, AbstractDocument d) {
        super.deepExport(n, d);
        AbstractEntity ae = (AbstractEntity)n;
        ae.nodeName = nodeName;
        ae.publicId = publicId;
        ae.systemId = systemId;
        return n;
    }
    protected Node copyInto(Node n) {
        super.copyInto(n);
        AbstractEntity ae = (AbstractEntity)n;
        ae.nodeName = nodeName;
        ae.publicId = publicId;
        ae.systemId = systemId;
        return n;
    }
    protected Node deepCopyInto(Node n) {
        super.deepCopyInto(n);
        AbstractEntity ae = (AbstractEntity)n;
        ae.nodeName = nodeName;
        ae.publicId = publicId;
        ae.systemId = systemId;
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
