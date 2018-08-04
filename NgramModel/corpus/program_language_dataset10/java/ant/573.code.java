package org.apache.tools.ant.taskdefs.optional.vss;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Commandline;
public class MSVSSCREATE extends MSVSS {
    Commandline buildCmdLine() {
        Commandline commandLine = new Commandline();
        if (getVsspath() == null) {
            String msg = "vsspath attribute must be set!";
            throw new BuildException(msg, getLocation());
        }
        commandLine.setExecutable(getSSCommand());
        commandLine.createArgument().setValue(COMMAND_CREATE);
        commandLine.createArgument().setValue(getVsspath());
        commandLine.createArgument().setValue(getComment());
        commandLine.createArgument().setValue(getAutoresponse());
        commandLine.createArgument().setValue(getQuiet());
        commandLine.createArgument().setValue(getLogin());
        return commandLine;
    }
    public void setComment(String comment) {
        super.setInternalComment(comment);
    }
    public final void setQuiet (boolean quiet) {
        super.setInternalQuiet(quiet);
    }
    public void setAutoresponse(String response) {
        super.setInternalAutoResponse(response);
    }
}