package org.apache.xerces.xs;
public interface XSFacet extends XSObject {
    public short getFacetKind();
    public String getLexicalFacetValue();
    public int getIntFacetValue();
    public Object getActualFacetValue();
    public boolean getFixed();
    public XSAnnotation getAnnotation();
    public XSObjectList getAnnotations();    
}
