package org.apache.wml;
public interface WMLDoElement extends WMLElement {
    public void setOptional(String newValue);
    public String getOptional();
    public void setLabel(String newValue);
    public String getLabel();
    public void setType(String newValue);
    public String getType();
    public void setName(String newValue);
    public String getName();
    public void setXmlLang(String newValue);
    public String getXmlLang();
}
