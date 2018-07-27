package org.apache.xerces.xs;
public interface ItemPSVI {
    public static final short VALIDITY_NOTKNOWN         = 0;
    public static final short VALIDITY_INVALID          = 1;
    public static final short VALIDITY_VALID            = 2;
    public static final short VALIDATION_NONE           = 0;
    public static final short VALIDATION_PARTIAL        = 1;
    public static final short VALIDATION_FULL           = 2;
    public String getValidationContext();
    public short getValidity();
    public short getValidationAttempted();
    public StringList getErrorCodes();
    public StringList getErrorMessages();
    public String getSchemaNormalizedValue();
    public Object getActualNormalizedValue()
                                   throws XSException;
    public short getActualNormalizedValueType()
                                   throws XSException;
    public ShortList getItemValueTypes()
                                   throws XSException;
    public XSValue getSchemaValue();
    public XSTypeDefinition getTypeDefinition();
    public XSSimpleTypeDefinition getMemberTypeDefinition();
    public String getSchemaDefault();
    public boolean getIsSchemaSpecified();
}
