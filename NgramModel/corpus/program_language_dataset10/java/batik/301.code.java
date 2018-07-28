package org.apache.batik.css.engine.sac;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
public abstract class AbstractCombinatorCondition
    implements CombinatorCondition,
               ExtendedCondition {
    protected Condition firstCondition;
    protected Condition secondCondition;
    protected AbstractCombinatorCondition(Condition c1, Condition c2) {
        firstCondition = c1;
        secondCondition = c2;
    }
    public boolean equals(Object obj) {
        if (obj == null || (obj.getClass() != getClass())) {
            return false;
        }
        AbstractCombinatorCondition c = (AbstractCombinatorCondition)obj;
        return (c.firstCondition.equals(firstCondition) &&
                c.secondCondition.equals(secondCondition));
    }
    public int getSpecificity() {
        return ((ExtendedCondition)getFirstCondition()).getSpecificity() +
               ((ExtendedCondition)getSecondCondition()).getSpecificity();
    }
    public Condition getFirstCondition() {
        return firstCondition;
    }
    public Condition getSecondCondition() {
        return secondCondition;
    }
}
