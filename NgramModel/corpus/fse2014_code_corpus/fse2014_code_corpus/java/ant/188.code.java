package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
public class Deltree extends Task {
    private File dir;
    public void setDir(File dir) {
        this.dir = dir;
    }
    public void execute() throws BuildException {
        log("DEPRECATED - The deltree task is deprecated.  "
            + "Use delete instead.");
        if (dir == null) {
            throw new BuildException("dir attribute must be set!", getLocation());
        }
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                if (!dir.delete()) {
                    throw new BuildException("Unable to delete directory "
                                             + dir.getAbsolutePath(),
                                             getLocation());
                }
                return;
            }
            log("Deleting: " + dir.getAbsolutePath());
            try {
                removeDir(dir);
            } catch (IOException ioe) {
                String msg = "Unable to delete " + dir.getAbsolutePath();
                throw new BuildException(msg, getLocation());
            }
        }
    }
    private void removeDir(File dir) throws IOException {
        String[] list = dir.list();
        for (int i = 0; i < list.length; i++) {
            String s = list[i];
            File f = new File(dir, s);
            if (f.isDirectory()) {
                removeDir(f);
            } else {
                if (!f.delete()) {
                    throw new BuildException("Unable to delete file "
                                             + f.getAbsolutePath());
                }
            }
        }
        if (!dir.delete()) {
            throw new BuildException("Unable to delete directory "
                                     + dir.getAbsolutePath());
        }
    }
}
