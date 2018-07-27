package org.apache.tools.ant.taskdefs.optional.sos;
import org.apache.tools.ant.types.Commandline;
public class SOSGet extends SOS {
    public final void setFile(String filename) {
        super.setInternalFilename(filename);
    }
    public void setRecursive(boolean recursive) {
        super.setInternalRecursive(recursive);
    }
    public void setVersion(String version) {
        super.setInternalVersion(version);
    }
    public void setLabel(String label) {
        super.setInternalLabel(label);
    }
    protected Commandline buildCmdLine() {
        commandLine = new Commandline();
        if (getFilename() != null) {
            commandLine.createArgument().setValue(SOSCmd.FLAG_COMMAND);
            commandLine.createArgument().setValue(SOSCmd.COMMAND_GET_FILE);
            commandLine.createArgument().setValue(SOSCmd.FLAG_FILE);
            commandLine.createArgument().setValue(getFilename());
            if (getVersion() != null) {
                commandLine.createArgument().setValue(SOSCmd.FLAG_VERSION);
                commandLine.createArgument().setValue(getVersion());
            }
        } else {
            commandLine.createArgument().setValue(SOSCmd.FLAG_COMMAND);
            commandLine.createArgument().setValue(SOSCmd.COMMAND_GET_PROJECT);
            commandLine.createArgument().setValue(getRecursive());
            if (getLabel() != null) {
                commandLine.createArgument().setValue(SOSCmd.FLAG_LABEL);
                commandLine.createArgument().setValue(getLabel());
            }
        }
        getRequiredAttributes();
        getOptionalAttributes();
        return commandLine;
    }
}
