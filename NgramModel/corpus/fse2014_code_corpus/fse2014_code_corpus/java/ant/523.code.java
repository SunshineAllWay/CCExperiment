package org.apache.tools.ant.taskdefs.optional.perforce;
import org.apache.tools.ant.BuildException;
public class P4Integrate extends P4Base {
    private String change = null;
    private String fromfile = null;
    private String tofile = null;
    private String branch = null;
    private boolean restoredeletedrevisions = false;
    private boolean forceintegrate = false;
    private boolean leavetargetrevision = false;
    private boolean enablebaselessmerges = false;
    private boolean simulationmode = false;
    private boolean reversebranchmappings = false;
    private boolean propagatesourcefiletype = false;
    private boolean nocopynewtargetfiles = false;
    public String getChange() {
        return change;
    }
    public void setChange(String change) {
        this.change = change;
    }
    public String getFromfile() {
        return fromfile;
    }
    public void setFromfile(String fromf) {
        this.fromfile = fromf;
    }
    public String getTofile() {
        return tofile;
    }
    public void setTofile(String tof) {
        this.tofile = tof;
    }
    public String getBranch() {
        return branch;
    }
    public void setBranch(String br) {
        this.branch = br;
    }
    public boolean isRestoreDeletedRevisions() {
        return restoredeletedrevisions;
    }
    public void setRestoreDeletedRevisions(boolean setrest) {
        this.restoredeletedrevisions = setrest;
    }
    public boolean isForceIntegrate() {
        return forceintegrate;
    }
    public void setForceIntegrate(boolean setrest) {
        this.forceintegrate = setrest;
    }
    public boolean isLeaveTargetRevision() {
        return leavetargetrevision;
    }
    public void setLeaveTargetRevision(boolean setrest) {
        this.leavetargetrevision = setrest;
    }
    public boolean isEnableBaselessMerges() {
        return enablebaselessmerges;
    }
    public void setEnableBaselessMerges(boolean setrest) {
        this.enablebaselessmerges = setrest;
    }
    public boolean isSimulationMode() {
        return simulationmode;
    }
    public void setSimulationMode(boolean setrest) {
        this.simulationmode = setrest;
    }
    public boolean isReversebranchmappings() {
        return reversebranchmappings;
    }
    public void setReversebranchmappings(boolean reversebranchmappings) {
        this.reversebranchmappings = reversebranchmappings;
    }
    public boolean isPropagatesourcefiletype() {
        return propagatesourcefiletype;
    }
    public void setPropagatesourcefiletype(boolean propagatesourcefiletype) {
        this.propagatesourcefiletype = propagatesourcefiletype;
    }
    public boolean isNocopynewtargetfiles() {
        return nocopynewtargetfiles;
    }
    public void setNocopynewtargetfiles(boolean nocopynewtargetfiles) {
        this.nocopynewtargetfiles = nocopynewtargetfiles;
    }
    public void execute() throws BuildException {
        if (change != null) {
            P4CmdOpts = "-c " + change;
        }
        if (this.forceintegrate) {
            P4CmdOpts = P4CmdOpts + " -f";
        }
        if (this.restoredeletedrevisions) {
                P4CmdOpts = P4CmdOpts + " -d";
            }
        if (this.leavetargetrevision) {
            P4CmdOpts = P4CmdOpts + " -h";
        }
        if (this.enablebaselessmerges) {
            P4CmdOpts = P4CmdOpts + " -i";
        }
        if (this.simulationmode) {
            P4CmdOpts = P4CmdOpts + " -n";
        }
        if (this.reversebranchmappings) {
            P4CmdOpts = P4CmdOpts + " -r";
        }
        if (this.propagatesourcefiletype) {
            P4CmdOpts = P4CmdOpts + " -t";
        }
        if (this.nocopynewtargetfiles) {
            P4CmdOpts = P4CmdOpts + "-v";
        }
        String command;
        if (branch == null && fromfile != null && tofile != null) {
           command = P4CmdOpts + " " + fromfile + " " + tofile;
        } else if (branch != null && fromfile == null && tofile != null) {
            command = P4CmdOpts + " -b " + branch + " " + tofile;
        } else if (branch != null && fromfile != null) {
            command = P4CmdOpts + " -b " + branch + " -s " + fromfile + " " + tofile;
        } else {
            throw new BuildException("you need to specify fromfile and tofile, "
            + "or branch and tofile, or branch and fromfile, or branch and fromfile and tofile ");
        }
        execP4Command("-s integrate " + command, new SimpleP4OutputHandler(this));
    }
}
