package org.apache.batik.css.parser;
public class DefaultIdCondition extends AbstractAttributeCondition {
    public DefaultIdCondition(String value) {
        super(value);
    }
    public short getConditionType() {
        return SAC_ID_CONDITION;
    }
    public String getNamespaceURI() {
        return null;
    }
    public String getLocalName() {
        return "id";
    }
    public boolean getSpecified() {
        return true;
    }
    public String toString() {
        return "#" + getValue();
    }
}
