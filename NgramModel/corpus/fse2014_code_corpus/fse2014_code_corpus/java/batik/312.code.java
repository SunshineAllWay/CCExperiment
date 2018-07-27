package org.apache.batik.css.engine.sac;
import java.util.Set;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public class CSSDescendantSelector extends AbstractDescendantSelector {
    public CSSDescendantSelector(Selector ancestor, SimpleSelector simple) {
        super(ancestor, simple);
    }
    public short getSelectorType() {
        return SAC_DESCENDANT_SELECTOR;
    }
    public boolean match(Element e, String pseudoE) {
        ExtendedSelector p = (ExtendedSelector)getAncestorSelector();
        if (!((ExtendedSelector)getSimpleSelector()).match(e,pseudoE))
            return false;
        for (Node n = e.getParentNode(); n != null; n = n.getParentNode()) {
            if (n.getNodeType() == Node.ELEMENT_NODE &&
                p.match((Element)n, null)) {
                return true;
            }
        }
        return false;
    }
    public void fillAttributeSet(Set attrSet) {
        ((ExtendedSelector)getSimpleSelector()).fillAttributeSet(attrSet);
    }
    public String toString() {
        return getAncestorSelector() + " " + getSimpleSelector();
    }
}
