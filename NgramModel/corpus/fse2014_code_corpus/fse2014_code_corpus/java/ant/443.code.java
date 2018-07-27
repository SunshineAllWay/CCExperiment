package org.apache.tools.ant.taskdefs.optional.extension.resolvers;
import java.io.File;
import java.net.URL;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Get;
import org.apache.tools.ant.taskdefs.optional.extension.Extension;
import org.apache.tools.ant.taskdefs.optional.extension.ExtensionResolver;
public class URLResolver implements ExtensionResolver {
    private File destfile;
    private File destdir;
    private URL url;
    public void setUrl(final URL url) {
        this.url = url;
    }
    public void setDestfile(final File destfile) {
        this.destfile = destfile;
    }
    public void setDestdir(final File destdir) {
        this.destdir = destdir;
    }
    public File resolve(final Extension extension,
                         final Project project) throws BuildException {
        validate();
        final File file = getDest();
        final Get get = new Get();
        get.setProject(project);
        get.setDest(file);
        get.setSrc(url);
        get.execute();
        return file;
    }
    private File getDest() {
        File result;
        if (null != destfile) {
            result = destfile;
        } else {
            final String file = url.getFile();
            String filename;
            if (null == file || file.length() <= 1) {
                filename = "default.file";
            } else {
                int index = file.lastIndexOf('/');
                if (-1 == index) {
                    index = 0;
                }
                filename = file.substring(index);
            }
            result = new File(destdir, filename);
        }
        return result;
    }
    private void validate() {
        if (null == url) {
            final String message = "Must specify URL";
            throw new BuildException(message);
        }
        if (null == destdir && null == destfile) {
            final String message = "Must specify destination file or directory";
            throw new BuildException(message);
        } else if (null != destdir && null != destfile) {
            final String message = "Must not specify both destination file or directory";
            throw new BuildException(message);
        }
    }
    public String toString() {
        return "URL[" + url + "]";
    }
}
