package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.util.Vector;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.rmic.RmicAdapter;
import org.apache.tools.ant.taskdefs.rmic.RmicAdapterFactory;
import org.apache.tools.ant.types.FilterSetCollection;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.SourceFileScanner;
import org.apache.tools.ant.util.StringUtils;
import org.apache.tools.ant.util.facade.FacadeTaskHelper;
public class Rmic extends MatchingTask {
    public static final String ERROR_RMIC_FAILED
        = "Rmic failed; see the compiler error output for details.";
    private File baseDir;
    private File destDir;
    private String classname;
    private File sourceBase;
    private String stubVersion;
    private Path compileClasspath;
    private Path extDirs;
    private boolean verify = false;
    private boolean filtering = false;
    private boolean iiop = false;
    private String  iiopOpts;
    private boolean idl  = false;
    private String  idlOpts;
    private boolean debug  = false;
    private boolean includeAntRuntime = true;
    private boolean includeJavaRuntime = false;
    private Vector compileList = new Vector();
    private AntClassLoader loader = null;
    private FacadeTaskHelper facade;
    public static final String ERROR_UNABLE_TO_VERIFY_CLASS = "Unable to verify class ";
    public static final String ERROR_NOT_FOUND = ". It could not be found.";
    public static final String ERROR_NOT_DEFINED = ". It is not defined.";
    public static final String ERROR_LOADING_CAUSED_EXCEPTION = ". Loading caused Exception: ";
    public static final String ERROR_NO_BASE_EXISTS = "base or destdir does not exist: ";
    public static final String ERROR_NOT_A_DIR = "base or destdir is not a directory:";
    public static final String ERROR_BASE_NOT_SET = "base or destdir attribute must be set!";
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private String executable = null;
    private boolean listFiles = false;
    private RmicAdapter nestedAdapter = null;
    public Rmic() {
        facade = new FacadeTaskHelper(RmicAdapterFactory.DEFAULT_COMPILER);
    }
    public void setBase(File base) {
        this.baseDir = base;
    }
    public void setDestdir(File destdir) {
        this.destDir = destdir;
    }
    public File getDestdir() {
        return this.destDir;
    }
    public File getOutputDir() {
        if (getDestdir() != null) {
            return getDestdir();
        }
        return getBase();
    }
    public File getBase() {
        return this.baseDir;
    }
    public void setClassname(String classname) {
        this.classname = classname;
    }
    public String getClassname() {
        return classname;
    }
    public void setSourceBase(File sourceBase) {
        this.sourceBase = sourceBase;
    }
    public File getSourceBase() {
        return sourceBase;
    }
    public void setStubVersion(String stubVersion) {
        this.stubVersion = stubVersion;
    }
    public String getStubVersion() {
        return stubVersion;
    }
    public void setFiltering(boolean filter) {
        this.filtering = filter;
    }
    public boolean getFiltering() {
        return filtering;
    }
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    public boolean getDebug() {
        return debug;
    }
    public synchronized void setClasspath(Path classpath) {
        if (compileClasspath == null) {
            compileClasspath = classpath;
        } else {
            compileClasspath.append(classpath);
        }
    }
    public synchronized Path createClasspath() {
        if (compileClasspath == null) {
            compileClasspath = new Path(getProject());
        }
        return compileClasspath.createPath();
    }
    public void setClasspathRef(Reference pathRef) {
        createClasspath().setRefid(pathRef);
    }
    public Path getClasspath() {
        return compileClasspath;
    }
    public void setVerify(boolean verify) {
        this.verify = verify;
    }
    public boolean getVerify() {
        return verify;
    }
    public void setIiop(boolean iiop) {
        this.iiop = iiop;
    }
    public boolean getIiop() {
        return iiop;
    }
    public void setIiopopts(String iiopOpts) {
        this.iiopOpts = iiopOpts;
    }
    public String getIiopopts() {
        return iiopOpts;
    }
    public void setIdl(boolean idl) {
        this.idl = idl;
    }
    public boolean getIdl() {
        return idl;
    }
    public void setIdlopts(String idlOpts) {
        this.idlOpts = idlOpts;
    }
    public String getIdlopts() {
        return idlOpts;
    }
    public Vector getFileList() {
        return compileList;
    }
    public void setIncludeantruntime(boolean include) {
        includeAntRuntime = include;
    }
    public boolean getIncludeantruntime() {
        return includeAntRuntime;
    }
    public void setIncludejavaruntime(boolean include) {
        includeJavaRuntime = include;
    }
    public boolean getIncludejavaruntime() {
        return includeJavaRuntime;
    }
    public synchronized void setExtdirs(Path extDirs) {
        if (this.extDirs == null) {
            this.extDirs = extDirs;
        } else {
            this.extDirs.append(extDirs);
        }
    }
    public synchronized Path createExtdirs() {
        if (extDirs == null) {
            extDirs = new Path(getProject());
        }
        return extDirs.createPath();
    }
    public Path getExtdirs() {
        return extDirs;
    }
    public Vector getCompileList() {
        return compileList;
    }
    public void setCompiler(String compiler) {
        if (compiler.length() > 0) {
            facade.setImplementation(compiler);
        }
    }
    public String getCompiler() {
        facade.setMagicValue(getProject().getProperty("build.rmic"));
        return facade.getImplementation();
    }
    public ImplementationSpecificArgument createCompilerArg() {
        ImplementationSpecificArgument arg = new ImplementationSpecificArgument();
        facade.addImplementationArgument(arg);
        return arg;
    }
    public String[] getCurrentCompilerArgs() {
        getCompiler();
        return facade.getArgs();
    }
    public void setExecutable(String ex) {
        executable = ex;
    }
    public String getExecutable() {
        return executable;
    }
    public Path createCompilerClasspath() {
        return facade.getImplementationClasspath(getProject());
    }
    public void setListfiles(boolean list) {
        listFiles = list;
    }
    public void add(RmicAdapter adapter) {
        if (nestedAdapter != null) {
            throw new BuildException("Can't have more than one rmic adapter");
        }
        nestedAdapter = adapter;
    }
    public void execute() throws BuildException {
        try {
            compileList.clear();
            File outputDir = getOutputDir();
            if (outputDir == null) {
                throw new BuildException(ERROR_BASE_NOT_SET, getLocation());
            }
            if (!outputDir.exists()) {
                throw new BuildException(ERROR_NO_BASE_EXISTS + outputDir,
                                         getLocation());
            }
            if (!outputDir.isDirectory()) {
                throw new BuildException(ERROR_NOT_A_DIR + outputDir, getLocation());
            }
            if (verify) {
                log("Verify has been turned on.", Project.MSG_VERBOSE);
            }
            RmicAdapter adapter =
                nestedAdapter != null ? nestedAdapter :
                RmicAdapterFactory.getRmic(getCompiler(), this,
                                           createCompilerClasspath());
            adapter.setRmic(this);
            Path classpath = adapter.getClasspath();
            loader = getProject().createClassLoader(classpath);
            if (classname == null) {
                DirectoryScanner ds = this.getDirectoryScanner(baseDir);
                String[] files = ds.getIncludedFiles();
                scanDir(baseDir, files, adapter.getMapper());
            } else {
                String path = classname.replace('.', File.separatorChar)
                    + ".class";
                File f = new File(baseDir, path);
                if (f.isFile()) {
                    scanDir(baseDir, new String[] {path}, adapter.getMapper());
                } else {
                    compileList.add(classname);
                }
            }
            int fileCount = compileList.size();
            if (fileCount > 0) {
                log("RMI Compiling " + fileCount + " class"
                    + (fileCount > 1 ? "es" : "") + " to "
                    + outputDir, Project.MSG_INFO);
                if (listFiles) {
                    for (int i = 0; i < fileCount; i++) {
                        log(compileList.get(i).toString());
                    }
                }
                if (!adapter.execute()) {
                    throw new BuildException(ERROR_RMIC_FAILED, getLocation());
                }
            }
            if (null != sourceBase && !outputDir.equals(sourceBase)
                && fileCount > 0) {
                if (idl) {
                    log("Cannot determine sourcefiles in idl mode, ",
                        Project.MSG_WARN);
                    log("sourcebase attribute will be ignored.",
                        Project.MSG_WARN);
                } else {
                    for (int j = 0; j < fileCount; j++) {
                        moveGeneratedFile(outputDir, sourceBase,
                                          (String) compileList.elementAt(j),
                                          adapter);
                    }
                }
            }
        } finally {
            cleanup();
        }
    }
    protected void cleanup() {
        if (loader != null) {
            loader.cleanup();
            loader = null;
        }
    }
    private void moveGeneratedFile(File baseDir, File sourceBaseFile, String classname,
                                   RmicAdapter adapter) throws BuildException {
        String classFileName = classname.replace('.', File.separatorChar)
            + ".class";
        String[] generatedFiles = adapter.getMapper().mapFileName(classFileName);
        for (int i = 0; i < generatedFiles.length; i++) {
            final String generatedFile = generatedFiles[i];
            if (!generatedFile.endsWith(".class")) {
                continue;
            }
            String sourceFileName = StringUtils.removeSuffix(generatedFile,
                                                             ".class")
                + ".java";
            File oldFile = new File(baseDir, sourceFileName);
            if (!oldFile.exists()) {
                continue;
            }
            File newFile = new File(sourceBaseFile, sourceFileName);
            try {
                if (filtering) {
                    FILE_UTILS.copyFile(oldFile, newFile,
                                        new FilterSetCollection(getProject()
                                                                .getGlobalFilterSet()));
                } else {
                    FILE_UTILS.copyFile(oldFile, newFile);
                }
                oldFile.delete();
            } catch (IOException ioe) {
                String msg = "Failed to copy " + oldFile + " to " + newFile
                    + " due to "
                    + ioe.getMessage();
                throw new BuildException(msg, ioe, getLocation());
            }
        }
    }
    protected void scanDir(File baseDir, String[] files, FileNameMapper mapper) {
        String[] newFiles = files;
        if (idl) {
            log("will leave uptodate test to rmic implementation in idl mode.",
                Project.MSG_VERBOSE);
        } else if (iiop && iiopOpts != null && iiopOpts.indexOf("-always") > -1) {
            log("no uptodate test as -always option has been specified",
                Project.MSG_VERBOSE);
        } else {
            SourceFileScanner sfs = new SourceFileScanner(this);
            newFiles = sfs.restrict(files, baseDir, getOutputDir(), mapper);
        }
        for (int i = 0; i < newFiles.length; i++) {
            String name = newFiles[i].replace(File.separatorChar, '.');
            name = name.substring(0, name.lastIndexOf(".class"));
            compileList.addElement(name);
        }
    }
    public boolean isValidRmiRemote(String classname) {
        try {
            Class testClass = loader.loadClass(classname);
            if (testClass.isInterface() && !iiop && !idl) {
                return false;
            }
            return isValidRmiRemote(testClass);
        } catch (ClassNotFoundException e) {
            log(ERROR_UNABLE_TO_VERIFY_CLASS + classname + ERROR_NOT_FOUND,
                Project.MSG_WARN);
        } catch (NoClassDefFoundError e) {
            log(ERROR_UNABLE_TO_VERIFY_CLASS + classname + ERROR_NOT_DEFINED,
                Project.MSG_WARN);
        } catch (Throwable t) {
            log(ERROR_UNABLE_TO_VERIFY_CLASS + classname
                + ERROR_LOADING_CAUSED_EXCEPTION + t.getMessage(),
                Project.MSG_WARN);
        }
        return false;
    }
    public Class getRemoteInterface(Class testClass) {
        if (Remote.class.isAssignableFrom(testClass)) {
            Class [] interfaces = testClass.getInterfaces();
            if (interfaces != null) {
                for (int i = 0; i < interfaces.length; i++) {
                    if (Remote.class.isAssignableFrom(interfaces[i])) {
                        return interfaces[i];
                    }
                }
            }
        }
        return null;
    }
    private boolean isValidRmiRemote (Class testClass) {
        return getRemoteInterface(testClass) != null;
    }
    public ClassLoader getLoader() {
        return loader;
    }
    public class ImplementationSpecificArgument extends
                                                    org.apache.tools.ant.util.facade.ImplementationSpecificArgument {
        public void setCompiler(String impl) {
            super.setImplementation(impl);
        }
    }
}
