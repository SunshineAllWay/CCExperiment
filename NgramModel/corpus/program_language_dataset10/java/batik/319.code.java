package org.apache.batik.css.engine.sac;
import org.w3c.dom.Element;
public class CSSPseudoElementSelector extends AbstractElementSelector {
    public CSSPseudoElementSelector(String uri, String name) {
        super(uri, name);
    }
    public short getSelectorType() {
        return SAC_PSEUDO_ELEMENT_SELECTOR;
    }
    public boolean match(Element e, String pseudoE) {
        return getLocalName().equalsIgnoreCase(pseudoE);
    }
    public int getSpecificity() {
        return 0;
    }
    public String toString() {
        return ":" + getLocalName();
    }
}
