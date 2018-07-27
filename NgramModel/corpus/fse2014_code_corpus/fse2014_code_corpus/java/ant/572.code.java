package org.apache.tools.ant.taskdefs.optional.vss;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Commandline;
public class MSVSSCP extends MSVSS {
    protected Commandline buildCmdLine() {
        Commandline commandLine = new Commandline();
        if (getVsspath() == null) {
            String msg = "vsspath attribute must be set!";
            throw new BuildException(msg, getLocation());
        }
        commandLine.setExecutable(getSSCommand());
        commandLine.createArgument().setValue(COMMAND_CP);
        commandLine.createArgument().setValue(getVsspath());
        commandLine.createArgument().setValue(getAutoresponse());
        commandLine.createArgument().setValue(getLogin());
        return commandLine;
    }
    public void setAutoresponse(String response) {
        super.setInternalAutoResponse(response);
    }
}
