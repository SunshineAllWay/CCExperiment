package org.apache.batik.css.engine.sac;
import org.w3c.dom.Element;
public class CSSOneOfAttributeCondition extends CSSAttributeCondition {
    public CSSOneOfAttributeCondition(String localName,
                                      String namespaceURI,
                                      boolean specified,
                                      String value) {
        super(localName, namespaceURI, specified, value);
    }
    public short getConditionType() {
        return SAC_ONE_OF_ATTRIBUTE_CONDITION;
    }
    public boolean match(Element e, String pseudoE) {
        String attr = e.getAttribute(getLocalName());
        String val = getValue();
        int i = attr.indexOf(val);
        if (i == -1) {
            return false;
        }
        if (i != 0 && !Character.isSpaceChar(attr.charAt(i - 1))) {
            return false;
        }
        int j = i + val.length();
        return (j == attr.length() ||
                (j < attr.length() && Character.isSpaceChar(attr.charAt(j))));
    }
    public String toString() {
        return "[" + getLocalName() + "~=\"" + getValue() + "\"]";
    }
}
