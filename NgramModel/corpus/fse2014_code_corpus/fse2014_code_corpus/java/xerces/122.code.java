package org.apache.wml;
import org.w3c.dom.Element;
public interface WMLElement extends Element {
    public void setId(String newValue);
    public String getId();
    public void setClassName(String newValue);
    public String getClassName();
}
