package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.taskdefs.condition.ConditionBase;
public class ConditionTask extends ConditionBase {
    private String property = null;
    private Object value = "true";
    private Object alternative = null;
    public ConditionTask() {
        super("condition");
    }
    public void setProperty(String p) {
        property = p;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    public void setValue(String v) {
        setValue((Object) v);
    }
    public void setElse(Object alt) {
        alternative = alt;
    }
    public void setElse(String e) {
        setElse((Object) e);
    }
    public void execute() throws BuildException {
        if (countConditions() > 1) {
            throw new BuildException("You must not nest more than one condition into <"
                    + getTaskName() + ">");
        }
        if (countConditions() < 1) {
            throw new BuildException("You must nest a condition into <" + getTaskName() + ">");
        }
        if (property == null) {
            throw new BuildException("The property attribute is required.");
        }
        Condition c = (Condition) getConditions().nextElement();
        if (c.eval()) {
            log("Condition true; setting " + property + " to " + value, Project.MSG_DEBUG);
            PropertyHelper.getPropertyHelper(getProject()).setNewProperty(property, value);
        } else if (alternative != null) {
            log("Condition false; setting " + property + " to " + alternative, Project.MSG_DEBUG);
            PropertyHelper.getPropertyHelper(getProject()).setNewProperty(property, alternative);
        } else {
            log("Condition false; not setting " + property, Project.MSG_DEBUG);
        }
    }
}
