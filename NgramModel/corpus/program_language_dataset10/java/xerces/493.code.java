package org.apache.xerces.impl.xs.traversers;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.identity.IdentityConstraint;
import org.apache.xerces.impl.xs.identity.KeyRef;
import org.apache.xerces.impl.xs.identity.UniqueOrKey;
import org.apache.xerces.xni.QName;
import org.w3c.dom.Element;
class XSDKeyrefTraverser extends XSDAbstractIDConstraintTraverser {
    public XSDKeyrefTraverser (XSDHandler handler,
                                  XSAttributeChecker gAttrCheck) {
        super(handler, gAttrCheck);
    }
    void traverse(Element krElem, XSElementDecl element,
            XSDocumentInfo schemaDoc, SchemaGrammar grammar) {
        Object[] attrValues = fAttrChecker.checkAttributes(krElem, false, schemaDoc);
        String krName = (String)attrValues[XSAttributeChecker.ATTIDX_NAME];
        if(krName == null){
            reportSchemaError("s4s-att-must-appear", new Object [] {SchemaSymbols.ELT_KEYREF , SchemaSymbols.ATT_NAME }, krElem);
            fAttrChecker.returnAttrArray(attrValues, schemaDoc);
            return;
        }
        QName kName = (QName)attrValues[XSAttributeChecker.ATTIDX_REFER];
        if(kName == null){
            reportSchemaError("s4s-att-must-appear", new Object [] {SchemaSymbols.ELT_KEYREF , SchemaSymbols.ATT_REFER }, krElem);
            fAttrChecker.returnAttrArray(attrValues, schemaDoc);
            return;
        }
        UniqueOrKey key = null;
        IdentityConstraint ret = (IdentityConstraint)fSchemaHandler.getGlobalDecl(schemaDoc, XSDHandler.IDENTITYCONSTRAINT_TYPE, kName, krElem);
        if (ret != null) {
            if (ret.getCategory() == IdentityConstraint.IC_KEY ||
                ret.getCategory() == IdentityConstraint.IC_UNIQUE) {
                key = (UniqueOrKey)ret;
            } else {
                reportSchemaError("src-resolve", new Object[]{kName.rawname, "identity constraint key/unique"}, krElem);
            }
        }
        if(key == null) {
            fAttrChecker.returnAttrArray(attrValues, schemaDoc);
            return;
        }
        KeyRef keyRef = new KeyRef(schemaDoc.fTargetNamespace, krName, element.fName, key);
        if (traverseIdentityConstraint(keyRef, krElem, schemaDoc, attrValues)) {
            if(key.getFieldCount() != keyRef.getFieldCount()) {
                reportSchemaError("c-props-correct.2" , new Object [] {krName,key.getIdentityConstraintName()}, krElem);
            } else {
                if (grammar.getIDConstraintDecl(keyRef.getIdentityConstraintName()) == null) {
                    grammar.addIDConstraintDecl(element, keyRef);
                }
                final String loc = fSchemaHandler.schemaDocument2SystemId(schemaDoc);
                final IdentityConstraint idc = grammar.getIDConstraintDecl(keyRef.getIdentityConstraintName(), loc); 
                if (idc  == null) {
                    grammar.addIDConstraintDecl(element, keyRef, loc);
                }
                if (fSchemaHandler.fTolerateDuplicates) {
                    if (idc  != null) {
                        if (idc instanceof KeyRef) {
                            keyRef = (KeyRef) idc;
                        }
                    }
                    fSchemaHandler.addIDConstraintDecl(keyRef);
                }
            }
        }
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);
    } 
} 
