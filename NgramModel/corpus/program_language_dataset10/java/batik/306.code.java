package org.apache.batik.css.engine.sac;
import java.util.Set;
import org.w3c.dom.Element;
public class CSSAttributeCondition extends AbstractAttributeCondition {
    protected String localName;
    protected String namespaceURI;
    protected boolean specified;
    public CSSAttributeCondition(String localName,
                                 String namespaceURI,
                                 boolean specified,
                                 String value) {
        super(value);
        this.localName = localName;
        this.namespaceURI = namespaceURI;
        this.specified = specified;
    }
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        CSSAttributeCondition c = (CSSAttributeCondition)obj;
        return (c.namespaceURI.equals(namespaceURI) &&
                c.localName.equals(localName)       &&
                c.specified == specified);
    }
    public int hashCode() {
        return namespaceURI.hashCode()
                ^ localName.hashCode()
                ^ (specified ? -1 : 0);
    }
    public short getConditionType() {
        return SAC_ATTRIBUTE_CONDITION;
    }
    public String getNamespaceURI() {
        return namespaceURI;
    }
    public String getLocalName() {
        return localName;
    }
    public boolean getSpecified() {
        return specified;
    }
    public boolean match(Element e, String pseudoE) {
        String val = getValue();
        if (val == null) {
            return !e.getAttribute(getLocalName()).equals("");
        }
        return e.getAttribute(getLocalName()).equals(val);
    }
    public void fillAttributeSet(Set attrSet) {
        attrSet.add(localName);
    }
    public String toString() {
        if (value == null) {
            return '[' + localName + ']';
        }
        return '[' + localName + "=\"" + value + "\"]";
    }
}
