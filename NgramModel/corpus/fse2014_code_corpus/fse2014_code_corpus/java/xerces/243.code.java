package org.apache.xerces.dom;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
public abstract class ParentNode
    extends ChildNode {
    static final long serialVersionUID = 2815829867152120872L;
    protected CoreDocumentImpl ownerDocument;
    protected ChildNode firstChild = null;
    protected transient NodeListCache fNodeListCache = null;
    protected ParentNode(CoreDocumentImpl ownerDocument) {
        super(ownerDocument);
        this.ownerDocument = ownerDocument;
    }
    public ParentNode() {}
    public Node cloneNode(boolean deep) {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
    	ParentNode newnode = (ParentNode) super.cloneNode(deep);
        newnode.ownerDocument = ownerDocument;
    	newnode.firstChild      = null;
        newnode.fNodeListCache = null;
    	if (deep) {
            for (ChildNode child = firstChild;
                 child != null;
                 child = child.nextSibling) {
                newnode.appendChild(child.cloneNode(true));
            }
        }
    	return newnode;
    } 
    public Document getOwnerDocument() {
        return ownerDocument;
    }
    CoreDocumentImpl ownerDocument() {
        return ownerDocument;
    }
    protected void setOwnerDocument(CoreDocumentImpl doc) {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        super.setOwnerDocument(doc);
        ownerDocument = doc;
        for (ChildNode child = firstChild;
        child != null; child = child.nextSibling) {
            child.setOwnerDocument(doc);
        }
    }
    public boolean hasChildNodes() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return firstChild != null;
    }
    public NodeList getChildNodes() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return this;
    } 
    public Node getFirstChild() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
    	return firstChild;
    }   
    public Node getLastChild() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return lastChild();
    } 
    final ChildNode lastChild() {
        return firstChild != null ? firstChild.previousSibling : null;
    }
    final void lastChild(ChildNode node) {
        if (firstChild != null) {
            firstChild.previousSibling = node;
        }
    }
    public Node insertBefore(Node newChild, Node refChild) 
        throws DOMException {
        return internalInsertBefore(newChild, refChild, false);
    } 
    Node internalInsertBefore(Node newChild, Node refChild, boolean replace) 
        throws DOMException {
        boolean errorChecking = ownerDocument.errorChecking;
        if (newChild.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
            if (errorChecking) {
                for (Node kid = newChild.getFirstChild(); 
                     kid != null; kid = kid.getNextSibling()) {
                    if (!ownerDocument.isKidOK(this, kid)) {
                        throw new DOMException(
                              DOMException.HIERARCHY_REQUEST_ERR, 
                              DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null));
                    }
                }
            }
            while (newChild.hasChildNodes()) {
                insertBefore(newChild.getFirstChild(), refChild);
            }
            return newChild;
        }
        if (newChild == refChild) {
            refChild = refChild.getNextSibling();
            removeChild(newChild);
            insertBefore(newChild, refChild);
            return newChild;
        }
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        if (errorChecking) {
            if (isReadOnly()) {
                throw new DOMException(
                              DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                              DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
            }
            if (newChild.getOwnerDocument() != ownerDocument && newChild != ownerDocument) {
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, 
                            DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
            }
            if (!ownerDocument.isKidOK(this, newChild)) {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, 
                            DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null));
            }
            if (refChild != null && refChild.getParentNode() != this) {
                throw new DOMException(DOMException.NOT_FOUND_ERR,
                            DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
            }
            boolean treeSafe = true;
            for (NodeImpl a = this; treeSafe && a != null; a = a.parentNode())
            {
                treeSafe = newChild != a;
            }
            if(!treeSafe) {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, 
                            DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null));
            }
        }
        ownerDocument.insertingNode(this, replace);
        ChildNode newInternal = (ChildNode)newChild;
        Node oldparent = newInternal.parentNode();
        if (oldparent != null) {
            oldparent.removeChild(newInternal);
        }
        ChildNode refInternal = (ChildNode)refChild;
        newInternal.ownerNode = this;
        newInternal.isOwned(true);
        if (firstChild == null) {
            firstChild = newInternal;
            newInternal.isFirstChild(true);
            newInternal.previousSibling = newInternal;
        }
        else {
            if (refInternal == null) {
                ChildNode lastChild = firstChild.previousSibling;
                lastChild.nextSibling = newInternal;
                newInternal.previousSibling = lastChild;
                firstChild.previousSibling = newInternal;
            }
            else {
                if (refChild == firstChild) {
                    firstChild.isFirstChild(false);
                    newInternal.nextSibling = firstChild;
                    newInternal.previousSibling = firstChild.previousSibling;
                    firstChild.previousSibling = newInternal;
                    firstChild = newInternal;
                    newInternal.isFirstChild(true);
                }
                else {
                    ChildNode prev = refInternal.previousSibling;
                    newInternal.nextSibling = refInternal;
                    prev.nextSibling = newInternal;
                    refInternal.previousSibling = newInternal;
                    newInternal.previousSibling = prev;
                }
            }
        }
        changed();
        if (fNodeListCache != null) {
            if (fNodeListCache.fLength != -1) {
                fNodeListCache.fLength++;
            }
            if (fNodeListCache.fChildIndex != -1) {
                if (fNodeListCache.fChild == refInternal) {
                    fNodeListCache.fChild = newInternal;
                } else {
                    fNodeListCache.fChildIndex = -1;
                }
            }
        }
        ownerDocument.insertedNode(this, newInternal, replace);
        checkNormalizationAfterInsert(newInternal);
        return newChild;
    } 
    public Node removeChild(Node oldChild) 
        throws DOMException {
        return internalRemoveChild(oldChild, false);
    } 
    Node internalRemoveChild(Node oldChild, boolean replace)
        throws DOMException {
        CoreDocumentImpl ownerDocument = ownerDocument();
        if (ownerDocument.errorChecking) {
            if (isReadOnly()) {
                throw new DOMException(
                            DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                            DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
            }
            if (oldChild != null && oldChild.getParentNode() != this) {
                throw new DOMException(DOMException.NOT_FOUND_ERR, 
                            DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
            }
        }
        ChildNode oldInternal = (ChildNode) oldChild;
        ownerDocument.removingNode(this, oldInternal, replace);
        if (fNodeListCache != null) {
            if (fNodeListCache.fLength != -1) {
                fNodeListCache.fLength--;
            }
            if (fNodeListCache.fChildIndex != -1) {
                if (fNodeListCache.fChild == oldInternal) {
                    fNodeListCache.fChildIndex--;
                    fNodeListCache.fChild = oldInternal.previousSibling();
                } else {
                    fNodeListCache.fChildIndex = -1;
                }
            }
        }
        if (oldInternal == firstChild) {
            oldInternal.isFirstChild(false);
            firstChild = oldInternal.nextSibling;
            if (firstChild != null) {
                firstChild.isFirstChild(true);
                firstChild.previousSibling = oldInternal.previousSibling;
            }
        } else {
            ChildNode prev = oldInternal.previousSibling;
            ChildNode next = oldInternal.nextSibling;
            prev.nextSibling = next;
            if (next == null) {
                firstChild.previousSibling = prev;
            } else {
                next.previousSibling = prev;
            }
        }
        ChildNode oldPreviousSibling = oldInternal.previousSibling();
        oldInternal.ownerNode       = ownerDocument;
        oldInternal.isOwned(false);
        oldInternal.nextSibling     = null;
        oldInternal.previousSibling = null;
        changed();
        ownerDocument.removedNode(this, replace);
        checkNormalizationAfterRemove(oldPreviousSibling);
        return oldInternal;
    } 
    public Node replaceChild(Node newChild, Node oldChild)
        throws DOMException {
        ownerDocument.replacingNode(this);
        internalInsertBefore(newChild, oldChild, true);
        if (newChild != oldChild) {
            internalRemoveChild(oldChild, true);
        }
        ownerDocument.replacedNode(this);
        return oldChild;
    }
    public String getTextContent() throws DOMException {
        Node child = getFirstChild();
        if (child != null) {
            Node next = child.getNextSibling();
            if (next == null) {
                return hasTextContent(child) ? ((NodeImpl) child).getTextContent() : "";
            }
            StringBuffer buf = new StringBuffer();
            getTextContent(buf);
            return buf.toString();
        }
        return "";
    }
    void getTextContent(StringBuffer buf) throws DOMException {
        Node child = getFirstChild();
        while (child != null) {
            if (hasTextContent(child)) {
                ((NodeImpl) child).getTextContent(buf);
            }
            child = child.getNextSibling();
        }
    }
    final boolean hasTextContent(Node child) {
        return child.getNodeType() != Node.COMMENT_NODE &&
            child.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE &&
            (child.getNodeType() != Node.TEXT_NODE ||
             ((TextImpl) child).isIgnorableWhitespace() == false);
    }
    public void setTextContent(String textContent)
        throws DOMException {
        Node child;
        while ((child = getFirstChild()) != null) {
            removeChild(child);
        }
        if (textContent != null && textContent.length() != 0){
            appendChild(ownerDocument().createTextNode(textContent));
        }
    }
    private int nodeListGetLength() {
        if (fNodeListCache == null) {
            if (needsSyncChildren()) {
                synchronizeChildren();
            }
            if (firstChild == null) {
                return 0;
            }
            if (firstChild == lastChild()) {
                return 1;
            }
            fNodeListCache = ownerDocument.getNodeListCache(this);
        }
        if (fNodeListCache.fLength == -1) { 
            int l;
            ChildNode n;
            if (fNodeListCache.fChildIndex != -1 &&
                fNodeListCache.fChild != null) {
                l = fNodeListCache.fChildIndex;
                n = fNodeListCache.fChild;
            } else {
                n = firstChild;
                l = 0;
            }
            while (n != null) {
                l++;
                n = n.nextSibling;
            }
            fNodeListCache.fLength = l;
        }
        return fNodeListCache.fLength;
    } 
    public int getLength() {
        return nodeListGetLength();
    }
    private Node nodeListItem(int index) {
        if (fNodeListCache == null) {
            if (needsSyncChildren()) {
                synchronizeChildren();
            }
            if (firstChild == lastChild()) {
                return index == 0 ? firstChild : null;
            }
            fNodeListCache = ownerDocument.getNodeListCache(this);
        }
        int i = fNodeListCache.fChildIndex;
        ChildNode n = fNodeListCache.fChild;
        boolean firstAccess = true;
        if (i != -1 && n != null) {
            firstAccess = false;
            if (i < index) {
                while (i < index && n != null) {
                    i++;
                    n = n.nextSibling;
                }
            }
            else if (i > index) {
                while (i > index && n != null) {
                    i--;
                    n = n.previousSibling();
                }
            }
        }
        else {
            if (index < 0) {
                return null;
            }
            n = firstChild;
            for (i = 0; i < index && n != null; i++) {
                n = n.nextSibling;
            }
        }
        if (!firstAccess && (n == firstChild || n == lastChild())) {
            fNodeListCache.fChildIndex = -1;
            fNodeListCache.fChild = null;
            ownerDocument.freeNodeListCache(fNodeListCache);
        }
        else {
            fNodeListCache.fChildIndex = i;
            fNodeListCache.fChild = n;
        }
        return n;
    } 
    public Node item(int index) {
        return nodeListItem(index);
    } 
    protected final NodeList getChildNodesUnoptimized() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return new NodeList() {
                public int getLength() {
                    return nodeListGetLength();
                } 
                public Node item(int index) {
                    return nodeListItem(index);
                } 
            };
    } 
    public void normalize() {
        if (isNormalized()) {
            return;
        }
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        ChildNode kid;
        for (kid = firstChild; kid != null; kid = kid.nextSibling) {
            kid.normalize();
        }
        isNormalized(true);
    }
    public boolean isEqualNode(Node arg) {
        if (!super.isEqualNode(arg)) {
            return false;
        }
        Node child1 = getFirstChild();
        Node child2 = arg.getFirstChild();
        while (child1 != null && child2 != null) {
            if (!child1.isEqualNode(child2)) {
                return false;
            }
            child1 = child1.getNextSibling();
            child2 = child2.getNextSibling();
        }
        if (child1 != child2) {
            return false;
        }
        return true;
    }
    public void setReadOnly(boolean readOnly, boolean deep) {
        super.setReadOnly(readOnly, deep);
        if (deep) {
            if (needsSyncChildren()) {
                synchronizeChildren();
            }
            for (ChildNode mykid = firstChild;
                 mykid != null;
                 mykid = mykid.nextSibling) {
                if (mykid.getNodeType() != Node.ENTITY_REFERENCE_NODE) {
                    mykid.setReadOnly(readOnly,true);
                }
            }
        }
    } 
    protected void synchronizeChildren() {
        needsSyncChildren(false);
    }
    void checkNormalizationAfterInsert(ChildNode insertedChild) {
        if (insertedChild.getNodeType() == Node.TEXT_NODE) {
            ChildNode prev = insertedChild.previousSibling();
            ChildNode next = insertedChild.nextSibling;
            if ((prev != null && prev.getNodeType() == Node.TEXT_NODE) ||
                (next != null && next.getNodeType() == Node.TEXT_NODE)) {
                isNormalized(false);
            }
        }
        else {
            if (!insertedChild.isNormalized()) {
                isNormalized(false);
            }
        }
    } 
    void checkNormalizationAfterRemove(ChildNode previousSibling) {
        if (previousSibling != null &&
            previousSibling.getNodeType() == Node.TEXT_NODE) {
            ChildNode next = previousSibling.nextSibling;
            if (next != null && next.getNodeType() == Node.TEXT_NODE) {
                isNormalized(false);
            }
        }
    } 
    private void writeObject(ObjectOutputStream out) throws IOException {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        out.defaultWriteObject();
    } 
    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        needsSyncChildren(false);
    } 
    class UserDataRecord implements Serializable {
        private static final long serialVersionUID = 3258126977134310455L;
        Object fData;
        UserDataHandler fHandler;
        UserDataRecord(Object data, UserDataHandler handler) {
            fData = data;
            fHandler = handler;
        }
    }
} 
