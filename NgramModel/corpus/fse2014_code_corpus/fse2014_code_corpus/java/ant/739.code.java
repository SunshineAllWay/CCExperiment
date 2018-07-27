package org.apache.tools.ant.types.selectors;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.IdentityMapper;
public class PresentSelector extends BaseSelector {
    private File targetdir = null;
    private Mapper mapperElement = null;
    private FileNameMapper map = null;
    private boolean destmustexist = true;
    public PresentSelector() {
    }
    public String toString() {
        StringBuffer buf = new StringBuffer("{presentselector targetdir: ");
        if (targetdir == null) {
            buf.append("NOT YET SET");
        } else {
            buf.append(targetdir.getName());
        }
        buf.append(" present: ");
        if (destmustexist) {
            buf.append("both");
        } else {
            buf.append("srconly");
        }
        if (map != null) {
            buf.append(map.toString());
        } else if (mapperElement != null) {
            buf.append(mapperElement.toString());
        }
        buf.append("}");
        return buf.toString();
    }
    public void setTargetdir(File targetdir) {
        this.targetdir = targetdir;
    }
    public Mapper createMapper() throws BuildException {
        if (map != null || mapperElement != null) {
            throw new BuildException("Cannot define more than one mapper");
        }
        mapperElement = new Mapper(getProject());
        return mapperElement;
    }
    public void addConfigured(FileNameMapper fileNameMapper) {
        if (map != null || mapperElement != null) {
            throw new BuildException("Cannot define more than one mapper");
        }
        this.map = fileNameMapper;
    }
    public void setPresent(FilePresence fp) {
        if (fp.getIndex() == 0) {
            destmustexist = false;
        }
    }
    public void verifySettings() {
        if (targetdir == null) {
            setError("The targetdir attribute is required.");
        }
        if (map == null) {
            if (mapperElement == null) {
                map = new IdentityMapper();
            } else {
                map = mapperElement.getImplementation();
                if (map == null) {
                    setError("Could not set <mapper> element.");
                }
            }
        }
    }
    public boolean isSelected(File basedir, String filename, File file) {
        validate();
        String[] destfiles = map.mapFileName(filename);
        if (destfiles == null) {
            return false;
        }
        if (destfiles.length != 1 || destfiles[0] == null) {
            throw new BuildException("Invalid destination file results for "
                    + targetdir + " with filename " + filename);
        }
        String destname = destfiles[0];
        File destfile = FileUtils.getFileUtils().resolveFile(targetdir, destname);
        return destfile.exists() == destmustexist;
    }
    public static class FilePresence extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] { "srconly", "both" };
        }
    }
}
