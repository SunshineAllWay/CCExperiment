package org.apache.wml;
public interface WMLCardElement extends WMLElement {
    public void setOnEnterBackward(String href);
    public String getOnEnterBackward();
    public void setOnEnterForward(String href);
    public String getOnEnterForward();
    public void setOnTimer(String href);
    public String getOnTimer();
    public void setTitle(String newValue);
    public String getTitle();
    public void setNewContext(boolean newValue);
    public boolean getNewContext();
    public void setOrdered(boolean newValue);
    public boolean getOrdered();
    public void setXmlLang(String newValue);
    public String getXmlLang();
}
