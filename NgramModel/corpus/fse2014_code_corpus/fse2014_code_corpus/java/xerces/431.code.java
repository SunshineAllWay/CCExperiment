package org.apache.xerces.impl.xs;
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.xs.ShortList;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSValue;
public class XSAttributeUseImpl implements XSAttributeUse {
    public XSAttributeDecl fAttrDecl = null;
    public short fUse = SchemaSymbols.USE_OPTIONAL;
    public short fConstraintType = XSConstants.VC_NONE;
    public ValidatedInfo fDefault = null;
    public XSObjectList fAnnotations = null;
    public void reset(){
        fDefault = null;
        fAttrDecl = null;
        fUse = SchemaSymbols.USE_OPTIONAL;
        fConstraintType = XSConstants.VC_NONE;
        fAnnotations = null;
    }
    public short getType() {
        return XSConstants.ATTRIBUTE_USE;
    }
    public String getName() {
        return null;
    }
    public String getNamespace() {
        return null;
    }
    public boolean getRequired() {
        return fUse == SchemaSymbols.USE_REQUIRED;
    }
    public XSAttributeDeclaration getAttrDeclaration() {
        return fAttrDecl;
    }
    public short getConstraintType() {
        return fConstraintType;
    }
    public String getConstraintValue() {
        return getConstraintType() == XSConstants.VC_NONE ?
               null :
               fDefault.stringValue();
    }
    public XSNamespaceItem getNamespaceItem() {
        return null;
    }
    public Object getActualVC() {
        return getConstraintType() == XSConstants.VC_NONE ?
               null :
               fDefault.actualValue;
    }
    public short getActualVCType() {
        return getConstraintType() == XSConstants.VC_NONE ?
               XSConstants.UNAVAILABLE_DT :
               fDefault.actualValueType;
    }
    public ShortList getItemValueTypes() {
        return getConstraintType() == XSConstants.VC_NONE ?
               null :
               fDefault.itemValueTypes;
    }
    public XSValue getValueConstraintValue() {
        return fDefault;
    }
    public XSObjectList getAnnotations() {
        return (fAnnotations != null) ? fAnnotations : XSObjectListImpl.EMPTY_LIST;
    }
} 
