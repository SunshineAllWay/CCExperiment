package org.apache.xerces.impl.xs.traversers;
import org.apache.xerces.impl.xpath.XPathException;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.identity.Field;
import org.apache.xerces.impl.xs.identity.IdentityConstraint;
import org.apache.xerces.impl.xs.identity.Selector;
import org.apache.xerces.util.DOMUtil;
import org.apache.xerces.util.XMLChar;
import org.w3c.dom.Element;
class XSDAbstractIDConstraintTraverser extends XSDAbstractTraverser {
    public XSDAbstractIDConstraintTraverser (XSDHandler handler,
            XSAttributeChecker gAttrCheck) {
        super(handler, gAttrCheck);
    }
    boolean traverseIdentityConstraint(IdentityConstraint ic,
            Element icElem, XSDocumentInfo schemaDoc, Object [] icElemAttrs) {
        Element sElem = DOMUtil.getFirstChildElement(icElem);
        if(sElem == null) {
            reportSchemaError("s4s-elt-must-match.2",
                    new Object[]{"identity constraint", "(annotation?, selector, field+)"},
                    icElem);
            return false;
        }
        if (DOMUtil.getLocalName(sElem).equals(SchemaSymbols.ELT_ANNOTATION)) {
            ic.addAnnotation(traverseAnnotationDecl(sElem, icElemAttrs, false, schemaDoc));
            sElem = DOMUtil.getNextSiblingElement(sElem);
            if(sElem == null) {
                reportSchemaError("s4s-elt-must-match.2", new Object[]{"identity constraint", "(annotation?, selector, field+)"}, icElem);
                return false;
            }
        }
        else {
            String text = DOMUtil.getSyntheticAnnotation(icElem);
            if (text != null) {
                ic.addAnnotation(traverseSyntheticAnnotation(icElem, text, icElemAttrs, false, schemaDoc));
            }
        }
        if(!DOMUtil.getLocalName(sElem).equals(SchemaSymbols.ELT_SELECTOR)) {
            reportSchemaError("s4s-elt-must-match.1", new Object[]{"identity constraint", "(annotation?, selector, field+)", SchemaSymbols.ELT_SELECTOR}, sElem);
            return false;
        }
        Object [] attrValues = fAttrChecker.checkAttributes(sElem, false, schemaDoc);
        Element selChild = DOMUtil.getFirstChildElement(sElem);
        if (selChild !=null) {
            if (DOMUtil.getLocalName(selChild).equals(SchemaSymbols.ELT_ANNOTATION)) {
                ic.addAnnotation(traverseAnnotationDecl(selChild, attrValues, false, schemaDoc));
                selChild = DOMUtil.getNextSiblingElement(selChild);
            }
            else {
                reportSchemaError("s4s-elt-must-match.1", new Object[]{SchemaSymbols.ELT_SELECTOR, "(annotation?)", DOMUtil.getLocalName(selChild)}, selChild);
            }
            if (selChild != null) {
                reportSchemaError("s4s-elt-must-match.1", new Object [] {SchemaSymbols.ELT_SELECTOR, "(annotation?)", DOMUtil.getLocalName(selChild)}, selChild);
            }
        }
        else {
            String text = DOMUtil.getSyntheticAnnotation(sElem);
            if (text != null) {
                ic.addAnnotation(traverseSyntheticAnnotation(icElem, text, attrValues, false, schemaDoc));
            }
        }
        String sText = ((String)attrValues[XSAttributeChecker.ATTIDX_XPATH]);
        if(sText == null) {
            reportSchemaError("s4s-att-must-appear", new Object [] {SchemaSymbols.ELT_SELECTOR, SchemaSymbols.ATT_XPATH}, sElem);
            return false;
        }
        sText = XMLChar.trim(sText);
        Selector.XPath sXpath = null;
        try {
            sXpath = new Selector.XPath(sText, fSymbolTable,
                    schemaDoc.fNamespaceSupport);
            Selector selector = new Selector(sXpath, ic);
            ic.setSelector(selector);
        }
        catch (XPathException e) {
            reportSchemaError(e.getKey(), new Object[]{sText}, sElem);
            fAttrChecker.returnAttrArray(attrValues, schemaDoc);
            return false;
        }
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);
        Element fElem = DOMUtil.getNextSiblingElement(sElem);
        if(fElem == null) {
            reportSchemaError("s4s-elt-must-match.2", new Object[]{"identity constraint", "(annotation?, selector, field+)"}, sElem);
            return false;
        }
        while (fElem != null) {
            if(!DOMUtil.getLocalName(fElem).equals(SchemaSymbols.ELT_FIELD)) {
                reportSchemaError("s4s-elt-must-match.1", new Object[]{"identity constraint", "(annotation?, selector, field+)", SchemaSymbols.ELT_FIELD}, fElem);
                fElem = DOMUtil.getNextSiblingElement(fElem);
                continue;
            }
            attrValues = fAttrChecker.checkAttributes(fElem, false, schemaDoc);
            Element fieldChild = DOMUtil.getFirstChildElement(fElem);
            if (fieldChild != null) {            
                if (DOMUtil.getLocalName(fieldChild).equals(SchemaSymbols.ELT_ANNOTATION)) {
                    ic.addAnnotation(traverseAnnotationDecl(fieldChild, attrValues, false, schemaDoc));
                    fieldChild = DOMUtil.getNextSiblingElement(fieldChild);
                }
            }
            if (fieldChild != null) {
                reportSchemaError("s4s-elt-must-match.1", new Object [] {SchemaSymbols.ELT_FIELD, "(annotation?)", DOMUtil.getLocalName(fieldChild)}, fieldChild);
            }
            else {
                String text = DOMUtil.getSyntheticAnnotation(fElem);
                if (text != null) {
                    ic.addAnnotation(traverseSyntheticAnnotation(icElem, text, attrValues, false, schemaDoc));
                }
            }
            String fText = ((String)attrValues[XSAttributeChecker.ATTIDX_XPATH]);
            if (fText == null) {
                reportSchemaError("s4s-att-must-appear", new Object [] {SchemaSymbols.ELT_FIELD, SchemaSymbols.ATT_XPATH}, fElem);
                fAttrChecker.returnAttrArray(attrValues, schemaDoc);
                return false;
            }
            fText = XMLChar.trim(fText);
            try {
                Field.XPath fXpath = new Field.XPath(fText, fSymbolTable,
                        schemaDoc.fNamespaceSupport);
                Field field = new Field(fXpath, ic);
                ic.addField(field);
            }
            catch (XPathException e) {
                reportSchemaError(e.getKey(), new Object[]{fText}, fElem);
                fAttrChecker.returnAttrArray(attrValues, schemaDoc);
                return false;
            }
            fElem = DOMUtil.getNextSiblingElement(fElem);
            fAttrChecker.returnAttrArray(attrValues, schemaDoc);
        }
        return ic.getFieldCount() > 0;
    } 
} 
