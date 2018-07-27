package org.apache.wml;
public interface WMLMetaElement extends WMLElement {
    public void setName(String newValue);
    public String getName();
    public void setHttpEquiv(String newValue);
    public String getHttpEquiv();
    public void setForua(boolean newValue);
    public boolean getForua();
    public void setScheme(String newValue);
    public String getScheme();
    public void setContent(String newValue);
    public String getContent();
}
