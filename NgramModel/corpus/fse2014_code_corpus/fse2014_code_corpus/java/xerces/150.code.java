package org.apache.wml.dom;
import org.apache.wml.WMLAccessElement;
public class WMLAccessElementImpl extends WMLElementImpl implements WMLAccessElement {
    private static final long serialVersionUID = -235627181817012806L;
    public WMLAccessElementImpl (WMLDocumentImpl owner, String tagName) {
        super( owner, tagName);
    }
    public void setClassName(String newValue) {
        setAttribute("class", newValue);
    }
    public String getClassName() {
        return getAttribute("class");
    }
    public void setDomain(String newValue) {
        setAttribute("domain", newValue);
    }
    public String getDomain() {
        return getAttribute("domain");
    }
    public void setId(String newValue) {
        setAttribute("id", newValue);
    }
    public String getId() {
        return getAttribute("id");
    }
    public void setPath(String newValue) {
        setAttribute("path", newValue);
    }
    public String getPath() {
        return getAttribute("path");
    }
}
