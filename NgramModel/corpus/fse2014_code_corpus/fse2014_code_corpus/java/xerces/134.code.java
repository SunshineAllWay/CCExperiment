package org.apache.wml;
public interface WMLOptionElement extends WMLElement {
    public void setValue(String newValue);
    public String getValue();
    public void setTitle(String newValue);
    public String getTitle();
    public void setOnPick(String href);
    public String getOnPick();
    public void setXmlLang(String newValue);
    public String getXmlLang();
}
