package org.apache.tools.ant.types.selectors;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.BuildException;
import java.io.File;
import java.io.IOException;
public class DifferentSelector extends MappingSelector {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private boolean ignoreFileTimes = true;
    private boolean ignoreContents = false;
    public void setIgnoreFileTimes(boolean ignoreFileTimes) {
        this.ignoreFileTimes = ignoreFileTimes;
    }
    public void setIgnoreContents(boolean ignoreContents) {
        this.ignoreContents = ignoreContents;
    }
    protected boolean selectionTest(File srcfile, File destfile) {
        if (srcfile.exists() != destfile.exists()) {
            return true;
        }
        if (srcfile.length() != destfile.length()) {
            return true;
        }
        if (!ignoreFileTimes) {
            boolean sameDate;
            sameDate = destfile.lastModified() >= srcfile.lastModified() - granularity
                    && destfile.lastModified() <= srcfile.lastModified() + granularity;
            if (!sameDate) {
                return true;
            }
        }
        if (!ignoreContents) {
            try {
                return !FILE_UTILS.contentEquals(srcfile, destfile);
            } catch (IOException e) {
                throw new BuildException("while comparing " + srcfile + " and "
                        + destfile, e);
            }
        } else {
            return false;
        }
    }
}