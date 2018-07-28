package org.apache.xerces.impl.xs.traversers;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.identity.IdentityConstraint;
import org.apache.xerces.impl.xs.identity.UniqueOrKey;
import org.apache.xerces.util.DOMUtil;
import org.w3c.dom.Element;
class XSDUniqueOrKeyTraverser extends XSDAbstractIDConstraintTraverser {
    public XSDUniqueOrKeyTraverser (XSDHandler handler,
                                  XSAttributeChecker gAttrCheck) {
        super(handler, gAttrCheck);
    }
    void traverse(Element uElem, XSElementDecl element,
            XSDocumentInfo schemaDoc, SchemaGrammar grammar) {
        Object[] attrValues = fAttrChecker.checkAttributes(uElem, false, schemaDoc);
        String uName = (String)attrValues[XSAttributeChecker.ATTIDX_NAME];
        if(uName == null){
            reportSchemaError("s4s-att-must-appear", new Object [] {DOMUtil.getLocalName(uElem) , SchemaSymbols.ATT_NAME }, uElem);
            fAttrChecker.returnAttrArray(attrValues, schemaDoc);
            return;
        }
        UniqueOrKey uniqueOrKey = null;
        if(DOMUtil.getLocalName(uElem).equals(SchemaSymbols.ELT_UNIQUE)) {
            uniqueOrKey = new UniqueOrKey(schemaDoc.fTargetNamespace, uName, element.fName, IdentityConstraint.IC_UNIQUE);
        } else {
            uniqueOrKey = new UniqueOrKey(schemaDoc.fTargetNamespace, uName, element.fName, IdentityConstraint.IC_KEY);
        }
        if (traverseIdentityConstraint(uniqueOrKey, uElem, schemaDoc, attrValues)) {
            if (grammar.getIDConstraintDecl(uniqueOrKey.getIdentityConstraintName()) == null) {
                grammar.addIDConstraintDecl(element, uniqueOrKey);
            }
            final String loc = fSchemaHandler.schemaDocument2SystemId(schemaDoc);
            final IdentityConstraint idc = grammar.getIDConstraintDecl(uniqueOrKey.getIdentityConstraintName(), loc);  
            if (idc == null) {
                grammar.addIDConstraintDecl(element, uniqueOrKey, loc);
            }
            if (fSchemaHandler.fTolerateDuplicates) {
                if (idc != null) {
                    if (idc instanceof UniqueOrKey) {
                        uniqueOrKey = (UniqueOrKey) uniqueOrKey;
                    }
                }
                fSchemaHandler.addIDConstraintDecl(uniqueOrKey);
            }
        }
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);
    } 
} 
