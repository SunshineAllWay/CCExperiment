package org.apache.tools.ant.types.optional.depend;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.depend.DependencyAnalyzer;
public class DependScanner extends DirectoryScanner {
    public static final String DEFAULT_ANALYZER_CLASS
        = "org.apache.tools.ant.util.depend.bcel.FullAnalyzer";
    private Vector rootClasses;
    private Vector included;
    private Vector additionalBaseDirs = new Vector();
    private DirectoryScanner parentScanner;
    public DependScanner(DirectoryScanner parentScanner) {
        this.parentScanner = parentScanner;
    }
    public synchronized void setRootClasses(Vector rootClasses) {
        this.rootClasses = rootClasses;
    }
    public String[] getIncludedFiles() {
        String[] files = new String[getIncludedFilesCount()];
        for (int i = 0; i < files.length; i++) {
            files[i] = (String) included.elementAt(i);
        }
        return files;
    }
    public synchronized int getIncludedFilesCount() {
        if (included == null) {
            throw new IllegalStateException();
        }
        return included.size();
    }
    public synchronized void scan() throws IllegalStateException {
        included = new Vector();
        String analyzerClassName = DEFAULT_ANALYZER_CLASS;
        DependencyAnalyzer analyzer = null;
        try {
            Class analyzerClass = Class.forName(analyzerClassName);
            analyzer = (DependencyAnalyzer) analyzerClass.newInstance();
        } catch (Exception e) {
            throw new BuildException("Unable to load dependency analyzer: "
                                     + analyzerClassName, e);
        }
        analyzer.addClassPath(new Path(null, basedir.getPath()));
        for (Enumeration e = additionalBaseDirs.elements(); e.hasMoreElements();) {
            File additionalBaseDir = (File) e.nextElement();
            analyzer.addClassPath(new Path(null, additionalBaseDir.getPath()));
        }
        for (Enumeration e = rootClasses.elements(); e.hasMoreElements();) {
            String rootClass = (String) e.nextElement();
            analyzer.addRootClass(rootClass);
        }
        Enumeration e = analyzer.getClassDependencies();
        String[] parentFiles = parentScanner.getIncludedFiles();
        Hashtable parentSet = new Hashtable();
        for (int i = 0; i < parentFiles.length; ++i) {
            parentSet.put(parentFiles[i], parentFiles[i]);
        }
        while (e.hasMoreElements()) {
            String classname = (String) e.nextElement();
            String filename = classname.replace('.', File.separatorChar);
            filename = filename + ".class";
            File depFile = new File(basedir, filename);
            if (depFile.exists() && parentSet.containsKey(filename)) {
                included.addElement(filename);
            }
        }
    }
    public void addDefaultExcludes() {
    }
    public String[] getExcludedDirectories() {
        return null;
    }
    public String[] getExcludedFiles() {
        return null;
    }
    public String[] getIncludedDirectories() {
        return new String[0];
    }
    public int getIncludedDirsCount() {
        return 0;
    }
    public String[] getNotIncludedDirectories() {
        return null;
    }
    public String[] getNotIncludedFiles() {
        return null;
    }
    public void setExcludes(String[] excludes) {
    }
    public void setIncludes(String[] includes) {
    }
    public void setCaseSensitive(boolean isCaseSensitive) {
    }
    public void addBasedir(File baseDir) {
        additionalBaseDirs.addElement(baseDir);
    }
}
