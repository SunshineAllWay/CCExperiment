package org.apache.tools.ant.taskdefs.optional.sos;
import org.apache.tools.ant.types.Commandline;
public class SOSCheckin extends SOS {
    public final void setFile(String filename) {
        super.setInternalFilename(filename);
    }
    public void setRecursive(boolean recursive) {
        super.setInternalRecursive(recursive);
    }
    public void setComment(String comment) {
        super.setInternalComment(comment);
    }
    protected Commandline buildCmdLine() {
        commandLine = new Commandline();
        if (getFilename() != null) {
            commandLine.createArgument().setValue(SOSCmd.FLAG_COMMAND);
            commandLine.createArgument().setValue(SOSCmd.COMMAND_CHECKIN_FILE);
            commandLine.createArgument().setValue(SOSCmd.FLAG_FILE);
            commandLine.createArgument().setValue(getFilename());
        } else {
            commandLine.createArgument().setValue(SOSCmd.FLAG_COMMAND);
            commandLine.createArgument().setValue(SOSCmd.COMMAND_CHECKIN_PROJECT);
            commandLine.createArgument().setValue(getRecursive());
        }
        getRequiredAttributes();
        getOptionalAttributes();
        if (getComment() != null) {
            commandLine.createArgument().setValue(SOSCmd.FLAG_COMMENT);
            commandLine.createArgument().setValue(getComment());
        }
        return commandLine;
    }
}
