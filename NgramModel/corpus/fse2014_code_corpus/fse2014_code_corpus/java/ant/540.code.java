package org.apache.tools.ant.taskdefs.optional.sos;
import org.apache.tools.ant.types.Commandline;
public class SOSCheckout extends SOS {
    public final void setFile(String filename) {
        super.setInternalFilename(filename);
    }
    public void setRecursive(boolean recursive) {
        super.setInternalRecursive(recursive);
    }
    protected Commandline buildCmdLine() {
        commandLine = new Commandline();
        if (getFilename() != null) {
            commandLine.createArgument().setValue(SOSCmd.FLAG_COMMAND);
            commandLine.createArgument().setValue(SOSCmd.COMMAND_CHECKOUT_FILE);
            commandLine.createArgument().setValue(SOSCmd.FLAG_FILE);
            commandLine.createArgument().setValue(getFilename());
        } else {
            commandLine.createArgument().setValue(SOSCmd.FLAG_COMMAND);
            commandLine.createArgument().setValue(SOSCmd.COMMAND_CHECKOUT_PROJECT);
            commandLine.createArgument().setValue(getRecursive());
        }
        getRequiredAttributes();
        getOptionalAttributes();
        return commandLine;
    }
}
