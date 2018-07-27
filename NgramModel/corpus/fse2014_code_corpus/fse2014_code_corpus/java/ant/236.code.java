package org.apache.tools.ant.taskdefs;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
public class Mkdir extends Task {
    private static final int MKDIR_RETRY_SLEEP_MILLIS = 10;
    private File dir;
    public void execute() throws BuildException {
        if (dir == null) {
            throw new BuildException("dir attribute is required", getLocation());
        }
        if (dir.isFile()) {
            throw new BuildException("Unable to create directory as a file "
                                     + "already exists with that name: "
                                     + dir.getAbsolutePath());
        }
        if (!dir.exists()) {
            boolean result = mkdirs(dir);
            if (!result) {
                if (dir.exists()) {
                    log("A different process or task has already created "
                        + "dir " + dir.getAbsolutePath(),
                        Project.MSG_VERBOSE);
                    return;
                }
                String msg = "Directory " + dir.getAbsolutePath()
                    + " creation was not successful for an unknown reason";
                throw new BuildException(msg, getLocation());
            }
            log("Created dir: " + dir.getAbsolutePath());
        } else {
            log("Skipping " + dir.getAbsolutePath()
                + " because it already exists.", Project.MSG_VERBOSE);
        }
    }
    public void setDir(File dir) {
        this.dir = dir;
    }
    private boolean mkdirs(File f) {
        if (!f.mkdirs()) {
            try {
                Thread.sleep(MKDIR_RETRY_SLEEP_MILLIS);
                return f.mkdirs();
            } catch (InterruptedException ex) {
                return f.mkdirs();
            }
        }
        return true;
    }
}
