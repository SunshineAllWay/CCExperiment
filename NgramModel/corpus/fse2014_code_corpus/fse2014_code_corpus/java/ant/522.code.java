package org.apache.tools.ant.taskdefs.optional.perforce;
import org.apache.tools.ant.BuildException;
public class P4Have extends P4Base {
    public void execute() throws BuildException {
        execP4Command("have " + P4CmdOpts + " " + P4View, new SimpleP4OutputHandler(this));
    }
}
