package org.apache.tools.ant.taskdefs.optional.perforce;
import org.apache.tools.ant.BuildException;
public class P4Resolve extends P4Base {
    private String resolvemode = null;
    private boolean redoall; 
    private boolean simulationmode;  
    private boolean forcetextmode;  
    private boolean markersforall; 
    private static final String AUTOMATIC = "automatic";
    private static final String FORCE = "force";
    private static final String SAFE = "safe";
    private static final String THEIRS = "theirs";
    private static final String YOURS = "yours";
    private static final String[] RESOLVE_MODES = {
        AUTOMATIC,
        FORCE,
        SAFE,
        THEIRS,
        YOURS
    };
    public String getResolvemode() {
        return resolvemode;
    }
    public void setResolvemode(String resolvemode) {
        boolean found = false;
        for (int counter = 0; counter < RESOLVE_MODES.length; counter++) {
            if (resolvemode.equals(RESOLVE_MODES[counter])) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new BuildException("Unacceptable value for resolve mode");
        }
        this.resolvemode = resolvemode;
    }
    public boolean isRedoall() {
        return redoall;
    }
    public void setRedoall(boolean redoall) {
        this.redoall = redoall;
    }
    public boolean isSimulationmode() {
        return simulationmode;
    }
    public void setSimulationmode(boolean simulationmode) {
        this.simulationmode = simulationmode;
    }
    public boolean isForcetextmode() {
        return forcetextmode;
    }
    public void setForcetextmode(boolean forcetextmode) {
        this.forcetextmode = forcetextmode;
    }
    public boolean isMarkersforall() {
        return markersforall;
    }
    public void setMarkersforall(boolean markersforall) {
        this.markersforall = markersforall;
    }
    public void execute() throws BuildException {
        if (this.resolvemode.equals(AUTOMATIC)) {
            P4CmdOpts = P4CmdOpts + " -am";
        } else if (this.resolvemode.equals(FORCE)) {
            P4CmdOpts = P4CmdOpts + " -af";
        } else if (this.resolvemode.equals(SAFE)) {
            P4CmdOpts = P4CmdOpts + " -as";
        } else if (this.resolvemode.equals(THEIRS)) {
            P4CmdOpts = P4CmdOpts + " -at";
        } else if (this.resolvemode.equals(YOURS)) {
            P4CmdOpts = P4CmdOpts + " -ay";
        } else {
            throw new BuildException("unsupported or absent resolve mode");
        }
        if (P4View == null) {
            throw new BuildException("please specify a view");
        }
        if (this.isRedoall()) {
            P4CmdOpts = P4CmdOpts + " -f";
        }
        if (this.isSimulationmode()) {
            P4CmdOpts = P4CmdOpts + " -n";
        }
        if (this.isForcetextmode()) {
            P4CmdOpts = P4CmdOpts + " -t";
        }
        if (this.isMarkersforall()) {
            P4CmdOpts = P4CmdOpts + " -v";
        }
        execP4Command("-s resolve " + P4CmdOpts + " " + P4View, new SimpleP4OutputHandler(this));
    }
}
