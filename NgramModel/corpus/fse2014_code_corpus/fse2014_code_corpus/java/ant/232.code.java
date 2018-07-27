package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.UnsupportedEncodingException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.launch.Locator;
import org.apache.tools.ant.util.FileUtils;
public class ManifestClassPath extends Task {
    private String name;
    private File dir;
    private int maxParentLevels = 2;
    private Path path;
    public void execute() {
        if (name == null) {
          throw new BuildException("Missing 'property' attribute!");
        }
        if (dir == null) {
          throw new BuildException("Missing 'jarfile' attribute!");
        }
        if (getProject().getProperty(name) != null) {
          throw new BuildException("Property '" + name + "' already set!");
        }
        if (path == null) {
            throw new BuildException("Missing nested <classpath>!");
        }
        StringBuffer tooLongSb = new StringBuffer();
        for (int i = 0; i < maxParentLevels + 1; i++) {
            tooLongSb.append("../");
        }
        final String tooLongPrefix = tooLongSb.toString();
        final FileUtils fileUtils = FileUtils.getFileUtils();
        dir = fileUtils.normalize(dir.getAbsolutePath());
        String[] elements = path.list();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < elements.length; ++i) {
            File pathEntry = new File(elements[i]);
            String fullPath = pathEntry.getAbsolutePath();
            pathEntry = fileUtils.normalize(fullPath);
            String relPath = null;
            String canonicalPath = null;
            try {
                relPath = FileUtils.getRelativePath(dir, pathEntry);
                canonicalPath = pathEntry.getCanonicalPath();
                if (File.separatorChar != '/') {
                    canonicalPath =
                        canonicalPath.replace(File.separatorChar, '/');
                }
            } catch (Exception e) {
                throw new BuildException("error trying to get the relative path"
                                         + " from " + dir + " to " + fullPath,
                                         e);
            }
            if (relPath.equals(canonicalPath)
                || relPath.startsWith(tooLongPrefix)) {
                throw new BuildException(
                    "No suitable relative path from "
                    + dir + " to " + fullPath);
            }
            if (pathEntry.isDirectory() && !relPath.endsWith("/")) {
                relPath = relPath + '/';
            }
            try {
                relPath = Locator.encodeURI(relPath);
            } catch (UnsupportedEncodingException exc) {
                throw new BuildException(exc);
            }
            buffer.append(relPath);
            buffer.append(' ');
        }
        getProject().setNewProperty(name, buffer.toString().trim());
    }
    public void setProperty(String name) {
        this.name = name;
    }
    public void setJarFile(File jarfile) {
        File parent = jarfile.getParentFile();
        if (!parent.isDirectory()) {
            throw new BuildException("Jar's directory not found: " + parent);
        }
        this.dir = parent;
    }
    public void setMaxParentLevels(int levels) {
        if (levels < 0) {
            throw new BuildException("maxParentLevels must not be a negative"
                                     + " number");
        }
        this.maxParentLevels = levels;
    }
    public void addClassPath(Path path) {
        this.path = path;
    }
}
