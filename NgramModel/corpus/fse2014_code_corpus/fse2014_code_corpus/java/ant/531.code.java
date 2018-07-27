package org.apache.tools.ant.taskdefs.optional.perforce;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import java.util.Vector;
public class P4Submit extends P4Base {
    public String change;
    private String changeProperty;
    private String needsResolveProperty;
    public void setChange(String change) {
        this.change = change;
    }
    public void setChangeProperty(String changeProperty) {
        this.changeProperty = changeProperty;
    }
    public void setNeedsResolveProperty(String needsResolveProperty) {
        this.needsResolveProperty = needsResolveProperty;
    }
    public void execute() throws BuildException {
        if (change != null) {
            execP4Command("submit -c " + change, (P4HandlerAdapter) new P4SubmitAdapter(this));
        } else {
            throw new BuildException("No change specified (no support for default change yet....");
        }
    }
    public class P4SubmitAdapter extends SimpleP4OutputHandler {
        public P4SubmitAdapter(P4Base parent) {
            super(parent);
        }
        public void process(String line) {
            super.process(line);
            getProject().setProperty("p4.needsresolve", "0");
            if (util.match("/renamed/", line)) {
                try {
                    Vector myarray = new Vector();
                    util.split(myarray, line);
                    boolean found = false;
                    for (int counter = 0; counter < myarray.size(); counter++) {
                        if (found) {
                            String chnum = (String) myarray.elementAt(counter + 1);
                            int changenumber = Integer.parseInt(chnum);
                            log("Perforce change renamed " + changenumber, Project.MSG_INFO);
                            getProject().setProperty("p4.change", "" + changenumber);
                            if (changeProperty != null) {
                                getProject().setNewProperty(changeProperty, chnum);
                            }
                            found = false;
                        }
                        if (((myarray.elementAt(counter))).equals("renamed")) {
                            found = true;
                        }
                    }
                } catch (Exception e) {
                    String msg = "Failed to parse " + line  + "\n"
                            + " due to " + e.getMessage();
                    throw new BuildException(msg, e, getLocation());
                }
            }
            if (util.match("/p4 submit -c/", line)) {
                getProject().setProperty("p4.needsresolve", "1");
                if (needsResolveProperty != null) {
                    getProject().setNewProperty(needsResolveProperty, "true");
                }
            }
        }
    }
}
