package org.apache.batik.css.engine.sac;
import java.util.Set;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.Element;
public class CSSConditionalSelector
    implements ConditionalSelector,
               ExtendedSelector {
    protected SimpleSelector simpleSelector;
    protected Condition condition;
    public CSSConditionalSelector(SimpleSelector s, Condition c) {
        simpleSelector = s;
        condition      = c;
    }
    public boolean equals(Object obj) {
        if (obj == null || (obj.getClass() != getClass())) {
            return false;
        }
        CSSConditionalSelector s = (CSSConditionalSelector)obj;
        return (s.simpleSelector.equals(simpleSelector) &&
                s.condition.equals(condition));
    }
    public short getSelectorType() {
        return SAC_CONDITIONAL_SELECTOR;
    }
    public boolean match(Element e, String pseudoE) {
        return ((ExtendedSelector)getSimpleSelector()).match(e, pseudoE) &&
               ((ExtendedCondition)getCondition()).match(e, pseudoE);
    }
    public void fillAttributeSet(Set attrSet) {
        ((ExtendedSelector)getSimpleSelector()).fillAttributeSet(attrSet);
        ((ExtendedCondition)getCondition()).fillAttributeSet(attrSet);
    }
    public int getSpecificity() {
        return ((ExtendedSelector)getSimpleSelector()).getSpecificity() +
               ((ExtendedCondition)getCondition()).getSpecificity();
    }
    public SimpleSelector getSimpleSelector() {
        return simpleSelector;
    }
    public Condition getCondition() {
        return condition;
    }
    public String toString() {
        return String.valueOf( simpleSelector ) + condition;
    }
}
