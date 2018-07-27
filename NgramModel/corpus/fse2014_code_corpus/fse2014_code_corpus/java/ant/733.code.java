package org.apache.tools.ant.types.selectors;
import java.io.File;
import org.apache.tools.ant.BuildException;
public interface FileSelector {
    boolean isSelected(File basedir, String filename, File file)
            throws BuildException;
}
