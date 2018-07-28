package org.apache.batik.dom;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
public abstract class AbstractCharacterData
    extends    AbstractChildNode
    implements CharacterData {
    protected String nodeValue = "";
    public String getNodeValue() throws DOMException {
        return nodeValue;
    }
    public void setNodeValue(String nodeValue) throws DOMException {
        if (isReadonly()) {
            throw createDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "readonly.node",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName() });
        }
        String val = this.nodeValue;
        this.nodeValue = (nodeValue == null) ? "" : nodeValue;
        fireDOMCharacterDataModifiedEvent(val, this.nodeValue);
        if (getParentNode() != null) {
            ((AbstractParentNode)getParentNode()).
                fireDOMSubtreeModifiedEvent();
        }
    }
    public String getData() throws DOMException {
        return getNodeValue();
    }
    public void setData(String data) throws DOMException {
        setNodeValue(data);
    }
    public int getLength() {
        return nodeValue.length();
    }
    public String substringData(int offset, int count) throws DOMException {
        checkOffsetCount(offset, count);
        String v = getNodeValue();
        return v.substring(offset, Math.min(v.length(), offset + count));
    }
    public void appendData(String arg) throws DOMException {
        if (isReadonly()) {
            throw createDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "readonly.node",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName() });
        }
        setNodeValue(getNodeValue() + ((arg == null) ? "" : arg));
    }
    public void insertData(int offset, String arg) throws DOMException {
        if (isReadonly()) {
            throw createDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "readonly.node",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName() });
        }
        if (offset < 0 || offset > getLength()) {
            throw createDOMException(DOMException.INDEX_SIZE_ERR,
                                     "offset",
                                     new Object[] { new Integer(offset) });
        }
        String v = getNodeValue();
        setNodeValue(v.substring(0, offset) + 
                     arg + v.substring(offset, v.length()));
    }
    public void deleteData(int offset, int count) throws DOMException {
        if (isReadonly()) {
            throw createDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "readonly.node",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName() });
        }
        checkOffsetCount(offset, count);
        String v = getNodeValue();
        setNodeValue(v.substring(0, offset) +
                     v.substring(Math.min(v.length(), offset + count),
                                 v.length()));
    }
    public void replaceData(int offset, int count, String arg)
        throws DOMException {
        if (isReadonly()) {
            throw createDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "readonly.node",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName() });
        }
        checkOffsetCount(offset, count);
        String v = getNodeValue();
        setNodeValue(v.substring(0, offset) +
                     arg +
                     v.substring(Math.min(v.length(), offset + count),
                                 v.length()));
    }
    protected void checkOffsetCount(int offset, int count)
        throws DOMException {
        if (offset < 0 || offset >= getLength()) {
            throw createDOMException(DOMException.INDEX_SIZE_ERR,
                                     "offset",
                                     new Object[] { new Integer(offset) });
        }
        if (count < 0) {
            throw createDOMException(DOMException.INDEX_SIZE_ERR,
                                     "negative.count",
                                     new Object[] { new Integer(count) });
        }
    }
    protected Node export(Node n, AbstractDocument d) {
        super.export(n, d);
        AbstractCharacterData cd = (AbstractCharacterData)n;
        cd.nodeValue = nodeValue;
        return n;
    }
    protected Node deepExport(Node n, AbstractDocument d) {
        super.deepExport(n, d);
        AbstractCharacterData cd = (AbstractCharacterData)n;
        cd.nodeValue = nodeValue;
        return n;
    }
    protected Node copyInto(Node n) {
        super.copyInto(n);
        AbstractCharacterData cd = (AbstractCharacterData)n;
        cd.nodeValue = nodeValue;
        return n;
    }
    protected Node deepCopyInto(Node n) {
        super.deepCopyInto(n);
        AbstractCharacterData cd = (AbstractCharacterData)n;
        cd.nodeValue = nodeValue;
        return n;
    }
}
