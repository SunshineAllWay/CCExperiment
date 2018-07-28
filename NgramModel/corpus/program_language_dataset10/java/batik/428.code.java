package org.apache.batik.css.parser;
public class DefaultOneOfAttributeCondition extends DefaultAttributeCondition {
    public DefaultOneOfAttributeCondition(String localName,
                                          String namespaceURI,
                                          boolean specified,
                                          String value) {
        super(localName, namespaceURI, specified, value);
    }
    public short getConditionType() {
        return SAC_ONE_OF_ATTRIBUTE_CONDITION;
    }
    public String toString() {
        return "[" + getLocalName() + "~=\"" + getValue() + "\"]";
    }
}
