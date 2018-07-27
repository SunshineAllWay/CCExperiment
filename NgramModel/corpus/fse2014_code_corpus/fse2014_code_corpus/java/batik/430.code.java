package org.apache.batik.css.parser;
public class DefaultPseudoElementSelector extends AbstractElementSelector {
    public DefaultPseudoElementSelector(String uri, String name) {
        super(uri, name);
    }
    public short getSelectorType() {
        return SAC_PSEUDO_ELEMENT_SELECTOR;
    }
    public String toString() {
        return ":" + getLocalName();
    }
}
