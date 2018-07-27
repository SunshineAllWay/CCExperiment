package org.apache.batik.css.parser;
public class DefaultClassCondition extends DefaultAttributeCondition {
    public DefaultClassCondition(String namespaceURI,
                                 String value) {
        super("class", namespaceURI, true, value);
    }
    public short getConditionType() {
        return SAC_CLASS_CONDITION;
    }
    public String toString() {
        return "." + getValue();
    }
}
