package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import java.io.File;
import java.io.IOException;
public class CopyPath extends Task {
    public static final String ERROR_NO_DESTDIR = "No destDir specified";
    public static final String ERROR_NO_PATH = "No path specified";
    public static final String ERROR_NO_MAPPER = "No mapper specified";
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private FileNameMapper mapper;
    private Path path;
    private File destDir;
    private long granularity = FILE_UTILS.getFileTimestampGranularity();
    private boolean preserveLastModified = false;
    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }
    public void add(FileNameMapper newmapper) {
        if (mapper != null) {
            throw new BuildException("Only one mapper allowed");
        }
        mapper = newmapper;
    }
    public void setPath(Path s) {
        createPath().append(s);
    }
    public void setPathRef(Reference r) {
        createPath().setRefid(r);
    }
    public Path createPath() {
        if (path == null) {
            path = new Path(getProject());
        }
        return path;
    }
    public void setGranularity(long granularity) {
        this.granularity = granularity;
    }
    public void setPreserveLastModified(boolean preserveLastModified) {
        this.preserveLastModified = preserveLastModified;
    }
    protected void validateAttributes() throws BuildException {
        if (destDir == null) {
            throw new BuildException(ERROR_NO_DESTDIR);
        }
        if (mapper == null) {
            throw new BuildException(ERROR_NO_MAPPER);
        }
        if (path == null) {
            throw new BuildException(ERROR_NO_PATH);
        }
    }
    public void execute() throws BuildException {
        log("This task should have never been released and was"
            + " obsoleted by ResourceCollection support in <copy> available"
            + " since Ant 1.7.0.  Don't use it.",
            Project.MSG_ERR);
        validateAttributes();
        String[] sourceFiles = path.list();
        if (sourceFiles.length == 0) {
            log("Path is empty", Project.MSG_VERBOSE);
            return;
        }
        for (int sources = 0; sources < sourceFiles.length; sources++) {
            String sourceFileName = sourceFiles[sources];
            File sourceFile = new File(sourceFileName);
            String[] toFiles = (String[]) mapper.mapFileName(sourceFileName);
            for (int i = 0; i < toFiles.length; i++) {
                String destFileName = toFiles[i];
                File destFile = new File(destDir, destFileName);
                if (sourceFile.equals(destFile)) {
                    log("Skipping self-copy of " + sourceFileName, Project.MSG_VERBOSE);
                    continue;
                }
                if (sourceFile.isDirectory()) {
                    log("Skipping directory " + sourceFileName);
                    continue;
                }
                try {
                    log("Copying " + sourceFile + " to " + destFile, Project.MSG_VERBOSE);
                    FILE_UTILS.copyFile(sourceFile, destFile, null, null, false,
                            preserveLastModified, null, null, getProject());
                } catch (IOException ioe) {
                    String msg = "Failed to copy " + sourceFile + " to " + destFile + " due to "
                            + ioe.getMessage();
                    if (destFile.exists() && !destFile.delete()) {
                        msg += " and I couldn't delete the corrupt " + destFile;
                    }
                    throw new BuildException(msg, ioe, getLocation());
                }
            }
        }
    }
}
