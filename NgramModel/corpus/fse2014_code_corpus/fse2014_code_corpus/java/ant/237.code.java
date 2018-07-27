package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterSet;
import org.apache.tools.ant.types.FilterSetCollection;
public class Move extends Copy {
    public Move() {
        super();
        setOverwrite(true);
    }
    protected void validateAttributes() throws BuildException {
        if (file != null && file.isDirectory()) {
            if ((destFile != null && destDir != null)
                || (destFile == null && destDir == null)) {
                throw new BuildException("One and only one of tofile and todir must be set.");
            }
            destFile = destFile == null ? new File(destDir, file.getName()) : destFile;
            destDir = destDir == null ? destFile.getParentFile() : destDir;
            completeDirMap.put(file, destFile);
            file = null;
        } else {
            super.validateAttributes();
        }
    }
    protected void doFileOperations() {
        if (completeDirMap.size() > 0) {
            for (Iterator fromDirs = completeDirMap.keySet().iterator(); fromDirs.hasNext();) {
                File fromDir = (File) fromDirs.next();
                File toDir = (File) completeDirMap.get(fromDir);
                boolean renamed = false;
                try {
                    log("Attempting to rename dir: " + fromDir + " to " + toDir, verbosity);
                    renamed = renameFile(fromDir, toDir, filtering, forceOverwrite);
                } catch (IOException ioe) {
                    String msg = "Failed to rename dir " + fromDir
                            + " to " + toDir + " due to " + ioe.getMessage();
                    throw new BuildException(msg, ioe, getLocation());
                }
                if (!renamed) {
                    FileSet fs = new FileSet();
                    fs.setProject(getProject());
                    fs.setDir(fromDir);
                    addFileset(fs);
                    DirectoryScanner ds = fs.getDirectoryScanner(getProject());
                    String[] files = ds.getIncludedFiles();
                    String[] dirs = ds.getIncludedDirectories();
                    scan(fromDir, toDir, files, dirs);
                }
            }
        }
        int moveCount = fileCopyMap.size();
        if (moveCount > 0) {   
            log("Moving " + moveCount + " file" + ((moveCount == 1) ? "" : "s")
                    + " to " + destDir.getAbsolutePath());
            for (Iterator fromFiles = fileCopyMap.keySet().iterator(); fromFiles.hasNext();) {
                String fromFile = (String) fromFiles.next();
                File f = new File(fromFile);
                boolean selfMove = false;
                if (f.exists()) { 
                    String[] toFiles = (String[]) fileCopyMap.get(fromFile);
                    for (int i = 0; i < toFiles.length; i++) {
                        String toFile = (String) toFiles[i];
                        if (fromFile.equals(toFile)) {
                            log("Skipping self-move of " + fromFile, verbosity);
                            selfMove = true;
                            continue;
                        }
                        File d = new File(toFile);
                        if ((i + 1) == toFiles.length && !selfMove) {
                            moveFile(f, d, filtering, forceOverwrite);
                        } else {
                            copyFile(f, d, filtering, forceOverwrite);
                        }
                    }
                }
            }
        }
        if (includeEmpty) {
            int createCount = 0;
            for (Iterator fromDirNames = dirCopyMap.keySet().iterator(); fromDirNames.hasNext();) {
                String fromDirName = (String) fromDirNames.next();
                String[] toDirNames = (String[]) dirCopyMap.get(fromDirName);
                boolean selfMove = false;
                for (int i = 0; i < toDirNames.length; i++) {
                    if (fromDirName.equals(toDirNames[i])) {
                        log("Skipping self-move of " + fromDirName, verbosity);
                        selfMove = true;
                        continue;
                    }
                    File d = new File(toDirNames[i]);
                    if (!d.exists()) {
                        if (!d.mkdirs()) {
                            log("Unable to create directory "
                                    + d.getAbsolutePath(), Project.MSG_ERR);
                        } else {
                            createCount++;
                        }
                    }
                }
                File fromDir = new File(fromDirName);
                if (!selfMove && okToDelete(fromDir)) {
                    deleteDir(fromDir);
                }
            }
            if (createCount > 0) {
                log("Moved " + dirCopyMap.size()
                        + " empty director"
                        + (dirCopyMap.size() == 1 ? "y" : "ies")
                        + " to " + createCount
                        + " empty director"
                        + (createCount == 1 ? "y" : "ies") + " under "
                        + destDir.getAbsolutePath());
            }
        }
    }
    private void moveFile(File fromFile, File toFile, boolean filtering, boolean overwrite) {
        boolean moved = false;
        try {
            log("Attempting to rename: " + fromFile + " to " + toFile, verbosity);
            moved = renameFile(fromFile, toFile, filtering, forceOverwrite);
        } catch (IOException ioe) {
            String msg = "Failed to rename " + fromFile
                + " to " + toFile + " due to " + ioe.getMessage();
            throw new BuildException(msg, ioe, getLocation());
        }
        if (!moved) {
            copyFile(fromFile, toFile, filtering, overwrite);
            if (!fromFile.delete()) {
                throw new BuildException("Unable to delete " + "file "
                        + fromFile.getAbsolutePath());
            }
        }
    }
    private void copyFile(File fromFile, File toFile, boolean filtering, boolean overwrite) {
        try {
            log("Copying " + fromFile + " to " + toFile, verbosity);
            FilterSetCollection executionFilters = new FilterSetCollection();
            if (filtering) {
                executionFilters.addFilterSet(getProject().getGlobalFilterSet());
            }
            for (Iterator filterIter = getFilterSets().iterator(); filterIter.hasNext();) {
                executionFilters.addFilterSet((FilterSet) filterIter.next());
            }
            getFileUtils().copyFile(fromFile, toFile, executionFilters,
                                    getFilterChains(),
                                    forceOverwrite,
                                    getPreserveLastModified(),
                                     false,
                                    getEncoding(),
                                    getOutputEncoding(),
                                    getProject(), getForce());
        } catch (IOException ioe) {
            String msg = "Failed to copy " + fromFile
                    + " to " + toFile + " due to " + ioe.getMessage();
            throw new BuildException(msg, ioe, getLocation());
        }
    }
    protected boolean okToDelete(File d) {
        String[] list = d.list();
        if (list == null) {
            return false;
        }     
        for (int i = 0; i < list.length; i++) {
            String s = list[i];
            File f = new File(d, s);
            if (f.isDirectory()) {
                if (!okToDelete(f)) {
                    return false;
                }
            } else {
                return false;   
            }
        }
        return true;
    }
    protected void deleteDir(File d) {
        deleteDir(d, false);
    }
    protected void deleteDir(File d, boolean deleteFiles) {
        String[] list = d.list();
        if (list == null) {
            return;
        }      
        for (int i = 0; i < list.length; i++) {
            String s = list[i];
            File f = new File(d, s);
            if (f.isDirectory()) {
                deleteDir(f);
            } else if (deleteFiles && !(f.delete())) {
                throw new BuildException("Unable to delete file " + f.getAbsolutePath());
            } else {
                throw new BuildException("UNEXPECTED ERROR - The file "
                        + f.getAbsolutePath() + " should not exist!");
            }
        }
        log("Deleting directory " + d.getAbsolutePath(), verbosity);
        if (!d.delete()) {
            throw new BuildException("Unable to delete directory " + d.getAbsolutePath());
        }
    }
    protected boolean renameFile(File sourceFile, File destFile, boolean filtering,
                                 boolean overwrite) throws IOException, BuildException {
        if (destFile.isDirectory() || filtering || getFilterSets().size() > 0
                || getFilterChains().size() > 0) {
            return false;
        }
        if (destFile.isFile() && !destFile.canWrite()) {
            if (!getForce()) {
                throw new IOException("can't replace read-only destination "
                                      + "file " + destFile);
            } else if (!getFileUtils().tryHardToDelete(destFile)) {
                throw new IOException("failed to delete read-only "
                                      + "destination file " + destFile);
            }
        }
        File parent = destFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        } else if (destFile.isFile()) {
            sourceFile = getFileUtils().normalize(sourceFile.getAbsolutePath()).getCanonicalFile();
            destFile = getFileUtils().normalize(destFile.getAbsolutePath());
            if (destFile.getAbsolutePath().equals(sourceFile.getAbsolutePath())) {
                log("Rename of " + sourceFile + " to " + destFile
                    + " is a no-op.", Project.MSG_VERBOSE);
                return true;
            }
            if (!(getFileUtils().areSame(sourceFile, destFile) || destFile.delete())) {
                throw new BuildException("Unable to remove existing file " + destFile);
            }
        }
        return sourceFile.renameTo(destFile);
    }
}
