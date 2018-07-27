package org.apache.tools.ant.util.depend;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipFile;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.VectorSet;
public abstract class AbstractAnalyzer implements DependencyAnalyzer {
    public static final int MAX_LOOPS = 1000;
    private Path sourcePath = new Path(null);
    private Path classPath = new Path(null);
    private final Vector rootClasses = new VectorSet();
    private boolean determined = false;
    private Vector fileDependencies;
    private Vector classDependencies;
    private boolean closure = true;
    protected AbstractAnalyzer() {
        reset();
    }
    public void setClosure(boolean closure) {
        this.closure = closure;
    }
    public Enumeration getFileDependencies() {
        if (!supportsFileDependencies()) {
            throw new RuntimeException("File dependencies are not supported "
                + "by this analyzer");
        }
        if (!determined) {
            determineDependencies(fileDependencies, classDependencies);
        }
        return fileDependencies.elements();
    }
    public Enumeration getClassDependencies() {
        if (!determined) {
            determineDependencies(fileDependencies, classDependencies);
        }
        return classDependencies.elements();
    }
    public File getClassContainer(String classname) throws IOException {
        String classLocation = classname.replace('.', '/') + ".class";
        return getResourceContainer(classLocation, classPath.list());
    }
    public File getSourceContainer(String classname) throws IOException {
        String sourceLocation = classname.replace('.', '/') + ".java";
        return getResourceContainer(sourceLocation, sourcePath.list());
    }
    public void addSourcePath(Path sourcePath) {
        if (sourcePath == null) {
            return;
        }
        this.sourcePath.append(sourcePath);
        this.sourcePath.setProject(sourcePath.getProject());
    }
    public void addClassPath(Path classPath) {
        if (classPath == null) {
            return;
        }
        this.classPath.append(classPath);
        this.classPath.setProject(classPath.getProject());
    }
    public void addRootClass(String className) {
        if (className == null) {
            return;
        }
        if (!rootClasses.contains(className)) {
            rootClasses.addElement(className);
        }
    }
    public void config(String name, Object info) {
    }
    public void reset() {
        rootClasses.removeAllElements();
        determined = false;
        fileDependencies = new Vector();
        classDependencies = new Vector();
    }
    protected Enumeration getRootClasses() {
        return rootClasses.elements();
    }
    protected boolean isClosureRequired() {
        return closure;
    }
    protected abstract void determineDependencies(Vector files, Vector classes);
    protected abstract boolean supportsFileDependencies();
    private File getResourceContainer(String resourceLocation, String[] paths)
         throws IOException {
        for (int i = 0; i < paths.length; ++i) {
            File element = new File(paths[i]);
            if (!element.exists()) {
                continue;
            }
            if (element.isDirectory()) {
                File resource = new File(element, resourceLocation);
                if (resource.exists()) {
                    return resource;
                }
            } else {
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(element);
                    if (zipFile.getEntry(resourceLocation) != null) {
                        return element;
                    }
                } finally {
                    if (zipFile != null) {
                        zipFile.close();
                    }
                }
            }
        }
        return null;
    }
}
