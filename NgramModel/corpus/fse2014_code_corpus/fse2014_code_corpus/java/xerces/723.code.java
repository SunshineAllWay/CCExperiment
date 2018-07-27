package org.apache.xerces.xs;
public interface XSParticle extends XSObject {
    public int getMinOccurs();
    public int getMaxOccurs();
    public boolean getMaxOccursUnbounded();
    public XSTerm getTerm();
    public XSObjectList getAnnotations();    
}
