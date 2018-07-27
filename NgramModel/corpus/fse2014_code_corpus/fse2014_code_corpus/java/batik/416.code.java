package org.apache.batik.css.parser;
public class DefaultBeginHyphenAttributeCondition
    extends DefaultAttributeCondition {
    public DefaultBeginHyphenAttributeCondition(String localName,
                                                String namespaceURI,
                                                boolean specified,
                                                String value) {
        super(localName, namespaceURI, specified, value);
    }
    public short getConditionType() {
        return SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION;
    }
    public String toString() {
        return "[" + getLocalName() + "|=\"" + getValue() + "\"]";
    }
}
