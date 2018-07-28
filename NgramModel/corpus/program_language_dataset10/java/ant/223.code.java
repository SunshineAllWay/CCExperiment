package org.apache.tools.ant.taskdefs;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;
import java.util.Vector;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.filters.util.ChainReaderHelper;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.JavaResource;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.ResourceUtils;
public class LoadProperties extends Task {
    private Resource src = null;
    private final Vector filterChains = new Vector();
    private String encoding = null;
    private String prefix = null;
    private boolean prefixValues = true;
    public final void setSrcFile(final File srcFile) {
        addConfigured(new FileResource(srcFile));
    }
    public void setResource(String resource) {
        getRequiredJavaResource().setName(resource);
    }
    public final void setEncoding(final String encoding) {
        this.encoding = encoding;
    }
    public void setClasspath(Path classpath) {
        getRequiredJavaResource().setClasspath(classpath);
    }
    public Path createClasspath() {
        return getRequiredJavaResource().createClasspath();
    }
    public void setClasspathRef(Reference r) {
        getRequiredJavaResource().setClasspathRef(r);
    }
    public Path getClasspath() {
        return getRequiredJavaResource().getClasspath();
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    public void setPrefixValues(boolean b) {
        prefixValues = b;
    }
    public final void execute() throws BuildException {
        if (src == null) {
            throw new BuildException("A source resource is required.");
        }
        if (!src.isExists()) {
            if (src instanceof JavaResource) {
                log("Unable to find resource " + src, Project.MSG_WARN);
                return;
            }
            throw new BuildException("Source resource does not exist: " + src);
        }
        BufferedInputStream bis = null;
        Reader instream = null;
        ByteArrayInputStream tis = null;
        try {
            bis = new BufferedInputStream(src.getInputStream());
            if (encoding == null) {
                instream = new InputStreamReader(bis);
            } else {
                instream = new InputStreamReader(bis, encoding);
            }
            ChainReaderHelper crh = new ChainReaderHelper();
            crh.setPrimaryReader(instream);
            crh.setFilterChains(filterChains);
            crh.setProject(getProject());
            instream = crh.getAssembledReader();
            String text = crh.readFully(instream);
            if (text != null && text.length() != 0) {
                if (!text.endsWith("\n")) {
                    text = text + "\n";
                }
                tis = new ByteArrayInputStream(text.getBytes(ResourceUtils.ISO_8859_1));
                final Properties props = new Properties();
                props.load(tis);
                Property propertyTask = new Property();
                propertyTask.bindToOwner(this);
                propertyTask.setPrefix(prefix);
                propertyTask.setPrefixValues(prefixValues);
                propertyTask.addProperties(props);
            }
        } catch (final IOException ioe) {
            throw new BuildException("Unable to load file: " + ioe, ioe, getLocation());
        } finally {
            FileUtils.close(bis);
            FileUtils.close(tis);
        }
    }
    public final void addFilterChain(FilterChain filter) {
        filterChains.addElement(filter);
    }
    public synchronized void addConfigured(ResourceCollection a) {
        if (src != null) {
            throw new BuildException("only a single source is supported");
        }
        if (a.size() != 1) {
            throw new BuildException(
                    "only single-element resource collections are supported");
        }
        src = (Resource) a.iterator().next();
    }
    private synchronized JavaResource getRequiredJavaResource() {
        if (src == null) {
            src = new JavaResource();
            src.setProject(getProject());
        } else if (!(src instanceof JavaResource)) {
            throw new BuildException("expected a java resource as source");
        }
        return (JavaResource) src;
    }
}
