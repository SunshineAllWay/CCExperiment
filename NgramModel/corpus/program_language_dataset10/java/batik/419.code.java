package org.apache.batik.css.parser;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.SimpleSelector;
public class DefaultConditionalSelector implements ConditionalSelector {
    protected SimpleSelector simpleSelector;
    protected Condition condition;
    public DefaultConditionalSelector(SimpleSelector s, Condition c) {
        simpleSelector = s;
        condition      = c;
    }
    public short getSelectorType() {
        return SAC_CONDITIONAL_SELECTOR;
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
