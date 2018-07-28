package org.apache.xerces.xni;
public interface XMLResourceIdentifier {
    public void setPublicId(String publicId);
    public String getPublicId();
    public void setExpandedSystemId(String systemId);
    public String getExpandedSystemId();
    public void setLiteralSystemId(String systemId);
    public String getLiteralSystemId();
    public void setBaseSystemId(String systemId); 
    public String getBaseSystemId();
    public void setNamespace(String namespace);
    public String getNamespace();
} 
