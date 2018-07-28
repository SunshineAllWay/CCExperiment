package org.apache.xerces.dom;
import org.w3c.dom.Node;
public class DeferredDocumentTypeImpl
    extends DocumentTypeImpl
    implements DeferredNode {
    static final long serialVersionUID = -2172579663227313509L;
    protected transient int fNodeIndex;
    DeferredDocumentTypeImpl(DeferredDocumentImpl ownerDocument, int nodeIndex) {
        super(ownerDocument, null);
        fNodeIndex = nodeIndex;
        needsSyncData(true);
        needsSyncChildren(true);
    } 
    public int getNodeIndex() {
        return fNodeIndex;
    }
    protected void synchronizeData() {
        needsSyncData(false);
        DeferredDocumentImpl ownerDocument =
            (DeferredDocumentImpl)this.ownerDocument;
        name = ownerDocument.getNodeName(fNodeIndex);
        publicID = ownerDocument.getNodeValue(fNodeIndex);
        systemID = ownerDocument.getNodeURI(fNodeIndex);
        int extraDataIndex = ownerDocument.getNodeExtra(fNodeIndex);
        internalSubset = ownerDocument.getNodeValue(extraDataIndex);
    } 
    protected void synchronizeChildren() {
        boolean orig = ownerDocument().getMutationEvents();
        ownerDocument().setMutationEvents(false);
        needsSyncChildren(false);
        DeferredDocumentImpl ownerDocument =
            (DeferredDocumentImpl)this.ownerDocument;
        entities  = new NamedNodeMapImpl(this);
        notations = new NamedNodeMapImpl(this);
        elements  = new NamedNodeMapImpl(this);
        DeferredNode last = null;
        for (int index = ownerDocument.getLastChild(fNodeIndex);
            index != -1;
            index = ownerDocument.getPrevSibling(index)) {
            DeferredNode node = ownerDocument.getNodeObject(index);
            int type = node.getNodeType();
            switch (type) {
                case Node.ENTITY_NODE: {
                    entities.setNamedItem(node);
                    break;
                }
                case Node.NOTATION_NODE: {
                    notations.setNamedItem(node);
                    break;
                }
                case NodeImpl.ELEMENT_DEFINITION_NODE: {
                    elements.setNamedItem(node);
                    break;
                }
                case Node.ELEMENT_NODE: {
                    if (((DocumentImpl)getOwnerDocument()).allowGrammarAccess){
                        insertBefore(node, last);
                        last = node;
                        break;
                    }
                }
                default: {
                    System.out.println("DeferredDocumentTypeImpl" +
                                       "#synchronizeInfo: " +
                                       "node.getNodeType() = " +
                                       node.getNodeType() +
                                       ", class = " +
                                       node.getClass().getName());
                }
             }
        }
        ownerDocument().setMutationEvents(orig);
        setReadOnly(true, false);
    } 
} 
