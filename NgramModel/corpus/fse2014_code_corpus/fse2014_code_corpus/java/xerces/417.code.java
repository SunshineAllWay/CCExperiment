package org.apache.xerces.impl.xs;
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.xs.util.StringListImpl;
import org.apache.xerces.xs.AttributePSVI;
import org.apache.xerces.xs.ShortList;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSValue;
public class AttributePSVImpl implements AttributePSVI {
    protected XSAttributeDeclaration fDeclaration = null;
    protected XSTypeDefinition fTypeDecl = null;
    protected boolean fSpecified = false;
    protected ValidatedInfo fValue = new ValidatedInfo();
    protected short fValidationAttempted = AttributePSVI.VALIDATION_NONE;
    protected short fValidity = AttributePSVI.VALIDITY_NOTKNOWN;
    protected String[] fErrors = null;
    protected String fValidationContext = null;
    public String getSchemaDefault() {
        return fDeclaration == null ? null : fDeclaration.getConstraintValue();
    }
    public String getSchemaNormalizedValue() {
        return fValue.getNormalizedValue();
    }
    public boolean getIsSchemaSpecified() {
        return fSpecified;
    }
    public short getValidationAttempted() {
        return fValidationAttempted;
    }
    public short getValidity() {
        return fValidity;
    }
    public StringList getErrorCodes() {
        if (fErrors == null || fErrors.length == 0) {
            return StringListImpl.EMPTY_LIST;
        }
        return new PSVIErrorList(fErrors, true);
    }
    public StringList getErrorMessages() {
        if (fErrors == null || fErrors.length == 0) {
            return StringListImpl.EMPTY_LIST;
        }
        return new PSVIErrorList(fErrors, false);
    }
    public String getValidationContext() {
        return fValidationContext;
    }
    public XSTypeDefinition getTypeDefinition() {
        return fTypeDecl;
    }
    public XSSimpleTypeDefinition getMemberTypeDefinition() {
        return fValue.getMemberTypeDefinition();
    }
    public XSAttributeDeclaration getAttributeDeclaration() {
        return fDeclaration;
    }
    public Object getActualNormalizedValue() {
        return fValue.getActualValue();
    }
    public short getActualNormalizedValueType() {
        return fValue.getActualValueType();
    }
    public ShortList getItemValueTypes() {
        return fValue.getListValueTypes();
    }
    public XSValue getSchemaValue() {
        return fValue;
    }
    public void reset() {
        fValue.reset();
        fDeclaration = null;
        fTypeDecl = null;
        fSpecified = false;
        fValidationAttempted = AttributePSVI.VALIDATION_NONE;
        fValidity = AttributePSVI.VALIDITY_NOTKNOWN;
        fErrors = null;
        fValidationContext = null;
    }
}
