package org.apache.xerces.xs;
public interface XSAttributeDeclaration extends XSObject {
    public XSSimpleTypeDefinition getTypeDefinition();
    public short getScope();
    public XSComplexTypeDefinition getEnclosingCTDefinition();
    public short getConstraintType();
    public String getConstraintValue();
    public Object getActualVC()
                                                        throws XSException;
    public short getActualVCType()
                                                        throws XSException;
    public ShortList getItemValueTypes()
                                                        throws XSException;
    public XSValue getValueConstraintValue();
    public XSAnnotation getAnnotation();
    public XSObjectList getAnnotations();    
}
