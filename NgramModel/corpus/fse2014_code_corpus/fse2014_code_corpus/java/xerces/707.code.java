package org.apache.xerces.xs;
public interface XSElementDeclaration extends XSTerm {
    public XSTypeDefinition getTypeDefinition();
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
    public boolean getNillable();
    public XSNamedMap getIdentityConstraints();
    public XSElementDeclaration getSubstitutionGroupAffiliation();
    public boolean isSubstitutionGroupExclusion(short exclusion);
    public short getSubstitutionGroupExclusions();
    public boolean isDisallowedSubstitution(short disallowed);
    public short getDisallowedSubstitutions();
    public boolean getAbstract();
    public XSAnnotation getAnnotation();
    public XSObjectList getAnnotations();    
}
