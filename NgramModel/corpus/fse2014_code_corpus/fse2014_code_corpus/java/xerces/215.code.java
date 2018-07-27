package org.apache.xerces.dom;
public class DeferredTextImpl
    extends TextImpl
    implements DeferredNode {
    static final long serialVersionUID = 2310613872100393425L;
    protected transient int fNodeIndex;
    DeferredTextImpl(DeferredDocumentImpl ownerDocument, int nodeIndex) {
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
            (DeferredDocumentImpl) this.ownerDocument();
        data = ownerDocument.getNodeValueString(fNodeIndex);
        isIgnorableWhitespace(ownerDocument.getNodeExtra(fNodeIndex) == 1);
    } 
} 
