package org.apache.batik.css.parser;
public class DefaultPseudoClassCondition extends AbstractAttributeCondition {
    protected String namespaceURI;
    public DefaultPseudoClassCondition(String namespaceURI, String value) {
        super(value);
        this.namespaceURI = namespaceURI;
    }
    public short getConditionType() {
        return SAC_PSEUDO_CLASS_CONDITION;
    }
    public String getNamespaceURI() {
        return namespaceURI;
    }
    public String getLocalName() {
        return null;
    }
    public boolean getSpecified() {
        return false;
    }
    public String toString() {
        return ":" + getValue();
    }
}
