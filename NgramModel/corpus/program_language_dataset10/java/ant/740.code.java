package org.apache.tools.ant.types.selectors;
import java.io.File;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.selectors.ResourceSelector;
public class ReadableSelector implements FileSelector, ResourceSelector {
    public boolean isSelected(File basedir, String filename, File file) {
        return file != null && file.canRead();
    }
    public boolean isSelected(Resource r) {
        FileProvider fp = (FileProvider) r.as(FileProvider.class);
        if (fp != null) {
            return isSelected(null, null, fp.getFile());
        }
        return false;
    }
}