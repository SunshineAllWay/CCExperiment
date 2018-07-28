package org.apache.xerces.dom;
public class DeferredNotationImpl
    extends NotationImpl
    implements DeferredNode {
    static final long serialVersionUID = 5705337172887990848L;
    protected transient int fNodeIndex;
    DeferredNotationImpl(DeferredDocumentImpl ownerDocument, int nodeIndex) {
        super(ownerDocument, null);
        fNodeIndex = nodeIndex;
        needsSyncData(true);
    } 
    public int getNodeIndex() {
        return fNodeIndex;
    }
    protected void synchronizeData() {
        needsSyncData(false);
        DeferredDocumentImpl ownerDocument =
            (DeferredDocumentImpl)this.ownerDocument();
        name = ownerDocument.getNodeName(fNodeIndex);
        ownerDocument.getNodeType(fNodeIndex);
        publicId = ownerDocument.getNodeValue(fNodeIndex);
        systemId = ownerDocument.getNodeURI(fNodeIndex);
        int extraDataIndex = ownerDocument.getNodeExtra(fNodeIndex);
        ownerDocument.getNodeType(extraDataIndex);
        baseURI = ownerDocument.getNodeName(extraDataIndex);
    } 
} 
