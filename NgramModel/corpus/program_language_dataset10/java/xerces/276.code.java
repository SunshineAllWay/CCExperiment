package org.apache.xerces.dom3.as;
import org.w3c.dom.Node;
public interface NodeEditAS {
    public static final short WF_CHECK                  = 1;
    public static final short NS_WF_CHECK               = 2;
    public static final short PARTIAL_VALIDITY_CHECK    = 3;
    public static final short STRICT_VALIDITY_CHECK     = 4;
    public boolean canInsertBefore(Node newChild, 
                                   Node refChild);
    public boolean canRemoveChild(Node oldChild);
    public boolean canReplaceChild(Node newChild, 
                                   Node oldChild);
    public boolean canAppendChild(Node newChild);
    public boolean isNodeValid(boolean deep, 
                               short wFValidityCheckLevel)
                               throws DOMASException;
}
