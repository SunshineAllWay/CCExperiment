package org.apache.batik.css.engine.sac;
import java.util.Set;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public class CSSDirectAdjacentSelector extends AbstractSiblingSelector {
    public CSSDirectAdjacentSelector(short type,
                                     Selector parent,
                                     SimpleSelector simple) {
        super(type, parent, simple);
    }
    public short getSelectorType() {
        return SAC_DIRECT_ADJACENT_SELECTOR;
    }
    public boolean match(Element e, String pseudoE) {
        Node n = e;
        if (!((ExtendedSelector)getSiblingSelector()).match(e, pseudoE))
            return false;
        while ((n = n.getPreviousSibling()) != null &&
               n.getNodeType() != Node.ELEMENT_NODE);
        if (n == null) 
            return false;
        return ((ExtendedSelector)getSelector()).match((Element)n, null);
    }
    public void fillAttributeSet(Set attrSet) {
        ((ExtendedSelector)getSelector()).fillAttributeSet(attrSet);
        ((ExtendedSelector)getSiblingSelector()).fillAttributeSet(attrSet);
    }
    public String toString() {
        return getSelector() + " + " + getSiblingSelector();
    }
}
