package org.apache.xerces.impl;
import org.apache.xerces.xni.XMLResourceIdentifier;
public interface XMLEntityDescription extends XMLResourceIdentifier {
    public void setEntityName(String name);
    public String getEntityName();
} 
