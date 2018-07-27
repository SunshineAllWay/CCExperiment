package org.apache.tools.ant.taskdefs.condition;
import org.apache.tools.ant.BuildException;
import java.util.Enumeration;
public class Xor extends ConditionBase implements Condition {
    public boolean eval() throws BuildException {
        Enumeration e = getConditions();
        boolean state = false;
        while (e.hasMoreElements()) {
            Condition c = (Condition) e.nextElement();
            state ^= c.eval();
        }
        return state;
    }
}
