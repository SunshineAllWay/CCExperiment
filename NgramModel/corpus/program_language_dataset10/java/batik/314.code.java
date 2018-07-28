package org.apache.batik.css.engine.sac;
import org.w3c.dom.Element;
public class CSSElementSelector extends AbstractElementSelector {
    public CSSElementSelector(String uri, String name) {
        super(uri, name);
    }
    public short getSelectorType() {
        return SAC_ELEMENT_NODE_SELECTOR;
    }
    public boolean match(Element e, String pseudoE) {
        String name = getLocalName();
        if (name == null) {
            return true;
        }
        String eName;
        if (e.getPrefix() == null) eName = e.getNodeName();
        else                       eName = e.getLocalName();
        return eName.equals(name);
    }
    public int getSpecificity() {
        return (getLocalName() == null) ? 0 : 1;
    }
    public String toString() {
        String name = getLocalName();
        if (name == null) {
            return "*";
        }
        return name;
    }
}
