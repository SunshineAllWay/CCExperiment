package org.apache.xerces.impl.xs;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSNotationDeclaration;
import org.apache.xerces.xs.XSObjectList;
public class XSNotationDecl implements XSNotationDeclaration {
    public String fName = null;
    public String fTargetNamespace = null;
    public String fPublicId = null;
    public String fSystemId = null;
    public XSObjectList fAnnotations = null;
    private XSNamespaceItem fNamespaceItem = null;
    public short getType() {
        return XSConstants.NOTATION_DECLARATION;
    }
    public String getName() {
        return fName;
    }
    public String getNamespace() {
        return fTargetNamespace;
    }
    public String getSystemId() {
        return fSystemId;
    }
    public String getPublicId() {
        return fPublicId;
    }
    public XSAnnotation getAnnotation() {
        return (fAnnotations != null) ? (XSAnnotation) fAnnotations.item(0) : null;
    }
    public XSObjectList getAnnotations() {
        return (fAnnotations != null) ? fAnnotations : XSObjectListImpl.EMPTY_LIST;
    }
    public XSNamespaceItem getNamespaceItem() {
        return fNamespaceItem;
    }
    void setNamespaceItem(XSNamespaceItem namespaceItem) {
        fNamespaceItem = namespaceItem;
    }
} 
