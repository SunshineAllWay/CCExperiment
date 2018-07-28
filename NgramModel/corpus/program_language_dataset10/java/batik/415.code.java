package org.apache.batik.css.parser;
public class DefaultAttributeCondition extends AbstractAttributeCondition {
    protected String localName;
    protected String namespaceURI;
    protected boolean specified;
    public DefaultAttributeCondition(String localName,
                                     String namespaceURI,
                                     boolean specified,
                                     String value) {
        super(value);
        this.localName = localName;
        this.namespaceURI = namespaceURI;
        this.specified = specified;
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
    public String toString() {
        if (value == null) {
            return "[" + localName + "]";
        }
        return "[" + localName + "=\"" + value + "\"]";
    }
}
