package org.apache.xerces.dom;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.TypeInfo;
public class AttrImpl
    extends NodeImpl
    implements Attr, TypeInfo{
    static final long serialVersionUID = 7277707688218972102L;
    static final String DTD_URI = "http://www.w3.org/TR/REC-xml";
    protected Object value = null;
    protected String name;
    transient Object type;
    protected AttrImpl(CoreDocumentImpl ownerDocument, String name) {
    	super(ownerDocument);
        this.name = name;
        isSpecified(true);
        hasStringValue(true);
    }
    protected AttrImpl() {}
    void rename(String name) {
        if (needsSyncData()) {
            synchronizeData();
        }
        this.name = name;
    }
    protected void makeChildNode() {
        if (hasStringValue()) {
            if (value != null) {
                TextImpl text =
                    (TextImpl) ownerDocument().createTextNode((String) value);
                value = text;
                text.isFirstChild(true);
                text.previousSibling = text;
                text.ownerNode = this;
                text.isOwned(true);
            }
            hasStringValue(false);
        }
    }
    protected void setOwnerDocument(CoreDocumentImpl doc) {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        super.setOwnerDocument(doc);
        if (!hasStringValue()) {
            for (ChildNode child = (ChildNode) value;
                child != null; child = child.nextSibling) {
                child.setOwnerDocument(doc);
            }
        }
    }
    public void setIdAttribute(boolean id){
        if (needsSyncData()) {
            synchronizeData();
        }
        isIdAttribute(id);
    }
    public boolean isId(){
        return isIdAttribute();
    }
    public Node cloneNode(boolean deep) {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        AttrImpl clone = (AttrImpl) super.cloneNode(deep);
    	if (!clone.hasStringValue()) {
            clone.value = null;
            for (Node child = (Node) value; child != null;
                 child = child.getNextSibling()) {
                 clone.appendChild(child.cloneNode(true));
            }
        }
        clone.isSpecified(true);
        return clone;
    }
    public short getNodeType() {
        return Node.ATTRIBUTE_NODE;
    }
    public String getNodeName() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return name;
    }
    public void setNodeValue(String value) throws DOMException {
    	setValue(value);
    }
    public String getTypeName() {
        return (String)type;
    }
    public String getTypeNamespace() {
        if (type != null) {
            return DTD_URI;
        }
        return null;
    }
    public TypeInfo getSchemaTypeInfo(){
      return this;
    }
    public String getNodeValue() {
    	return getValue();
    }
    public String getName() {
        if (needsSyncData()) {
            synchronizeData();
        }
    	return name;
    } 
    public void setValue(String newvalue) {
        CoreDocumentImpl ownerDocument = ownerDocument();
        if (ownerDocument.errorChecking && isReadOnly()) {
            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
        }
        Element ownerElement = getOwnerElement();
        String oldvalue = "";
        TextImpl textNode = null;
        if (needsSyncData()) {
            synchronizeData();
        }
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        if (value != null) {
            if (ownerDocument.getMutationEvents()) {
                if (hasStringValue()) {
                    oldvalue = (String) value;
                    textNode = (TextImpl) ownerDocument.createTextNode((String) value);
                    value = textNode;
                    textNode.isFirstChild(true);
                    textNode.previousSibling = textNode;
                    textNode.ownerNode = this;
                    textNode.isOwned(true);
                    hasStringValue(false);
                    internalRemoveChild(textNode, true);
                }
                else {
                    oldvalue = getValue();
                    while (value != null) {
                        internalRemoveChild((Node) value, true);
                    }
                }
            }
            else {
                if (hasStringValue()) {
                    oldvalue = (String) value;
                }
                else {
                    oldvalue = getValue();
                    ChildNode firstChild = (ChildNode) value;
                    firstChild.previousSibling = null;
                    firstChild.isFirstChild(false);
                    firstChild.ownerNode = ownerDocument;
                }
                value = null;
                needsSyncChildren(false);
            }
            if (isIdAttribute() && ownerElement != null) {
                ownerDocument.removeIdentifier(oldvalue);
            }
        }
    	isSpecified(true);
        if (ownerDocument.getMutationEvents()) {
            if (textNode == null) {
                textNode = (TextImpl) ownerDocument.createTextNode(newvalue);
            } 
            else {
                textNode.data = newvalue;
            }
            internalInsertBefore(textNode, null, true);
            hasStringValue(false);
            ownerDocument.modifiedAttrValue(this, oldvalue);
        } else {
            value = newvalue;
            hasStringValue(true);
            changed();
        }
        if (isIdAttribute() && ownerElement != null) {
            ownerDocument.putIdentifier(newvalue, ownerElement);
        }
    } 
    public String getValue() {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        if (value == null) {
            return "";
        }
        if (hasStringValue()) {
            return (String) value;
        }
        ChildNode firstChild = ((ChildNode) value);
        String data = null;
        if (firstChild.getNodeType() == Node.ENTITY_REFERENCE_NODE){
                data = ((EntityReferenceImpl)firstChild).getEntityRefValue();
        }
        else {
                data =  firstChild.getNodeValue();
        }
        ChildNode node = firstChild.nextSibling;
        if (node == null || data == null)  return (data == null)?"":data;
        StringBuffer value = new StringBuffer(data);
    	while (node != null) {
            if (node.getNodeType()  == Node.ENTITY_REFERENCE_NODE){
                data = ((EntityReferenceImpl)node).getEntityRefValue();
                if (data == null) return "";
                value.append(data);
            }
            else {
                value.append(node.getNodeValue());
            }
            node = node.nextSibling;
    	}
    	return value.toString();
    } 
    public boolean getSpecified() {
        if (needsSyncData()) {
            synchronizeData();
        }
    	return isSpecified();
    } 
    public Element getElement() {
        return (Element) (isOwned() ? ownerNode : null);
    }
    public Element getOwnerElement() {
        return (Element) (isOwned() ? ownerNode : null);
    }
    public void normalize() {
        if (isNormalized() || hasStringValue())
            return;
        Node kid, next;
        ChildNode firstChild = (ChildNode)value;
        for (kid = firstChild; kid != null; kid = next) {
            next = kid.getNextSibling();
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
        }
        isNormalized(true);
    } 
    public void setSpecified(boolean arg) {
        if (needsSyncData()) {
            synchronizeData();
        }
    	isSpecified(arg);
    } 
    public void setType (Object type){
        this.type = type;
    }
    public String toString() {
    	return getName() + "=" + "\"" + getValue() + "\"";
    }
    public boolean hasChildNodes() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return value != null;
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
        makeChildNode();
    	return (Node) value;
    }   
    public Node getLastChild() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return lastChild();
    } 
    final ChildNode lastChild() {
        makeChildNode();
        return value != null ? ((ChildNode) value).previousSibling : null;
    }
    final void lastChild(ChildNode node) {
        if (value != null) {
            ((ChildNode) value).previousSibling = node;
        }
    }
    public Node insertBefore(Node newChild, Node refChild) 
        throws DOMException {
        return internalInsertBefore(newChild, refChild, false);
    } 
    Node internalInsertBefore(Node newChild, Node refChild, boolean replace) 
        throws DOMException {
        CoreDocumentImpl ownerDocument = ownerDocument();
        boolean errorChecking = ownerDocument.errorChecking;
        if (newChild.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
            if (errorChecking) {
                for (Node kid = newChild.getFirstChild(); 
                     kid != null; kid = kid.getNextSibling()) {
                    if (!ownerDocument.isKidOK(this, kid)) {
                        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null);
                        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, msg);
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
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
                throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
            }
            if (newChild.getOwnerDocument() != ownerDocument) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
            }
            if (!ownerDocument.isKidOK(this, newChild)) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null);
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, msg);
            }
            if (refChild != null && refChild.getParentNode() != this) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null);
                throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
            }
            boolean treeSafe = true;
            for (NodeImpl a = this; treeSafe && a != null; a = a.parentNode())
            {
                treeSafe = newChild != a;
            }
            if (!treeSafe) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null);
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, msg);
            }
        }
        makeChildNode(); 
        ownerDocument.insertingNode(this, replace);
        ChildNode newInternal = (ChildNode)newChild;
        Node oldparent = newInternal.parentNode();
        if (oldparent != null) {
            oldparent.removeChild(newInternal);
        }
        ChildNode refInternal = (ChildNode) refChild;
        newInternal.ownerNode = this;
        newInternal.isOwned(true);
        ChildNode firstChild = (ChildNode) value;
        if (firstChild == null) {
            value = newInternal; 
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
                    value = newInternal; 
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
        ownerDocument.insertedNode(this, newInternal, replace);
        checkNormalizationAfterInsert(newInternal);
        return newChild;
    } 
    public Node removeChild(Node oldChild) 
        throws DOMException {
        if (hasStringValue()) {
            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null);
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);            
        }
        return internalRemoveChild(oldChild, false);
    } 
    Node internalRemoveChild(Node oldChild, boolean replace)
        throws DOMException {
        CoreDocumentImpl ownerDocument = ownerDocument();
        if (ownerDocument.errorChecking) {
            if (isReadOnly()) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
                throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
            }
            if (oldChild != null && oldChild.getParentNode() != this) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null);
                throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
            }
        }
        ChildNode oldInternal = (ChildNode) oldChild;
        ownerDocument.removingNode(this, oldInternal, replace);
        if (oldInternal == value) { 
            oldInternal.isFirstChild(false);
            value = oldInternal.nextSibling;
            ChildNode firstChild = (ChildNode) value;
            if (firstChild != null) {
                firstChild.isFirstChild(true);
                firstChild.previousSibling = oldInternal.previousSibling;
            }
        } else {
            ChildNode prev = oldInternal.previousSibling;
            ChildNode next = oldInternal.nextSibling;
            prev.nextSibling = next;
            if (next == null) {
                ChildNode firstChild = (ChildNode) value;
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
        makeChildNode();
        CoreDocumentImpl ownerDocument = ownerDocument();
        ownerDocument.replacingNode(this);
        internalInsertBefore(newChild, oldChild, true);
        if (newChild != oldChild) {
            internalRemoveChild(oldChild, true);
        }
        ownerDocument.replacedNode(this);
        return oldChild;
    }
    public int getLength() {
        if (hasStringValue()) {
            return 1;
        }
        ChildNode node = (ChildNode) value;
        int length = 0;
        for (; node != null; node = node.nextSibling) {
            length++;
        }
        return length;
    } 
    public Node item(int index) {
        if (hasStringValue()) {
            if (index != 0 || value == null) {
                return null;
            }
            else {
                makeChildNode();
                return (Node) value;
            }
        }
        if (index < 0) {
            return null;
        }
        ChildNode node = (ChildNode) value;
        for (int i = 0; i < index && node != null; i++) {
            node = node.nextSibling;
        } 
        return node;
    } 
    public boolean isEqualNode(Node arg) {
        return super.isEqualNode(arg);
    }
    public boolean isDerivedFrom(String typeNamespaceArg, 
                                 String typeNameArg, 
                                 int derivationMethod) {
        return false;
    }
    public void setReadOnly(boolean readOnly, boolean deep) {
        super.setReadOnly(readOnly, deep);
        if (deep) {
            if (needsSyncChildren()) {
                synchronizeChildren();
            }
            if (hasStringValue()) {
                return;
            }
            for (ChildNode mykid = (ChildNode) value;
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
} 
