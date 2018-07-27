package org.apache.xerces.impl.xs.traversers;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XSAnnotationImpl;
import org.apache.xerces.impl.xs.XSNotationDecl;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.util.DOMUtil;
import org.apache.xerces.xs.XSObjectList;
import org.w3c.dom.Element;
class  XSDNotationTraverser extends XSDAbstractTraverser {
    XSDNotationTraverser (XSDHandler handler,
            XSAttributeChecker gAttrCheck) {
        super(handler, gAttrCheck);
    }
    XSNotationDecl traverse(Element elmNode,
            XSDocumentInfo schemaDoc,
            SchemaGrammar grammar) {
        Object[] attrValues = fAttrChecker.checkAttributes(elmNode, true, schemaDoc);
        String  nameAttr   = (String) attrValues[XSAttributeChecker.ATTIDX_NAME];
        String  publicAttr = (String) attrValues[XSAttributeChecker.ATTIDX_PUBLIC];
        String  systemAttr = (String) attrValues[XSAttributeChecker.ATTIDX_SYSTEM];
        if (nameAttr == null) {
            reportSchemaError("s4s-att-must-appear", new Object[]{SchemaSymbols.ELT_NOTATION, SchemaSymbols.ATT_NAME}, elmNode);
            fAttrChecker.returnAttrArray(attrValues, schemaDoc);
            return null;
        }
        if (systemAttr == null && publicAttr == null) {
            reportSchemaError("PublicSystemOnNotation", null, elmNode);
            publicAttr = "missing";
        }
        XSNotationDecl notation = new XSNotationDecl();
        notation.fName = nameAttr;
        notation.fTargetNamespace = schemaDoc.fTargetNamespace;
        notation.fPublicId = publicAttr;
        notation.fSystemId = systemAttr;
        Element content = DOMUtil.getFirstChildElement(elmNode);
        XSAnnotationImpl annotation = null;
        if (content != null && DOMUtil.getLocalName(content).equals(SchemaSymbols.ELT_ANNOTATION)) {
            annotation = traverseAnnotationDecl(content, attrValues, false, schemaDoc);
            content = DOMUtil.getNextSiblingElement(content);
        }
        else {
            String text = DOMUtil.getSyntheticAnnotation(elmNode);
            if (text != null) {
                annotation = traverseSyntheticAnnotation(elmNode, text, attrValues, false, schemaDoc);
            }
        }
        XSObjectList annotations;
        if (annotation != null) {
            annotations = new XSObjectListImpl();
            ((XSObjectListImpl) annotations).addXSObject(annotation);
        } else {
            annotations = XSObjectListImpl.EMPTY_LIST;
        }
        notation.fAnnotations = annotations;
        if (content!=null){
            Object[] args = new Object [] {SchemaSymbols.ELT_NOTATION, "(annotation?)", DOMUtil.getLocalName(content)};
            reportSchemaError("s4s-elt-must-match.1", args, content);
        }
        if (grammar.getGlobalNotationDecl(notation.fName) == null) {
            grammar.addGlobalNotationDecl(notation);
        }
        final String loc = fSchemaHandler.schemaDocument2SystemId(schemaDoc);
        final XSNotationDecl notation2 = grammar.getGlobalNotationDecl(notation.fName, loc);  
        if (notation2 == null) {
            grammar.addGlobalNotationDecl(notation, loc);
        }
        if (fSchemaHandler.fTolerateDuplicates) {
            if (notation2 != null) {
                notation = notation2;
            }
            fSchemaHandler.addGlobalNotationDecl(notation);
        }
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);
        return notation;
    }
}
