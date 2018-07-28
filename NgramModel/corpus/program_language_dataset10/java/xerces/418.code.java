package org.apache.xerces.impl.xs;
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.xs.util.StringListImpl;
import org.apache.xerces.xs.ElementPSVI;
import org.apache.xerces.xs.ShortList;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSNotationDeclaration;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSValue;
public class ElementPSVImpl implements ElementPSVI {
    protected XSElementDeclaration fDeclaration = null;
    protected XSTypeDefinition fTypeDecl = null;
    protected boolean fNil = false;
    protected boolean fSpecified = false;
    protected ValidatedInfo fValue = new ValidatedInfo();
    protected XSNotationDeclaration fNotation = null;
    protected short fValidationAttempted = ElementPSVI.VALIDATION_NONE;
    protected short fValidity = ElementPSVI.VALIDITY_NOTKNOWN;
    protected String[] fErrors = null;
    protected String fValidationContext = null;
    protected SchemaGrammar[] fGrammars = null;
    protected XSModel fSchemaInformation = null;
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
    public boolean getNil() {
        return fNil;
    }
    public XSNotationDeclaration getNotation() {
        return fNotation;
    }
    public XSTypeDefinition getTypeDefinition() {
        return fTypeDecl;
    }
    public XSSimpleTypeDefinition getMemberTypeDefinition() {
        return fValue.getMemberTypeDefinition();
    }
    public XSElementDeclaration getElementDeclaration() {
        return fDeclaration;
    }
    public synchronized XSModel getSchemaInformation() {
        if (fSchemaInformation == null && fGrammars != null) {
            fSchemaInformation = new XSModelImpl(fGrammars);
        }
        return fSchemaInformation;
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
        fDeclaration = null;
        fTypeDecl = null;
        fNil = false;
        fSpecified = false;
        fNotation = null;
        fValidationAttempted = ElementPSVI.VALIDATION_NONE;
        fValidity = ElementPSVI.VALIDITY_NOTKNOWN;
        fErrors = null;
        fValidationContext = null;
        fValue.reset();
    }
    public void copySchemaInformationTo(ElementPSVImpl target) {
        target.fGrammars = fGrammars;
        target.fSchemaInformation = fSchemaInformation;
    }
}
