package org.apache.tools.ant.taskdefs;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.FileResource;
public abstract class Unpack extends Task {
    protected File source;
    protected File dest;
    protected Resource srcResource;
    public void setSrc(String src) {
        log("DEPRECATED - The setSrc(String) method has been deprecated."
            + " Use setSrc(File) instead.");
        setSrc(getProject().resolveFile(src));
    }
    public void setDest(String dest) {
        log("DEPRECATED - The setDest(String) method has been deprecated."
            + " Use setDest(File) instead.");
        setDest(getProject().resolveFile(dest));
    }
    public void setSrc(File src) {
        setSrcResource(new FileResource(src));
    }
    public void setSrcResource(Resource src) {
        if (!src.isExists()) {
            throw new BuildException(
                "the archive " + src.getName() + " doesn't exist");
        }
        if (src.isDirectory()) {
            throw new BuildException(
                "the archive " + src.getName() + " can't be a directory");
        }
        FileProvider fp = (FileProvider) src.as(FileProvider.class);
        if (fp != null) {
            source = fp.getFile();
        } else if (!supportsNonFileResources()) {
            throw new BuildException(
                "The source " + src.getName()
                + " is not a FileSystem "
                + "Only FileSystem resources are"
                + " supported.");
        }
        srcResource = src;
    }
    public void addConfigured(ResourceCollection a) {
        if (a.size() != 1) {
            throw new BuildException("only single argument resource collections"
                                     + " are supported as archives");
        }
        setSrcResource((Resource) a.iterator().next());
    }
    public void setDest(File dest) {
        this.dest = dest;
    }
    private void validate() throws BuildException {
        if (srcResource == null) {
            throw new BuildException("No Src specified", getLocation());
        }
        if (dest == null) {
            dest = new File(source.getParent());
        }
        if (dest.isDirectory()) {
            String defaultExtension = getDefaultExtension();
            createDestFile(defaultExtension);
        }
    }
    private void createDestFile(String defaultExtension) {
        String sourceName = source.getName();
        int len = sourceName.length();
        if (defaultExtension != null
            && len > defaultExtension.length()
            && defaultExtension.equalsIgnoreCase(
                sourceName.substring(len - defaultExtension.length()))) {
            dest = new File(dest, sourceName.substring(0,
                                                       len - defaultExtension.length()));
        } else {
            dest = new File(dest, sourceName);
        }
    }
    public void execute() throws BuildException {
        File savedDest = dest; 
        try {
            validate();
            extract();
        } finally {
            dest = savedDest;
        }
    }
    protected abstract String getDefaultExtension();
    protected abstract void extract();
    protected boolean supportsNonFileResources() {
        return false;
    }
}
