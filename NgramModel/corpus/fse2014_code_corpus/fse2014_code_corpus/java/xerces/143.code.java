package org.apache.wml;
public interface WMLTableElement extends WMLElement {
    public void setTitle(String newValue);
    public String getTitle();
    public void setAlign(String newValue);
    public String getAlign();
    public void setColumns(int newValue);
    public int getColumns();
    public void setXmlLang(String newValue);
    public String getXmlLang();
}
