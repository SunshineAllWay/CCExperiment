package org.apache.batik.css.parser;
public class DefaultElementSelector extends AbstractElementSelector {
    public DefaultElementSelector(String uri, String name) {
        super(uri, name);
    }
    public short getSelectorType() {
        return SAC_ELEMENT_NODE_SELECTOR;
    }
    public String toString() {
        String name = getLocalName();
        if (name == null) {
            return "*";
        }
        return name;
    }
}
