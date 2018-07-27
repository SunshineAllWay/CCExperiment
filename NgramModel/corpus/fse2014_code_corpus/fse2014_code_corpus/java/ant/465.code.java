package org.apache.tools.ant.taskdefs.optional.jsp;
import java.io.File;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.optional.jsp.compilers.JspCompilerAdapter;
import org.apache.tools.ant.taskdefs.optional.jsp.compilers.JspCompilerAdapterFactory;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
public class JspC extends MatchingTask {
    private Path classpath;
    private Path compilerClasspath;
    private Path src;
    private File destDir;
    private String packageName;
    private String compilerName = "jasper";
    private String iepluginid;
    private boolean mapped;
    private int verbose = 0;
    protected Vector compileList = new Vector();
    Vector javaFiles = new Vector();
    protected boolean failOnError = true;
    private File uriroot;
    private File webinc;
    private File webxml;
    protected WebAppParameter webApp;
    private static final String FAIL_MSG
        = "Compile failed, messages should have been provided.";
    public void setSrcDir(Path srcDir) {
        if (src == null) {
            src = srcDir;
        } else {
            src.append(srcDir);
        }
    }
    public Path getSrcDir() {
        return src;
    }
    public void setDestdir(File destDir) {
        this.destDir = destDir;
    }
    public File getDestdir() {
        return destDir;
    }
    public void setPackage(String pkg) {
        this.packageName = pkg;
    }
    public String getPackage() {
        return packageName;
    }
    public void setVerbose(int i) {
        verbose = i;
    }
    public int getVerbose() {
        return verbose;
    }
    public void setFailonerror(boolean fail) {
        failOnError = fail;
    }
    public boolean getFailonerror() {
        return failOnError;
    }
    public String getIeplugin() {
        return iepluginid;
    }
    public void setIeplugin(String iepluginid) {
        this.iepluginid = iepluginid;
    }
    public boolean isMapped() {
        return mapped;
    }
    public void setMapped(boolean mapped) {
        this.mapped = mapped;
    }
    public void setUribase(File uribase) {
        log("Uribase is currently an unused parameter", Project.MSG_WARN);
    }
    public File getUribase() {
        return uriroot;
    }
    public void setUriroot(File uriroot) {
        this.uriroot = uriroot;
    }
    public File getUriroot() {
        return uriroot;
    }
    public void setClasspath(Path cp) {
        if (classpath == null) {
            classpath = cp;
        } else {
            classpath.append(cp);
        }
    }
    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(getProject());
        }
        return classpath.createPath();
    }
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }
    public Path getClasspath() {
        return classpath;
    }
    public void setCompilerclasspath(Path cp) {
        if (compilerClasspath == null) {
            compilerClasspath = cp;
        } else {
            compilerClasspath.append(cp);
        }
    }
    public Path getCompilerclasspath() {
        return compilerClasspath;
    }
    public Path createCompilerclasspath() {
        if (compilerClasspath == null) {
            compilerClasspath = new Path(getProject());
        }
        return compilerClasspath.createPath();
    }
    public void setWebxml(File webxml) {
        this.webxml = webxml;
    }
    public File getWebxml() {
        return this.webxml;
    }
    public void setWebinc(File webinc) {
        this.webinc = webinc;
    }
    public File getWebinc() {
        return this.webinc;
    }
    public void addWebApp(WebAppParameter webappParam)
        throws BuildException {
        if (webApp == null) {
            webApp = webappParam;
        } else {
            throw new BuildException("Only one webapp can be specified");
        }
    }
    public WebAppParameter getWebApp() {
        return webApp;
    }
    public void setCompiler(String compiler) {
        this.compilerName = compiler;
    }
    public Vector getCompileList() {
        return compileList;
    }
    public void execute()
        throws BuildException {
        if (destDir == null) {
            throw new BuildException("destdir attribute must be set!",
                                     getLocation());
        }
        if (!destDir.isDirectory()) {
            throw new BuildException("destination directory \"" + destDir
                                     + "\" does not exist or is not a directory",
                                     getLocation());
        }
        File dest = getActualDestDir();
        AntClassLoader al = null;
        try {
            JspCompilerAdapter compiler =
                JspCompilerAdapterFactory
                .getCompiler(compilerName, this,
                             al = getProject().createClassLoader(compilerClasspath));
            if (webApp != null) {
                doCompilation(compiler);
                return;
            }
            if (src == null) {
                throw new BuildException("srcdir attribute must be set!",
                                         getLocation());
            }
            String [] list = src.list();
            if (list.length == 0) {
                throw new BuildException("srcdir attribute must be set!",
                                         getLocation());
            }
            if (compiler.implementsOwnDependencyChecking()) {
                doCompilation(compiler);
                return;
            }
            JspMangler mangler = compiler.createMangler();
            resetFileLists();
            int filecount = 0;
            for (int i = 0; i < list.length; i++) {
                File srcDir = getProject().resolveFile(list[i]);
                if (!srcDir.exists()) {
                    throw new BuildException("srcdir \"" + srcDir.getPath()
                                             + "\" does not exist!",
                                             getLocation());
                }
                DirectoryScanner ds = this.getDirectoryScanner(srcDir);
                String[] files = ds.getIncludedFiles();
                filecount = files.length;
                scanDir(srcDir, dest, mangler, files);
            }
            log("compiling " + compileList.size() + " files",
                Project.MSG_VERBOSE);
            if (compileList.size() > 0) {
                log("Compiling " + compileList.size() + " source file"
                    + (compileList.size() == 1 ? "" : "s")
                    + " to "
                    + dest);
                doCompilation(compiler);
            } else {
                if (filecount == 0) {
                    log("there were no files to compile", Project.MSG_INFO);
                } else {
                    log("all files are up to date", Project.MSG_VERBOSE);
                }
            }
        } finally {
            if (al != null) {
                al.cleanup();
            }
        }
    }
    private File getActualDestDir() {
        File dest = null;
        if (packageName == null) {
            dest = destDir;
        } else {
            String path = destDir.getPath() + File.separatorChar
                + packageName.replace('.', File.separatorChar);
            dest = new File(path);
        }
        return dest;
    }
    private void doCompilation(JspCompilerAdapter compiler)
        throws BuildException {
        compiler.setJspc(this);
        if (!compiler.execute()) {
            if (failOnError) {
                throw new BuildException(FAIL_MSG, getLocation());
            } else {
                log(FAIL_MSG, Project.MSG_ERR);
            }
        }
    }
    protected void resetFileLists() {
        compileList.removeAllElements();
    }
    protected void scanDir(File srcDir, File dest, JspMangler mangler,
                           String[] files) {
        long now = (new Date()).getTime();
        for (int i = 0; i < files.length; i++) {
            String filename = files[i];
            File srcFile = new File(srcDir, filename);
            File javaFile = mapToJavaFile(mangler, srcFile, srcDir, dest);
            if (javaFile == null) {
                continue;
            }
            if (srcFile.lastModified() > now) {
                log("Warning: file modified in the future: " + filename,
                    Project.MSG_WARN);
            }
            boolean shouldCompile = false;
            shouldCompile = isCompileNeeded(srcFile, javaFile);
            if (shouldCompile) {
                compileList.addElement(srcFile.getAbsolutePath());
                javaFiles.addElement(javaFile);
            }
        }
    }
    private boolean isCompileNeeded(File srcFile, File javaFile) {
        boolean shouldCompile = false;
        if (!javaFile.exists()) {
            shouldCompile = true;
            log("Compiling " + srcFile.getPath()
                + " because java file " + javaFile.getPath()
                + " does not exist", Project.MSG_VERBOSE);
        } else {
            if (srcFile.lastModified() > javaFile.lastModified()) {
                shouldCompile = true;
                log("Compiling " + srcFile.getPath()
                    + " because it is out of date with respect to "
                    + javaFile.getPath(),
                    Project.MSG_VERBOSE);
            } else {
                if (javaFile.length() == 0) {
                    shouldCompile = true;
                    log("Compiling " + srcFile.getPath()
                        + " because java file " + javaFile.getPath()
                        + " is empty", Project.MSG_VERBOSE);
                }
            }
        }
        return shouldCompile;
    }
    protected File mapToJavaFile(JspMangler mangler, File srcFile, File srcDir,
                                 File dest) {
        if (!srcFile.getName().endsWith(".jsp")) {
            return null;
        }
        String javaFileName = mangler.mapJspToJavaName(srcFile);
        return new File(dest, javaFileName);
    }
    public void deleteEmptyJavaFiles() {
        if (javaFiles != null) {
            Enumeration e = javaFiles.elements();
            while (e.hasMoreElements()) {
                File file = (File) e.nextElement();
                if (file.exists() && file.length() == 0) {
                    log("deleting empty output file " + file);
                    file.delete();
                }
            }
        }
    }
    public static class WebAppParameter {
        private File directory;
        public File getDirectory() {
            return directory;
        }
        public void setBaseDir(File directory) {
            this.directory = directory;
        }
    }
}
