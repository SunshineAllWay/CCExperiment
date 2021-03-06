package org.apache.batik.css.engine.sac;
import java.util.Set;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public class CSSChildSelector extends AbstractDescendantSelector {
    public CSSChildSelector(Selector ancestor, SimpleSelector simple) {
        super(ancestor, simple);
    }
    public short getSelectorType() {
        return SAC_CHILD_SELECTOR;
    }
    public boolean match(Element e, String pseudoE) {
        Node n = e.getParentNode();
        if (n != null && n.getNodeType() == Node.ELEMENT_NODE) {
            return ((ExtendedSelector)getAncestorSelector()).match((Element)n,
                                                                   null) &&
                   ((ExtendedSelector)getSimpleSelector()).match(e, pseudoE);
        }
        return false;
    }
    public void fillAttributeSet(Set attrSet) {
        ((ExtendedSelector)getAncestorSelector()).fillAttributeSet(attrSet);
        ((ExtendedSelector)getSimpleSelector()).fillAttributeSet(attrSet);
    }
    public String toString() {
        SimpleSelector s = getSimpleSelector();
        if (s.getSelectorType() == SAC_PSEUDO_ELEMENT_SELECTOR) {
            return String.valueOf( getAncestorSelector() ) + s;
        }
        return getAncestorSelector() + " > " + s;
    }
}
