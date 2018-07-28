package org.apache.xerces.xs;
public interface XSModelGroup extends XSTerm {
    public static final short COMPOSITOR_SEQUENCE       = 1;
    public static final short COMPOSITOR_CHOICE         = 2;
    public static final short COMPOSITOR_ALL            = 3;
    public short getCompositor();
    public XSObjectList getParticles();
    public XSAnnotation getAnnotation();
    public XSObjectList getAnnotations();    
}
