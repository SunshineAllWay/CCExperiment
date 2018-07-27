package org.apache.batik.css.parser;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SimpleSelector;
public class DefaultDescendantSelector extends AbstractDescendantSelector {
    public DefaultDescendantSelector(Selector ancestor,
                                     SimpleSelector simple) {
        super(ancestor, simple);
    }
    public short getSelectorType() {
        return SAC_DESCENDANT_SELECTOR;
    }
    public String toString() {
        return getAncestorSelector() + " " + getSimpleSelector();
    }
}
