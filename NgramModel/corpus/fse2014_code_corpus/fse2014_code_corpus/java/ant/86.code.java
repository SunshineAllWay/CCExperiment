package org.apache.tools.ant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class TaskConfigurationChecker {
    private List errors = new ArrayList();
    private final Task task;
    public TaskConfigurationChecker(Task task) {
        this.task = task;
    }
    public void assertConfig(boolean condition, String errormessage) {
        if (!condition) {
            errors.add(errormessage);
        }
    }
    public void fail(String errormessage) {
        errors.add(errormessage);
    }
    public void checkErrors() throws BuildException {
        if (!errors.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            sb.append("Configurationerror on <");
            sb.append(task.getTaskName());
            sb.append(">:");
            sb.append(System.getProperty("line.separator"));
            for (Iterator it = errors.iterator(); it.hasNext();) {
                String msg = (String) it.next();
                sb.append("- ");
                sb.append(msg);
                sb.append(System.getProperty("line.separator"));
            }
            throw new BuildException(sb.toString(), task.getLocation());
        }
    }
}
