package org.apache.xerces.impl.xs.traversers;
import java.util.Stack;
import java.util.Vector;
import org.apache.xerces.impl.validation.ValidationState;
import org.apache.xerces.impl.xs.SchemaNamespaceSupport;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XMLSchemaException;
import org.apache.xerces.impl.xs.util.XInt;
import org.apache.xerces.util.SymbolTable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
class XSDocumentInfo {
    protected SchemaNamespaceSupport fNamespaceSupport;
    protected SchemaNamespaceSupport fNamespaceSupportRoot;
    protected Stack SchemaNamespaceSupportStack = new Stack();
    protected boolean fAreLocalAttributesQualified;
    protected boolean fAreLocalElementsQualified;
    protected short fBlockDefault;
    protected short fFinalDefault;
    String fTargetNamespace;
    protected boolean fIsChameleonSchema;
    protected Element fSchemaElement;
    Vector fImportedNS = new Vector();
    protected ValidationState fValidationContext = new ValidationState();
    SymbolTable fSymbolTable = null;
    protected XSAttributeChecker fAttrChecker;
    protected Object [] fSchemaAttrs;
    protected XSAnnotationInfo fAnnotations = null;
    XSDocumentInfo (Element schemaRoot, XSAttributeChecker attrChecker, SymbolTable symbolTable)
                    throws XMLSchemaException {
        fSchemaElement = schemaRoot;
        fNamespaceSupport = new SchemaNamespaceSupport(schemaRoot, symbolTable);
        fNamespaceSupport.reset();
        fIsChameleonSchema = false;
        fSymbolTable = symbolTable;
        fAttrChecker = attrChecker;
        if (schemaRoot != null) {
            Element root = schemaRoot;
            fSchemaAttrs = attrChecker.checkAttributes(root, true, this);
            if (fSchemaAttrs == null) {
                throw new XMLSchemaException(null, null);
            }
            fAreLocalAttributesQualified =
                ((XInt)fSchemaAttrs[XSAttributeChecker.ATTIDX_AFORMDEFAULT]).intValue() == SchemaSymbols.FORM_QUALIFIED;
            fAreLocalElementsQualified =
                ((XInt)fSchemaAttrs[XSAttributeChecker.ATTIDX_EFORMDEFAULT]).intValue() == SchemaSymbols.FORM_QUALIFIED;
            fBlockDefault =
                ((XInt)fSchemaAttrs[XSAttributeChecker.ATTIDX_BLOCKDEFAULT]).shortValue();
            fFinalDefault =
                ((XInt)fSchemaAttrs[XSAttributeChecker.ATTIDX_FINALDEFAULT]).shortValue();
            fTargetNamespace =
                (String)fSchemaAttrs[XSAttributeChecker.ATTIDX_TARGETNAMESPACE];
            if (fTargetNamespace != null)
                fTargetNamespace = symbolTable.addSymbol(fTargetNamespace);
            fNamespaceSupportRoot = new SchemaNamespaceSupport(fNamespaceSupport);
            fValidationContext.setNamespaceSupport(fNamespaceSupport);
            fValidationContext.setSymbolTable(symbolTable);
        }
    }
    void backupNSSupport(SchemaNamespaceSupport nsSupport) {
        SchemaNamespaceSupportStack.push(fNamespaceSupport);
        if (nsSupport == null)
            nsSupport = fNamespaceSupportRoot;
        fNamespaceSupport = new SchemaNamespaceSupport(nsSupport);
        fValidationContext.setNamespaceSupport(fNamespaceSupport);
    }
    void restoreNSSupport() {
        fNamespaceSupport = (SchemaNamespaceSupport)SchemaNamespaceSupportStack.pop();
        fValidationContext.setNamespaceSupport(fNamespaceSupport);
    }
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (fTargetNamespace == null) {
            buf.append("no targetNamspace");
        }
        else {
            buf.append("targetNamespace is ");
            buf.append(fTargetNamespace);
        }
        Document doc = (fSchemaElement != null) ? fSchemaElement.getOwnerDocument() : null;
        if (doc instanceof org.apache.xerces.impl.xs.opti.SchemaDOM) {
            String documentURI = doc.getDocumentURI();
            if (documentURI != null && documentURI.length() > 0) {
                buf.append(" :: schemaLocation is ");
                buf.append(documentURI);
            }
        }
        return buf.toString();
    }
    public void addAllowedNS(String namespace) {
        fImportedNS.addElement(namespace == null ? "" : namespace);
    }
    public boolean isAllowedNS(String namespace) {
        return fImportedNS.contains(namespace == null ? "" : namespace);
    }
    private Vector fReportedTNS = null;
    final boolean needReportTNSError(String uri) {
        if (fReportedTNS == null)
            fReportedTNS = new Vector();
        else if (fReportedTNS.contains(uri))
            return false;
        fReportedTNS.addElement(uri);
        return true;
    }
    Object [] getSchemaAttrs () {
        return fSchemaAttrs;
    }
    void returnSchemaAttrs () {
        fAttrChecker.returnAttrArray (fSchemaAttrs, null);
        fSchemaAttrs = null;
    }
    void addAnnotation(XSAnnotationInfo info) {
        info.next = fAnnotations;
        fAnnotations = info;
    }
    XSAnnotationInfo getAnnotations() {
        return fAnnotations;
    }
    void removeAnnotations() {
        fAnnotations = null;
    }
} 
