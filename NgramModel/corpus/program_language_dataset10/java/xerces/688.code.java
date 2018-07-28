package org.apache.xerces.xpointer;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xs.AttributePSVI;
import org.apache.xerces.xs.XSTypeDefinition;
final class ShortHandPointer implements XPointerPart {
    private String fShortHandPointer;
    private boolean fIsFragmentResolved = false;
    private SymbolTable fSymbolTable;
    public ShortHandPointer() {
    }
    public ShortHandPointer(SymbolTable symbolTable) {
        fSymbolTable = symbolTable;
    }
    public void parseXPointer(String part) throws XNIException {
        fShortHandPointer = part;
        fIsFragmentResolved = false;
    }
    int fMatchingChildCount = 0;
    public boolean resolveXPointer(QName element, XMLAttributes attributes,
            Augmentations augs, int event) throws XNIException {
        if (fMatchingChildCount == 0) {
            fIsFragmentResolved = false;
        }
        if (event == XPointerPart.EVENT_ELEMENT_START) {
            if (fMatchingChildCount == 0) {
                fIsFragmentResolved = hasMatchingIdentifier(element, attributes, augs,
                    event);
            }
            if (fIsFragmentResolved) {
               fMatchingChildCount++;
            }
        } else if (event == XPointerPart.EVENT_ELEMENT_EMPTY) {
            if (fMatchingChildCount == 0) {
                fIsFragmentResolved = hasMatchingIdentifier(element, attributes, augs,
                    event);
            }
        }
        else {
            if (fIsFragmentResolved) {
                fMatchingChildCount--;
            }
        }
        return fIsFragmentResolved ;
    }
    private boolean hasMatchingIdentifier(QName element,
            XMLAttributes attributes, Augmentations augs, int event)
    throws XNIException {
        String normalizedValue = null;
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                normalizedValue = getSchemaDeterminedID(attributes, i);
                if (normalizedValue != null) {
                    break;
                }
                normalizedValue = getChildrenSchemaDeterminedID(attributes, i);
                if (normalizedValue != null) {
                    break;
                }
                normalizedValue = getDTDDeterminedID(attributes, i);
                if (normalizedValue != null) {
                    break;
                }
            }
        }
        if (normalizedValue != null
                && normalizedValue.equals(fShortHandPointer)) {
            return true;
        }
        return false;
    }
    public String getDTDDeterminedID(XMLAttributes attributes, int index)
    throws XNIException {
        if (attributes.getType(index).equals("ID")) {
            return attributes.getValue(index);
        }
        return null;
    }
    public String getSchemaDeterminedID(XMLAttributes attributes, int index)
    throws XNIException {
        Augmentations augs = attributes.getAugmentations(index);
        AttributePSVI attrPSVI = (AttributePSVI) augs
        .getItem(Constants.ATTRIBUTE_PSVI);
        if (attrPSVI != null) {
            XSTypeDefinition typeDef = attrPSVI.getMemberTypeDefinition();
            if (typeDef != null) {
                typeDef = attrPSVI.getTypeDefinition();
            }
            if (typeDef != null && ((XSSimpleType) typeDef).isIDType()) {
                return attrPSVI.getSchemaNormalizedValue();
            }
        }
        return null;
    }
    public String getChildrenSchemaDeterminedID(XMLAttributes attributes,
            int index) throws XNIException {
        return null;
    }
    public boolean isFragmentResolved() {
        return fIsFragmentResolved;
    }
    public boolean isChildFragmentResolved() {
        return fIsFragmentResolved && (fMatchingChildCount > 0);
    }
    public String getSchemeName() {
        return fShortHandPointer;
    }
    public String getSchemeData() {
        return null;
    }
    public void setSchemeName(String schemeName) {
        fShortHandPointer = schemeName;
    }
    public void setSchemeData(String schemeData) {
    }
}