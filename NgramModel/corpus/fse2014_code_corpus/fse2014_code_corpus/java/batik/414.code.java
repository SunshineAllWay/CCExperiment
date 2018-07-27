package org.apache.batik.css.parser;
import org.w3c.css.sac.Condition;
public class DefaultAndCondition extends AbstractCombinatorCondition {
    public DefaultAndCondition(Condition c1, Condition c2) {
        super(c1, c2);
    }
    public short getConditionType() {
        return SAC_AND_CONDITION;
    }
    public String toString() {
        return String.valueOf( getFirstCondition() ) + getSecondCondition();
    }
}
