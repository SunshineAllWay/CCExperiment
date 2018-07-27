package org.apache.tools.ant.types.selectors;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.IdentityMapper;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;
import java.io.File;
public abstract class MappingSelector extends BaseSelector {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    protected File targetdir = null;
    protected Mapper mapperElement = null;
    protected FileNameMapper map = null;
    protected int granularity = 0;
    public MappingSelector() {
        granularity = (int) FILE_UTILS.getFileTimestampGranularity();
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
                    + targetdir.getName() + " with filename " + filename);
        }
        String destname = destfiles[0];
        File destfile = FILE_UTILS.resolveFile(targetdir, destname);
        boolean selected = selectionTest(file, destfile);
        return selected;
    }
    protected abstract boolean selectionTest(File srcfile, File destfile);
    public void setGranularity(int granularity) {
        this.granularity = granularity;
    }
}
