package org.apache.xerces.dom3.as;
public interface ASNotationDeclaration extends ASObject {
    public String getSystemId();
    public void setSystemId(String systemId);
    public String getPublicId();
    public void setPublicId(String publicId);
}
