package org.apache.batik.css.parser;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
public abstract class AbstractCombinatorCondition
    implements CombinatorCondition {
    protected Condition firstCondition;
    protected Condition secondCondition;
    protected AbstractCombinatorCondition(Condition c1, Condition c2) {
        firstCondition = c1;
        secondCondition = c2;
    }
    public Condition getFirstCondition() {
        return firstCondition;
    }
    public Condition getSecondCondition() {
        return secondCondition;
    }
}
