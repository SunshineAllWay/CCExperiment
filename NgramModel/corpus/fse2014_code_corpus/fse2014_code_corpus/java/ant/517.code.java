package org.apache.tools.ant.taskdefs.optional.perforce;
import org.apache.tools.ant.BuildException;
public class P4Delete extends P4Base {
    public String change = null;
    public void setChange(String change) {
        this.change = change;
    }
    public void execute() throws BuildException {
        if (change != null) {
            P4CmdOpts = "-c " + change;
        }
        if (P4View == null) {
            throw new BuildException("No view specified to delete");
        }
        execP4Command("-s delete " + P4CmdOpts + " " + P4View, new SimpleP4OutputHandler(this));
    }
}
