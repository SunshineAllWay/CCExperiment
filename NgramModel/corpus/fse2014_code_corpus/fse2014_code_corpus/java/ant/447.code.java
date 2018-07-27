package org.apache.tools.ant.taskdefs.optional.j2ee;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Commandline;
public class GenericHotDeploymentTool extends AbstractHotDeploymentTool {
    private Java java;
    private String className;
    private static final String[] VALID_ACTIONS = {ACTION_DEPLOY};
    public Commandline.Argument createArg() {
        return java.createArg();
    }
    public Commandline.Argument createJvmarg() {
        return java.createJvmarg();
    }
    protected boolean isActionValid() {
        return (getTask().getAction().equals(VALID_ACTIONS[0]));
    }
    public void setTask(ServerDeploy task) {
        super.setTask(task);
        java = new Java(task);
    }
    public void deploy() throws BuildException {
        java.setClassname(className);
        java.setClasspath(getClasspath());
        java.setFork(true);
        java.setFailonerror(true);
        java.execute();
    }
    public void validateAttributes() throws BuildException {
        super.validateAttributes();
        if (className == null) {
            throw new BuildException("The classname attribute must be set");
        }
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public Java getJava() {
        return java;
    }
    public String getClassName() {
        return className;
    }
}
