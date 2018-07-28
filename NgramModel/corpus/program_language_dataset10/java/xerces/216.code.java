package org.apache.xerces.dom;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
public class DocumentFragmentImpl 
    extends ParentNode
    implements DocumentFragment {
    static final long serialVersionUID = -7596449967279236746L;
    public DocumentFragmentImpl(CoreDocumentImpl ownerDoc) {
        super(ownerDoc);
    }  
    public DocumentFragmentImpl() {}
    public short getNodeType() {
        return Node.DOCUMENT_FRAGMENT_NODE;
    }
    public String getNodeName() {
        return "#document-fragment";
    }
    public void normalize() {
        if (isNormalized()) {
            return;
        }
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        ChildNode kid, next;
        for (kid = firstChild; kid != null; kid = next) {
            next = kid.nextSibling;
            if ( kid.getNodeType() == Node.TEXT_NODE )
            {
                if ( next!=null && next.getNodeType() == Node.TEXT_NODE )
                {
                    ((Text)kid).appendData(next.getNodeValue());
                    removeChild( next );
                    next = kid; 
                }
                else
                {
                    if ( kid.getNodeValue() == null || kid.getNodeValue().length() == 0 ) {
                        removeChild( kid );
                    }
                }
            }
            kid.normalize();
        }
        isNormalized(true);
    }
} 
