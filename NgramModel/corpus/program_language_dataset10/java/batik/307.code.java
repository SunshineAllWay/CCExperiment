package org.apache.batik.css.engine.sac;
import org.w3c.dom.Element;
public class CSSBeginHyphenAttributeCondition
    extends CSSAttributeCondition {
    public CSSBeginHyphenAttributeCondition(String localName,
                                              String namespaceURI,
                                              boolean specified,
                                              String value) {
        super(localName, namespaceURI, specified, value);
    }
    public short getConditionType() {
        return SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION;
    }
    public boolean match(Element e, String pseudoE) {
        return e.getAttribute(getLocalName()).startsWith(getValue());
    }
    public String toString() {
        return '[' + getLocalName() + "|=\"" + getValue() + "\"]";
    }
}
