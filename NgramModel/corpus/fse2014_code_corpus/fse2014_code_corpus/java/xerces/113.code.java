package org.apache.wml;
public interface WMLAElement extends WMLElement {
    public void setHref(String newValue);
    public String getHref();
    public void setTitle(String newValue);
    public String getTitle();
    public void setId(String newValue);
    public String getId();
    public void setXmlLang(String newValue);
    public String getXmlLang();
}
