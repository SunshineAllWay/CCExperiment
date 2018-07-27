package org.apache.wml;
public interface WMLInputElement extends WMLElement {
    public void setName(String newValue);
    public String getName();
    public void setValue(String newValue);
    public String getValue();
    public void setType(String newValue);
    public String getType();
    public void setFormat(String newValue);
    public String getFormat();
    public void setEmptyOk(boolean newValue);
    public boolean getEmptyOk();
    public void setSize(int newValue);
    public int getSize();
    public void setMaxLength(int newValue);
    public int getMaxLength();
    public void setTitle(String newValue);
    public String getTitle();
    public void setTabIndex(int newValue);
    public int getTabIndex();
    public void setXmlLang(String newValue);
    public String getXmlLang();
}
