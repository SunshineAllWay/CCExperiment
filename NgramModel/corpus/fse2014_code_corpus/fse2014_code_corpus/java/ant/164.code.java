package org.apache.tools.ant.taskdefs;
import java.io.File;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.StringUtils;
public class Available extends Task implements Condition {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private String property;
    private String classname;
    private String filename;
    private File file;
    private Path filepath;
    private String resource;
    private FileDir type;
    private Path classpath;
    private AntClassLoader loader;
    private Object value = "true";
    private boolean isTask = false;
    private boolean ignoreSystemclasses = false;
    private boolean searchParents   = false;
    public void setSearchParents(boolean  searchParents) {
        this.searchParents = searchParents;
    }
    public void setClasspath(Path classpath) {
        createClasspath().append(classpath);
    }
    public Path createClasspath() {
        if (this.classpath == null) {
            this.classpath = new Path(getProject());
        }
        return this.classpath.createPath();
    }
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }
    public void setFilepath(Path filepath) {
        createFilepath().append(filepath);
    }
    public Path createFilepath() {
        if (this.filepath == null) {
            this.filepath = new Path(getProject());
        }
        return this.filepath.createPath();
    }
    public void setProperty(String property) {
        this.property = property;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    public void setValue(String value) {
        setValue((Object) value);
    }
    public void setClassname(String classname) {
        if (!"".equals(classname)) {
            this.classname = classname;
        }
    }
    public void setFile(File file) {
        this.file = file;
        this.filename = FILE_UTILS.removeLeadingPath(getProject().getBaseDir(), file);
    }
    public void setResource(String resource) {
        this.resource = resource;
    }
    public void setType(String type) {
        log("DEPRECATED - The setType(String) method has been deprecated."
            + " Use setType(Available.FileDir) instead.",
            Project.MSG_WARN);
        this.type = new FileDir();
        this.type.setValue(type);
    }
    public void setType(FileDir type) {
        this.type = type;
    }
    public void setIgnoresystemclasses(boolean ignore) {
        this.ignoreSystemclasses = ignore;
    }
    public void execute() throws BuildException {
        if (property == null) {
            throw new BuildException("property attribute is required",
                                     getLocation());
        }
        isTask = true;
        try {
            if (eval()) {
                PropertyHelper ph = PropertyHelper.getPropertyHelper(getProject());
                Object oldvalue = ph.getProperty(property);
                if (null != oldvalue && !oldvalue.equals(value)) {
                    log("DEPRECATED - <available> used to override an existing"
                        + " property."
                        + StringUtils.LINE_SEP
                        + "  Build file should not reuse the same property"
                        + " name for different values.",
                        Project.MSG_WARN);
                }
                ph.setProperty(property, value, true);
            }
        } finally {
            isTask = false;
        }
    }
    public boolean eval() throws BuildException {
        try {
            if (classname == null && file == null && resource == null) {
                throw new BuildException("At least one of (classname|file|"
                                         + "resource) is required", getLocation());
            }
            if (type != null) {
                if (file == null) {
                    throw new BuildException("The type attribute is only valid "
                                             + "when specifying the file "
                                             + "attribute.", getLocation());
                }
            }
            if (classpath != null) {
                classpath.setProject(getProject());
                this.loader = getProject().createClassLoader(classpath);
            }
            String appendix = "";
            if (isTask) {
                appendix = " to set property " + property;
            } else {
                setTaskName("available");
            }
            if ((classname != null) && !checkClass(classname)) {
                log("Unable to load class " + classname + appendix,
                    Project.MSG_VERBOSE);
                return false;
            }
            if ((file != null) && !checkFile()) {
                StringBuffer buf = new StringBuffer("Unable to find ");
                if (type != null) {
                    buf.append(type).append(' ');
                }
                buf.append(filename).append(appendix);
                log(buf.toString(), Project.MSG_VERBOSE);
                return false;
            }
            if ((resource != null) && !checkResource(resource)) {
                log("Unable to load resource " + resource + appendix,
                    Project.MSG_VERBOSE);
                return false;
            }
        } finally {
            if (loader != null) {
                loader.cleanup();
                loader = null;
            }
            if (!isTask) {
                setTaskName(null);
            }
        }
        return true;
    }
    private boolean checkFile() {
        if (filepath == null) {
            return checkFile(file, filename);
        } else {
            String[] paths = filepath.list();
            for (int i = 0; i < paths.length; ++i) {
                log("Searching " + paths[i], Project.MSG_VERBOSE);
                File path = new File(paths[i]);
                if (path.exists()
                    && (filename.equals(paths[i])
                        || filename.equals(path.getName()))) {
                    if (type == null) {
                        log("Found: " + path, Project.MSG_VERBOSE);
                        return true;
                    } else if (type.isDir()
                               && path.isDirectory()) {
                        log("Found directory: " + path, Project.MSG_VERBOSE);
                        return true;
                    } else if (type.isFile()
                               && path.isFile()) {
                        log("Found file: " + path, Project.MSG_VERBOSE);
                        return true;
                    }
                    return false;
                }
                File parent = path.getParentFile();
                if (parent != null && parent.exists()
                    && filename.equals(parent.getAbsolutePath())) {
                    if (type == null) {
                        log("Found: " + parent, Project.MSG_VERBOSE);
                        return true;
                    } else if (type.isDir()) {
                        log("Found directory: " + parent, Project.MSG_VERBOSE);
                        return true;
                    }
                    return false;
                }
                if (path.exists() && path.isDirectory()) {
                    if (checkFile(new File(path, filename),
                                  filename + " in " + path)) {
                        return true;
                    }
                }
                while (searchParents && parent != null && parent.exists()) {
                    if (checkFile(new File(parent, filename),
                                  filename + " in " + parent)) {
                        return true;
                    }
                    parent = parent.getParentFile();
                }
            }
        }
        return false;
    }
    private boolean checkFile(File f, String text) {
        if (type != null) {
            if (type.isDir()) {
                if (f.isDirectory()) {
                    log("Found directory: " + text, Project.MSG_VERBOSE);
                }
                return f.isDirectory();
            } else if (type.isFile()) {
                if (f.isFile()) {
                    log("Found file: " + text, Project.MSG_VERBOSE);
                }
                return f.isFile();
            }
        }
        if (f.exists()) {
            log("Found: " + text, Project.MSG_VERBOSE);
        }
        return f.exists();
    }
    private boolean checkResource(String resource) {
        if (loader != null) {
            return (loader.getResourceAsStream(resource) != null);
        } else {
            ClassLoader cL = this.getClass().getClassLoader();
            if (cL != null) {
                return (cL.getResourceAsStream(resource) != null);
            } else {
                return
                    (ClassLoader.getSystemResourceAsStream(resource) != null);
            }
        }
    }
    private boolean checkClass(String classname) {
        try {
            if (ignoreSystemclasses) {
                loader = getProject().createClassLoader(classpath);
                loader.setParentFirst(false);
                loader.addJavaLibraries();
                if (loader != null) {
                    try {
                        loader.findClass(classname);
                    } catch (SecurityException se) {
                        return true;
                    }
                } else {
                    return false;
                }
            } else if (loader != null) {
                loader.loadClass(classname);
            } else {
                ClassLoader l = this.getClass().getClassLoader();
                if (l != null) {
                    Class.forName(classname, true, l);
                } else {
                    Class.forName(classname);
                }
            }
            return true;
        } catch (ClassNotFoundException e) {
            log("class \"" + classname + "\" was not found",
                Project.MSG_DEBUG);
            return false;
        } catch (NoClassDefFoundError e) {
            log("Could not load dependent class \"" + e.getMessage()
                + "\" for class \"" + classname + "\"",
                Project.MSG_DEBUG);
            return false;
        }
    }
    public static class FileDir extends EnumeratedAttribute {
        private static final String[] VALUES = {"file", "dir"};
        public String[] getValues() {
            return VALUES;
        }
        public boolean isDir() {
            return "dir".equalsIgnoreCase(getValue());
        }
        public boolean isFile() {
            return "file".equalsIgnoreCase(getValue());
        }
    }
}
