package org.apache.tools.ant.taskdefs.optional.sos;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Commandline;
public class SOSLabel extends SOS {
    public void setVersion(String version) {
        super.setInternalVersion(version);
    }
    public void setLabel(String label) {
        super.setInternalLabel(label);
    }
    public void setComment(String comment) {
        super.setInternalComment(comment);
    }
    protected Commandline buildCmdLine() {
        commandLine = new Commandline();
        commandLine.createArgument().setValue(SOSCmd.FLAG_COMMAND);
        commandLine.createArgument().setValue(SOSCmd.COMMAND_LABEL);
        getRequiredAttributes();
        if (getLabel() == null) {
            throw new BuildException("label attribute must be set!", getLocation());
        }
        commandLine.createArgument().setValue(SOSCmd.FLAG_LABEL);
        commandLine.createArgument().setValue(getLabel());
        commandLine.createArgument().setValue(getVerbose());
        if (getComment() != null) {
            commandLine.createArgument().setValue(SOSCmd.FLAG_COMMENT);
            commandLine.createArgument().setValue(getComment());
        }
        return commandLine;
    }
}
