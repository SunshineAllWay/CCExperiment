package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapter;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapterExtension;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapterFactory;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.ant.util.JavaEnvUtils;
import org.apache.tools.ant.util.SourceFileScanner;
import org.apache.tools.ant.util.facade.FacadeTaskHelper;
public class Javac extends MatchingTask {
    private static final String FAIL_MSG
        = "Compile failed; see the compiler error output for details.";
    private static final String JAVAC17 = "javac1.7";
    private static final String JAVAC16 = "javac1.6";
    private static final String JAVAC15 = "javac1.5";
    private static final String JAVAC14 = "javac1.4";
    private static final String JAVAC13 = "javac1.3";
    private static final String JAVAC12 = "javac1.2";
    private static final String JAVAC11 = "javac1.1";
    private static final String MODERN = "modern";
    private static final String CLASSIC = "classic";
    private static final String EXTJAVAC = "extJavac";
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private Path src;
    private File destDir;
    private Path compileClasspath;
    private Path compileSourcepath;
    private String encoding;
    private boolean debug = false;
    private boolean optimize = false;
    private boolean deprecation = false;
    private boolean depend = false;
    private boolean verbose = false;
    private String targetAttribute;
    private Path bootclasspath;
    private Path extdirs;
    private Boolean includeAntRuntime;
    private boolean includeJavaRuntime = false;
    private boolean fork = false;
    private String forkedExecutable = null;
    private boolean nowarn = false;
    private String memoryInitialSize;
    private String memoryMaximumSize;
    private FacadeTaskHelper facade = null;
    protected boolean failOnError = true;
    protected boolean listFiles = false;
    protected File[] compileList = new File[0];
    private Map packageInfos = new HashMap();
    private String source;
    private String debugLevel;
    private File tmpDir;
    private String updatedProperty;
    private String errorProperty;
    private boolean taskSuccess = true; 
    private boolean includeDestClasses = true;
    private CompilerAdapter nestedAdapter = null;
    public Javac() {
        facade = new FacadeTaskHelper(assumedJavaVersion());
    }
    private String assumedJavaVersion() {
        if (JavaEnvUtils.isJavaVersion(JavaEnvUtils.JAVA_1_4)) {
            return JAVAC14;
        } else if (JavaEnvUtils.isJavaVersion(JavaEnvUtils.JAVA_1_5)) {
            return JAVAC15;
        } else if (JavaEnvUtils.isJavaVersion(JavaEnvUtils.JAVA_1_6)) {
            return JAVAC16;
        } else if (JavaEnvUtils.isJavaVersion(JavaEnvUtils.JAVA_1_7)) {
            return JAVAC17;
        } else {
            return CLASSIC;
        }
    }
    public String getDebugLevel() {
        return debugLevel;
    }
    public void setDebugLevel(String  v) {
        this.debugLevel = v;
    }
    public String getSource() {
        return source != null
            ? source : getProject().getProperty(MagicNames.BUILD_JAVAC_SOURCE);
    }
    public void setSource(String  v) {
        this.source = v;
    }
    public Path createSrc() {
        if (src == null) {
            src = new Path(getProject());
        }
        return src.createPath();
    }
    protected Path recreateSrc() {
        src = null;
        return createSrc();
    }
    public void setSrcdir(Path srcDir) {
        if (src == null) {
            src = srcDir;
        } else {
            src.append(srcDir);
        }
    }
    public Path getSrcdir() {
        return src;
    }
    public void setDestdir(File destDir) {
        this.destDir = destDir;
    }
    public File getDestdir() {
        return destDir;
    }
    public void setSourcepath(Path sourcepath) {
        if (compileSourcepath == null) {
            compileSourcepath = sourcepath;
        } else {
            compileSourcepath.append(sourcepath);
        }
    }
    public Path getSourcepath() {
        return compileSourcepath;
    }
    public Path createSourcepath() {
        if (compileSourcepath == null) {
            compileSourcepath = new Path(getProject());
        }
        return compileSourcepath.createPath();
    }
    public void setSourcepathRef(Reference r) {
        createSourcepath().setRefid(r);
    }
    public void setClasspath(Path classpath) {
        if (compileClasspath == null) {
            compileClasspath = classpath;
        } else {
            compileClasspath.append(classpath);
        }
    }
    public Path getClasspath() {
        return compileClasspath;
    }
    public Path createClasspath() {
        if (compileClasspath == null) {
            compileClasspath = new Path(getProject());
        }
        return compileClasspath.createPath();
    }
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }
    public void setBootclasspath(Path bootclasspath) {
        if (this.bootclasspath == null) {
            this.bootclasspath = bootclasspath;
        } else {
            this.bootclasspath.append(bootclasspath);
        }
    }
    public Path getBootclasspath() {
        return bootclasspath;
    }
    public Path createBootclasspath() {
        if (bootclasspath == null) {
            bootclasspath = new Path(getProject());
        }
        return bootclasspath.createPath();
    }
    public void setBootClasspathRef(Reference r) {
        createBootclasspath().setRefid(r);
    }
    public void setExtdirs(Path extdirs) {
        if (this.extdirs == null) {
            this.extdirs = extdirs;
        } else {
            this.extdirs.append(extdirs);
        }
    }
    public Path getExtdirs() {
        return extdirs;
    }
    public Path createExtdirs() {
        if (extdirs == null) {
            extdirs = new Path(getProject());
        }
        return extdirs.createPath();
    }
    public void setListfiles(boolean list) {
        listFiles = list;
    }
    public boolean getListfiles() {
        return listFiles;
    }
    public void setFailonerror(boolean fail) {
        failOnError = fail;
    }
    public void setProceed(boolean proceed) {
        failOnError = !proceed;
    }
    public boolean getFailonerror() {
        return failOnError;
    }
    public void setDeprecation(boolean deprecation) {
        this.deprecation = deprecation;
    }
    public boolean getDeprecation() {
        return deprecation;
    }
    public void setMemoryInitialSize(String memoryInitialSize) {
        this.memoryInitialSize = memoryInitialSize;
    }
    public String getMemoryInitialSize() {
        return memoryInitialSize;
    }
    public void setMemoryMaximumSize(String memoryMaximumSize) {
        this.memoryMaximumSize = memoryMaximumSize;
    }
    public String getMemoryMaximumSize() {
        return memoryMaximumSize;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public String getEncoding() {
        return encoding;
    }
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    public boolean getDebug() {
        return debug;
    }
    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }
    public boolean getOptimize() {
        return optimize;
    }
    public void setDepend(boolean depend) {
        this.depend = depend;
    }
    public boolean getDepend() {
        return depend;
    }
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    public boolean getVerbose() {
        return verbose;
    }
    public void setTarget(String target) {
        this.targetAttribute = target;
    }
    public String getTarget() {
        return targetAttribute != null
            ? targetAttribute
            : getProject().getProperty(MagicNames.BUILD_JAVAC_TARGET);
    }
    public void setIncludeantruntime(boolean include) {
        includeAntRuntime = Boolean.valueOf(include);
    }
    public boolean getIncludeantruntime() {
        return includeAntRuntime != null ? includeAntRuntime.booleanValue() : true;
    }
    public void setIncludejavaruntime(boolean include) {
        includeJavaRuntime = include;
    }
    public boolean getIncludejavaruntime() {
        return includeJavaRuntime;
    }
    public void setFork(boolean f) {
        fork = f;
    }
    public void setExecutable(String forkExec) {
        forkedExecutable = forkExec;
    }
    public String getExecutable() {
        return forkedExecutable;
    }
    public boolean isForkedJavac() {
        return fork || EXTJAVAC.equalsIgnoreCase(getCompiler());
    }
    public String getJavacExecutable() {
        if (forkedExecutable == null && isForkedJavac()) {
            forkedExecutable = getSystemJavac();
        } else if (forkedExecutable != null && !isForkedJavac()) {
            forkedExecutable = null;
        }
        return forkedExecutable;
    }
    public void setNowarn(boolean flag) {
        this.nowarn = flag;
    }
    public boolean getNowarn() {
        return nowarn;
    }
    public ImplementationSpecificArgument createCompilerArg() {
        ImplementationSpecificArgument arg =
            new ImplementationSpecificArgument();
        facade.addImplementationArgument(arg);
        return arg;
    }
    public String[] getCurrentCompilerArgs() {
        String chosen = facade.getExplicitChoice();
        try {
            String appliedCompiler = getCompiler();
            facade.setImplementation(appliedCompiler);
            String[] result = facade.getArgs();
            String altCompilerName = getAltCompilerName(facade.getImplementation());
            if (result.length == 0 && altCompilerName != null) {
                facade.setImplementation(altCompilerName);
                result = facade.getArgs();
            }
            return result;
        } finally {
            facade.setImplementation(chosen);
        }
    }
    private String getAltCompilerName(String anImplementation) {
        if (JAVAC17.equalsIgnoreCase(anImplementation)
                || JAVAC16.equalsIgnoreCase(anImplementation)
                || JAVAC15.equalsIgnoreCase(anImplementation)
                || JAVAC14.equalsIgnoreCase(anImplementation)
                || JAVAC13.equalsIgnoreCase(anImplementation)) {
            return MODERN;
        }
        if (JAVAC12.equalsIgnoreCase(anImplementation)
                || JAVAC11.equalsIgnoreCase(anImplementation)) {
            return CLASSIC;
        }
        if (MODERN.equalsIgnoreCase(anImplementation)) {
            String nextSelected = assumedJavaVersion();
            if (JAVAC17.equalsIgnoreCase(nextSelected)
                    || JAVAC16.equalsIgnoreCase(nextSelected)
                    || JAVAC15.equalsIgnoreCase(nextSelected)
                    || JAVAC14.equalsIgnoreCase(nextSelected)
                    || JAVAC13.equalsIgnoreCase(nextSelected)) {
                return nextSelected;
            }
        }
        if (CLASSIC.equalsIgnoreCase(anImplementation)) {
            return assumedJavaVersion();
        }
        if (EXTJAVAC.equalsIgnoreCase(anImplementation)) {
            return assumedJavaVersion();
        }
        return null;
    }
    public void setTempdir(File tmpDir) {
        this.tmpDir = tmpDir;
    }
    public File getTempdir() {
        return tmpDir;
    }
    public void setUpdatedProperty(String updatedProperty) {
        this.updatedProperty = updatedProperty;
    }
    public void setErrorProperty(String errorProperty) {
        this.errorProperty = errorProperty;
    }
    public void setIncludeDestClasses(boolean includeDestClasses) {
        this.includeDestClasses = includeDestClasses;
    }
    public boolean isIncludeDestClasses() {
        return includeDestClasses;
    }
    public boolean getTaskSuccess() {
        return taskSuccess;
    }
    public Path createCompilerClasspath() {
        return facade.getImplementationClasspath(getProject());
    }
    public void add(CompilerAdapter adapter) {
        if (nestedAdapter != null) {
            throw new BuildException("Can't have more than one compiler"
                                     + " adapter");
        }
        nestedAdapter = adapter;
    }
    public void execute() throws BuildException {
        checkParameters();
        resetFileLists();
        String[] list = src.list();
        for (int i = 0; i < list.length; i++) {
            File srcDir = getProject().resolveFile(list[i]);
            if (!srcDir.exists()) {
                throw new BuildException("srcdir \""
                                         + srcDir.getPath()
                                         + "\" does not exist!", getLocation());
            }
            DirectoryScanner ds = this.getDirectoryScanner(srcDir);
            String[] files = ds.getIncludedFiles();
            scanDir(srcDir, destDir != null ? destDir : srcDir, files);
        }
        compile();
        if (updatedProperty != null
            && taskSuccess
            && compileList.length != 0) {
            getProject().setNewProperty(updatedProperty, "true");
        }
    }
    protected void resetFileLists() {
        compileList = new File[0];
        packageInfos = new HashMap();
    }
    protected void scanDir(File srcDir, File destDir, String[] files) {
        GlobPatternMapper m = new GlobPatternMapper();
        String[] extensions = findSupportedFileExtensions();
        for (int i = 0; i < extensions.length; i++) {
            m.setFrom(extensions[i]);
            m.setTo("*.class");
            SourceFileScanner sfs = new SourceFileScanner(this);
            File[] newFiles = sfs.restrictAsFiles(files, srcDir, destDir, m);
            if (newFiles.length > 0) {
                lookForPackageInfos(srcDir, newFiles);
                File[] newCompileList
                    = new File[compileList.length + newFiles.length];
                System.arraycopy(compileList, 0, newCompileList, 0,
                                 compileList.length);
                System.arraycopy(newFiles, 0, newCompileList,
                                 compileList.length, newFiles.length);
                compileList = newCompileList;
            }
        }
    }
    private String[] findSupportedFileExtensions() {
        String compilerImpl = getCompiler();
        CompilerAdapter adapter =
            nestedAdapter != null ? nestedAdapter :
            CompilerAdapterFactory.getCompiler(compilerImpl, this,
                                               createCompilerClasspath());
        String[] extensions = null;
        if (adapter instanceof CompilerAdapterExtension) {
            extensions =
                ((CompilerAdapterExtension) adapter).getSupportedFileExtensions();
        } 
        if (extensions == null) {
            extensions = new String[] { "java" };
        }
        for (int i = 0; i < extensions.length; i++) {
            if (!extensions[i].startsWith("*.")) {
                extensions[i] = "*." + extensions[i];
            }
        }
        return extensions; 
    }
    public File[] getFileList() {
        return compileList;
    }
    protected boolean isJdkCompiler(String compilerImpl) {
        return MODERN.equals(compilerImpl)
            || CLASSIC.equals(compilerImpl)
            || JAVAC17.equals(compilerImpl)
            || JAVAC16.equals(compilerImpl)
            || JAVAC15.equals(compilerImpl)
            || JAVAC14.equals(compilerImpl)
            || JAVAC13.equals(compilerImpl)
            || JAVAC12.equals(compilerImpl)
            || JAVAC11.equals(compilerImpl);
    }
    protected String getSystemJavac() {
        return JavaEnvUtils.getJdkExecutable("javac");
    }
    public void setCompiler(String compiler) {
        facade.setImplementation(compiler);
    }
    public String getCompiler() {
        String compilerImpl = getCompilerVersion();
        if (fork) {
            if (isJdkCompiler(compilerImpl)) {
                compilerImpl = EXTJAVAC;
            } else {
                log("Since compiler setting isn't classic or modern, "
                    + "ignoring fork setting.", Project.MSG_WARN);
            }
        }
        return compilerImpl;
    }
    public String getCompilerVersion() {
        facade.setMagicValue(getProject().getProperty("build.compiler"));
        return facade.getImplementation();
    }
    protected void checkParameters() throws BuildException {
        if (src == null) {
            throw new BuildException("srcdir attribute must be set!",
                                     getLocation());
        }
        if (src.size() == 0) {
            throw new BuildException("srcdir attribute must be set!",
                                     getLocation());
        }
        if (destDir != null && !destDir.isDirectory()) {
            throw new BuildException("destination directory \""
                                     + destDir
                                     + "\" does not exist "
                                     + "or is not a directory", getLocation());
        }
        if (includeAntRuntime == null && getProject().getProperty("build.sysclasspath") == null) {
            log(getLocation() + "warning: 'includeantruntime' was not set, " +
                    "defaulting to build.sysclasspath=last; set to false for repeatable builds",
                    Project.MSG_WARN);
        }
    }
    protected void compile() {
        String compilerImpl = getCompiler();
        if (compileList.length > 0) {
            log("Compiling " + compileList.length + " source file"
                + (compileList.length == 1 ? "" : "s")
                + (destDir != null ? " to " + destDir : ""));
            if (listFiles) {
                for (int i = 0; i < compileList.length; i++) {
                  String filename = compileList[i].getAbsolutePath();
                  log(filename);
                }
            }
            CompilerAdapter adapter =
                nestedAdapter != null ? nestedAdapter :
                CompilerAdapterFactory.getCompiler(compilerImpl, this,
                                                   createCompilerClasspath());
            adapter.setJavac(this);
            if (adapter.execute()) {
                try {
                    generateMissingPackageInfoClasses();
                } catch (IOException x) {
                    throw new BuildException(x, getLocation());
                }
            } else {
                this.taskSuccess = false;
                if (errorProperty != null) {
                    getProject().setNewProperty(
                        errorProperty, "true");
                }
                if (failOnError) {
                    throw new BuildException(FAIL_MSG, getLocation());
                } else {
                    log(FAIL_MSG, Project.MSG_ERR);
                }
            }
        }
    }
    public class ImplementationSpecificArgument extends
        org.apache.tools.ant.util.facade.ImplementationSpecificArgument {
        public void setCompiler(String impl) {
            super.setImplementation(impl);
        }
    }
    private void lookForPackageInfos(File srcDir, File[] newFiles) {
        for (int i = 0; i < newFiles.length; i++) {
            File f = newFiles[i];
            if (!f.getName().equals("package-info.java")) {
                continue;
            }
            String path = FILE_UTILS.removeLeadingPath(srcDir, f).
                    replace(File.separatorChar, '/');
            String suffix = "/package-info.java";
            if (!path.endsWith(suffix)) {
                log("anomalous package-info.java path: " + path, Project.MSG_WARN);
                continue;
            }
            String pkg = path.substring(0, path.length() - suffix.length());
            packageInfos.put(pkg, new Long(f.lastModified()));
        }
    }
    private void generateMissingPackageInfoClasses() throws IOException {
        for (Iterator i = packageInfos.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            String pkg = (String) entry.getKey();
            Long sourceLastMod = (Long) entry.getValue();
            File pkgBinDir = new File(destDir, pkg.replace('/', File.separatorChar));
            pkgBinDir.mkdirs();
            File pkgInfoClass = new File(pkgBinDir, "package-info.class");
            if (pkgInfoClass.isFile() && pkgInfoClass.lastModified() >= sourceLastMod.longValue()) {
                continue;
            }
            log("Creating empty " + pkgInfoClass);
            OutputStream os = new FileOutputStream(pkgInfoClass);
            try {
                os.write(PACKAGE_INFO_CLASS_HEADER);
                byte[] name = pkg.getBytes("UTF-8");
                int length = name.length +  13;
                os.write((byte) length / 256);
                os.write((byte) length % 256);
                os.write(name);
                os.write(PACKAGE_INFO_CLASS_FOOTER);
            } finally {
                os.close();
            }
        }
    }
    private static final byte[] PACKAGE_INFO_CLASS_HEADER = {
        (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe, 0x00, 0x00, 0x00,
        0x31, 0x00, 0x07, 0x07, 0x00, 0x05, 0x07, 0x00, 0x06, 0x01, 0x00, 0x0a,
        0x53, 0x6f, 0x75, 0x72, 0x63, 0x65, 0x46, 0x69, 0x6c, 0x65, 0x01, 0x00,
        0x11, 0x70, 0x61, 0x63, 0x6b, 0x61, 0x67, 0x65, 0x2d, 0x69, 0x6e, 0x66,
        0x6f, 0x2e, 0x6a, 0x61, 0x76, 0x61, 0x01
    };
    private static final byte[] PACKAGE_INFO_CLASS_FOOTER = {
        0x2f, 0x70, 0x61, 0x63, 0x6b, 0x61, 0x67, 0x65, 0x2d, 0x69, 0x6e, 0x66,
        0x6f, 0x01, 0x00, 0x10, 0x6a, 0x61, 0x76, 0x61, 0x2f, 0x6c, 0x61, 0x6e,
        0x67, 0x2f, 0x4f, 0x62, 0x6a, 0x65, 0x63, 0x74, 0x02, 0x00, 0x00, 0x01,
        0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x03,
        0x00, 0x00, 0x00, 0x02, 0x00, 0x04
    };
}
