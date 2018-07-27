package org.apache.tools.ant.taskdefs.optional.perforce;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
public class P4Sync extends P4Base {
    String label;
    private String syncCmd = "";
    public void setLabel(String label) throws BuildException {
        if (label == null || label.equals("")) {
            throw new BuildException("P4Sync: Labels cannot be Null or Empty");
        }
        this.label = label;
    }
    public void setForce(String force) throws BuildException {
        if (force == null && !label.equals("")) {
            throw new BuildException("P4Sync: If you want to force, set force to non-null string!");
        }
        P4CmdOpts = "-f";
    }
    public void execute() throws BuildException {
        if (P4View != null) {
            syncCmd = P4View;
        }
        if (label != null && !label.equals("")) {
            syncCmd = syncCmd + "@" + label;
        }
        log("Execing sync " + P4CmdOpts + " " + syncCmd, Project.MSG_VERBOSE);
        execP4Command("-s sync " + P4CmdOpts + " " + syncCmd, new SimpleP4OutputHandler(this));
    }
}
