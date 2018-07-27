package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ExitStatusException;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.taskdefs.condition.ConditionBase;
public class Exit extends Task {
    private static class NestedCondition extends ConditionBase implements Condition {
        public boolean eval() {
            if (countConditions() != 1) {
                throw new BuildException(
                    "A single nested condition is required.");
            }
            return ((Condition) (getConditions().nextElement())).eval();
        }
    }
    private String message;
    private Object ifCondition, unlessCondition;
    private NestedCondition nestedCondition;
    private Integer status;
    public void setMessage(String value) {
        this.message = value;
    }
    public void setIf(Object c) {
        ifCondition = c;
    }
    public void setIf(String c) {
        setIf((Object) c);
    }
    public void setUnless(Object c) {
        unlessCondition = c;
    }
    public void setUnless(String c) {
        setUnless((Object) c);
    }
    public void setStatus(int i) {
        status = new Integer(i);
    }
    public void execute() throws BuildException {
        boolean fail = (nestedConditionPresent()) ? testNestedCondition()
                     : (testIfCondition() && testUnlessCondition());
        if (fail) {
            String text = null;
            if (message != null && message.trim().length() > 0) {
                text = message.trim();
            } else {
                if (ifCondition != null && !"".equals(ifCondition)
                    && testIfCondition()) {
                    text = "if=" + ifCondition;
                }
                if (unlessCondition != null && !"".equals(unlessCondition)
                    && testUnlessCondition()) {
                    if (text == null) {
                        text = "";
                    } else {
                        text += " and ";
                    }
                    text += "unless=" + unlessCondition;
                }
                if (nestedConditionPresent()) {
                    text = "condition satisfied";
                } else {
                    if (text == null) {
                        text = "No message";
                    }
                }
            }
            log("failing due to " + text, Project.MSG_DEBUG);
            throw ((status == null) ? new BuildException(text)
             : new ExitStatusException(text, status.intValue()));
        }
    }
    public void addText(String msg) {
        if (message == null) {
            message = "";
        }
        message += getProject().replaceProperties(msg);
    }
    public ConditionBase createCondition() {
        if (nestedCondition != null) {
            throw new BuildException("Only one nested condition is allowed.");
        }
        nestedCondition = new NestedCondition();
        return nestedCondition;
    }
    private boolean testIfCondition() {
        return PropertyHelper.getPropertyHelper(getProject())
            .testIfCondition(ifCondition);
    }
    private boolean testUnlessCondition() {
        return PropertyHelper.getPropertyHelper(getProject())
            .testUnlessCondition(unlessCondition);
    }
    private boolean testNestedCondition() {
        boolean result = nestedConditionPresent();
        if (result && ifCondition != null || unlessCondition != null) {
            throw new BuildException("Nested conditions "
                + "not permitted in conjunction with if/unless attributes");
        }
        return result && nestedCondition.eval();
    }
    private boolean nestedConditionPresent() {
        return (nestedCondition != null);
    }
}
