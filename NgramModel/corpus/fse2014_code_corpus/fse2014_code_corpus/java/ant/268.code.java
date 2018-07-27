package org.apache.tools.ant.taskdefs;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;
public class TempFile extends Task {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private String property;
    private File destDir = null;
    private String prefix;
    private String suffix = "";
    private boolean deleteOnExit;
    private boolean createFile;
    public void setProperty(String property) {
        this.property = property;
    }
    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    public void setDeleteOnExit(boolean deleteOnExit) {
        this.deleteOnExit = deleteOnExit;
    }
    public boolean isDeleteOnExit() {
        return deleteOnExit;
    }
    public void setCreateFile(boolean createFile) {
        this.createFile = createFile;
    }
    public boolean isCreateFile() {
        return createFile;
    }
    public void execute() throws BuildException {
        if (property == null || property.length() == 0) {
            throw new BuildException("no property specified");
        }
        if (destDir == null) {
            destDir = getProject().resolveFile(".");
        }
        File tfile = FILE_UTILS.createTempFile(prefix, suffix, destDir,
                    deleteOnExit, createFile);
        getProject().setNewProperty(property, tfile.toString());
    }
}
