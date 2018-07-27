package org.apache.tools.ant.taskdefs.optional.ccm;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
public class CCMReconfigure extends Continuus {
    private String ccmProject = null;
    private boolean recurse = false;
    private boolean verbose = false;
    public CCMReconfigure() {
        super();
        setCcmAction(COMMAND_RECONFIGURE);
    }
    public void execute() throws BuildException {
        Commandline commandLine = new Commandline();
        int result = 0;
        commandLine.setExecutable(getCcmCommand());
        commandLine.createArgument().setValue(getCcmAction());
        checkOptions(commandLine);
        result = run(commandLine);
        if (Execute.isFailure(result)) {
            String msg = "Failed executing: " + commandLine.toString();
            throw new BuildException(msg, getLocation());
        }
    }
    private void checkOptions(Commandline cmd) {
        if (isRecurse()) {
            cmd.createArgument().setValue(FLAG_RECURSE);
        } 
        if (isVerbose()) {
            cmd.createArgument().setValue(FLAG_VERBOSE);
        } 
        if (getCcmProject() != null) {
            cmd.createArgument().setValue(FLAG_PROJECT);
            cmd.createArgument().setValue(getCcmProject());
        }
    }
    public String getCcmProject() {
        return ccmProject;
    }
    public void setCcmProject(String v) {
        this.ccmProject = v;
    }
    public boolean isRecurse() {
        return recurse;
    }
    public void setRecurse(boolean v) {
        this.recurse = v;
    }
    public boolean isVerbose() {
        return verbose;
    }
    public void setVerbose(boolean v) {
        this.verbose = v;
    }
    public static final String FLAG_RECURSE = "/recurse";
    public static final String FLAG_VERBOSE = "/verbose";
    public static final String FLAG_PROJECT = "/project";
}
