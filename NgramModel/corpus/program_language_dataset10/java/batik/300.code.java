package org.apache.batik.css.engine.sac;
import org.w3c.css.sac.AttributeCondition;
public abstract class AbstractAttributeCondition
    implements AttributeCondition,
               ExtendedCondition {
    protected String value;
    protected AbstractAttributeCondition(String value) {
        this.value = value;
    }
    public boolean equals(Object obj) {
        if (obj == null || (obj.getClass() != getClass())) {
            return false;
        }
        AbstractAttributeCondition c = (AbstractAttributeCondition)obj;
        return c.value.equals(value);
    }
    public int hashCode() {
        return value == null ? -1 : value.hashCode();
    }
    public int getSpecificity() {
        return 1 << 8;
    }
    public String getValue() {
        return value;
    }
}
