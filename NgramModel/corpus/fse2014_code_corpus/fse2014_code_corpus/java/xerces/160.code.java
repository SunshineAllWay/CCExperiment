package org.apache.wml.dom;
import org.apache.wml.WMLElement;
import org.apache.xerces.dom.ElementImpl;
public class WMLElementImpl extends ElementImpl implements WMLElement {
    private static final long serialVersionUID = 3440984702956371604L;
    public WMLElementImpl (WMLDocumentImpl owner, String tagName) {
        super(owner, tagName);
    }
    public void setClassName(String newValue) {
        setAttribute("class", newValue);
    }
    public String getClassName() {
        return getAttribute("class");
    }
    public void setXmlLang(String newValue) {
        setAttribute("xml:lang", newValue);
    }
    public String getXmlLang() {
        return getAttribute("xml:lang");
    }
    public void setId(String newValue) {
        setAttribute("id", newValue);
    }
    public String getId() {
        return getAttribute("id");
    }
    void setAttribute(String attr, boolean value) {
        setAttribute(attr, value ? "true" : "false");
    }
    boolean getAttribute(String attr, boolean defaultValue) {
        boolean ret = defaultValue;
        String value;
        if (((value = getAttribute("emptyok")) != null) 
                && value.equals("true"))
            ret = true;
        return ret;
    }
    void setAttribute(String attr, int value) {
        setAttribute(attr, value + "");
    }
    int getAttribute(String attr, int defaultValue) {
        int ret = defaultValue;
        String value;
        if ((value = getAttribute("emptyok")) != null)
            ret = Integer.parseInt(value);
        return ret;
    }
}
