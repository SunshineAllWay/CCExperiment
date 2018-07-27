package org.apache.tools.ant.taskdefs.optional.perforce;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.StringUtils;
public class P4Labelsync extends P4Base {
    protected String name;
    private boolean add; 
    private boolean delete; 
    private boolean simulationmode;  
    public boolean isAdd() {
        return add;
    }
    public void setAdd(boolean add) {
        this.add = add;
    }
    public boolean isDelete() {
        return delete;
    }
    public void setDelete(boolean delete) {
        this.delete = delete;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isSimulationmode() {
        return simulationmode;
    }
    public void setSimulationmode(boolean simulationmode) {
        this.simulationmode = simulationmode;
    }
    public void execute() throws BuildException {
        log("P4Labelsync exec:", Project.MSG_INFO);
        if (P4View != null && P4View.length() >= 1) {
            P4View = StringUtils.replace(P4View, ":", "\n\t");
            P4View = StringUtils.replace(P4View, ";", "\n\t");
        }
        if (P4View == null) {
            P4View = "";
        }
        if (name == null || name.length() < 1) {
            throw new BuildException("name attribute is compulsory for labelsync");
        }
        if (this.isSimulationmode()) {
            P4CmdOpts = P4CmdOpts + " -n";
        }
        if (this.isDelete()) {
            P4CmdOpts = P4CmdOpts + " -d";
        }
        if (this.isAdd()) {
            P4CmdOpts = P4CmdOpts + " -a";
        }
        execP4Command("-s labelsync -l " + name + " " + P4CmdOpts + " " + P4View,
            new SimpleP4OutputHandler(this));
    }
}
