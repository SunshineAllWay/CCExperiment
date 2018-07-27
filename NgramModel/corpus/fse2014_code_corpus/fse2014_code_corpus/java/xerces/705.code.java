package org.apache.xerces.xs;
public interface XSComplexTypeDefinition extends XSTypeDefinition {
    public static final short CONTENTTYPE_EMPTY         = 0;
    public static final short CONTENTTYPE_SIMPLE        = 1;
    public static final short CONTENTTYPE_ELEMENT       = 2;
    public static final short CONTENTTYPE_MIXED         = 3;
    public short getDerivationMethod();
    public boolean getAbstract();
    public XSObjectList getAttributeUses();
    public XSWildcard getAttributeWildcard();
    public short getContentType();
    public XSSimpleTypeDefinition getSimpleType();
    public XSParticle getParticle();
    public boolean isProhibitedSubstitution(short restriction);
    public short getProhibitedSubstitutions();
    public XSObjectList getAnnotations();
}
