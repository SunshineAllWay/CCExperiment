package org.apache.wml;
public interface WMLTemplateElement extends WMLElement {
    public void setOnTimer(String newValue);
    public String getOnTimer();
    public void setOnEnterBackward(String newValue);
    public String getOnEnterBackward();
    public void setOnEnterForward(String newValue);
    public String getOnEnterForward();
}
