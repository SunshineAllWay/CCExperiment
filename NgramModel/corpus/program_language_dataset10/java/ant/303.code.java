package org.apache.tools.ant.taskdefs.condition;
import java.util.Enumeration;
import org.apache.tools.ant.BuildException;
public class And extends ConditionBase implements Condition {
    public boolean eval() throws BuildException {
        Enumeration e = getConditions();
        while (e.hasMoreElements()) {
            Condition c = (Condition) e.nextElement();
            if (!c.eval()) {
                return false;
            }
        }
        return true;
    }
}