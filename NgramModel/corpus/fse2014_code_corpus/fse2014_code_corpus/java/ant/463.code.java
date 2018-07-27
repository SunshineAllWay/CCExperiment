package org.apache.tools.ant.taskdefs.optional.jlink;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
public class JlinkTask extends MatchingTask {
    public  void setOutfile(File outfile) {
        this.outfile = outfile;
    }
    public  Path createMergefiles() {
        if (this.mergefiles == null) {
            this.mergefiles = new Path(getProject());
        }
        return this.mergefiles.createPath();
    }
    public  void setMergefiles(Path mergefiles) {
        if (this.mergefiles == null) {
            this.mergefiles = mergefiles;
        } else {
            this.mergefiles.append(mergefiles);
        }
    }
    public  Path createAddfiles() {
        if (this.addfiles == null) {
            this.addfiles = new Path(getProject());
        }
        return this.addfiles.createPath();
    }
    public  void setAddfiles(Path addfiles) {
        if (this.addfiles == null) {
            this.addfiles = addfiles;
        } else {
            this.addfiles.append(addfiles);
        }
    }
    public  void setCompress(boolean compress) {
        this.compress = compress;
    }
    public  void execute() throws BuildException {
        if (outfile == null) {
            throw new BuildException("outfile attribute is required! "
                + "Please set.");
        }
        if (!haveAddFiles() && !haveMergeFiles()) {
            throw new BuildException("addfiles or mergefiles required! "
                + "Please set.");
        }
        log("linking:     " + outfile.getPath());
        log("compression: " + compress, Project.MSG_VERBOSE);
        jlink linker = new jlink();
        linker.setOutfile(outfile.getPath());
        linker.setCompression(compress);
        if (haveMergeFiles()) {
            log("merge files: " + mergefiles.toString(), Project.MSG_VERBOSE);
            linker.addMergeFiles(mergefiles.list());
        }
        if (haveAddFiles()) {
            log("add files: " + addfiles.toString(), Project.MSG_VERBOSE);
            linker.addAddFiles(addfiles.list());
        }
        try  {
            linker.link();
        } catch (Exception ex) {
            throw new BuildException(ex, getLocation());
        }
    }
    private boolean haveAddFiles() {
        return haveEntries(addfiles);
    }
    private boolean haveMergeFiles() {
        return haveEntries(mergefiles);
    }
    private boolean haveEntries(Path p) {
        if (p == null) {
            return false;
        }
        if (p.size() > 0) {
            return true;
        }
        return false;
    }
    private  File outfile = null;
    private  Path mergefiles = null;
    private  Path addfiles = null;
    private  boolean compress = false;
}
