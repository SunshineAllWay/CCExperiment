package org.apache.batik.dom;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
public abstract class AbstractProcessingInstruction
    extends    AbstractChildNode
    implements ProcessingInstruction {
    protected String data;
    public String getNodeName() {
        return getTarget();
    }
    public short getNodeType() {
        return PROCESSING_INSTRUCTION_NODE;
    }
    public String getNodeValue() throws DOMException {
        return getData();
    }
    public void setNodeValue(String nodeValue) throws DOMException {
        setData(nodeValue);
    }
    public String getData() {
        return data;
    }
    public void setData(String data) throws DOMException {
        if (isReadonly()) {
            throw createDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "readonly.node",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName() });
        }
        String val = this.data;
        this.data = data;
        fireDOMCharacterDataModifiedEvent(val, this.data);
        if (getParentNode() != null) {
            ((AbstractParentNode)getParentNode()).
                fireDOMSubtreeModifiedEvent();
        }
    }
    public String getTextContent() {
        return getNodeValue();
    }
    protected Node export(Node n, AbstractDocument d) {
        AbstractProcessingInstruction p;
        p = (AbstractProcessingInstruction)super.export(n, d);
        p.data = data;
        return p;
    }
    protected Node deepExport(Node n, AbstractDocument d) {
        AbstractProcessingInstruction p;
        p = (AbstractProcessingInstruction)super.deepExport(n, d);
        p.data = data;
        return p;
    }
    protected Node copyInto(Node n) {
        AbstractProcessingInstruction p;
        p = (AbstractProcessingInstruction)super.copyInto(n);
        p.data = data;
        return p;
    }
    protected Node deepCopyInto(Node n) {
        AbstractProcessingInstruction p;
        p = (AbstractProcessingInstruction)super.deepCopyInto(n);
        p.data = data;
        return p;
    }
}
