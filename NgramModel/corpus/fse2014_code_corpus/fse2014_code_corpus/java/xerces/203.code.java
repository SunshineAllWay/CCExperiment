package org.apache.xerces.dom;
public class DeferredCommentImpl 
    extends CommentImpl 
    implements DeferredNode {
    static final long serialVersionUID = 6498796371083589338L;
    protected transient int fNodeIndex;
    DeferredCommentImpl(DeferredDocumentImpl ownerDocument, int nodeIndex) {
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
    } 
} 
