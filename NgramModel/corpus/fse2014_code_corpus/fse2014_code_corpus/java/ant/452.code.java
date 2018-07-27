package org.apache.tools.ant.taskdefs.optional.javacc;
import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.JavaEnvUtils;
public class JavaCC extends Task {
    private static final String LOOKAHEAD              = "LOOKAHEAD";
    private static final String CHOICE_AMBIGUITY_CHECK = "CHOICE_AMBIGUITY_CHECK";
    private static final String OTHER_AMBIGUITY_CHECK  = "OTHER_AMBIGUITY_CHECK";
    private static final String STATIC                 = "STATIC";
    private static final String DEBUG_PARSER           = "DEBUG_PARSER";
    private static final String DEBUG_LOOKAHEAD        = "DEBUG_LOOKAHEAD";
    private static final String DEBUG_TOKEN_MANAGER    = "DEBUG_TOKEN_MANAGER";
    private static final String OPTIMIZE_TOKEN_MANAGER = "OPTIMIZE_TOKEN_MANAGER";
    private static final String ERROR_REPORTING        = "ERROR_REPORTING";
    private static final String JAVA_UNICODE_ESCAPE    = "JAVA_UNICODE_ESCAPE";
    private static final String UNICODE_INPUT          = "UNICODE_INPUT";
    private static final String IGNORE_CASE            = "IGNORE_CASE";
    private static final String COMMON_TOKEN_ACTION    = "COMMON_TOKEN_ACTION";
    private static final String USER_TOKEN_MANAGER     = "USER_TOKEN_MANAGER";
    private static final String USER_CHAR_STREAM       = "USER_CHAR_STREAM";
    private static final String BUILD_PARSER           = "BUILD_PARSER";
    private static final String BUILD_TOKEN_MANAGER    = "BUILD_TOKEN_MANAGER";
    private static final String SANITY_CHECK           = "SANITY_CHECK";
    private static final String FORCE_LA_CHECK         = "FORCE_LA_CHECK";
    private static final String CACHE_TOKENS           = "CACHE_TOKENS";
    private static final String KEEP_LINE_COLUMN       = "KEEP_LINE_COLUMN";
    private static final String JDK_VERSION            = "JDK_VERSION";
    private final Hashtable optionalAttrs = new Hashtable();
    private File outputDirectory = null;
    private File targetFile      = null;
    private File javaccHome      = null;
    private CommandlineJava cmdl = new CommandlineJava();
    protected static final int TASKDEF_TYPE_JAVACC = 1;
    protected static final int TASKDEF_TYPE_JJTREE = 2;
    protected static final int TASKDEF_TYPE_JJDOC = 3;
    protected static final String[] ARCHIVE_LOCATIONS =
        new String[] {
        "JavaCC.zip",
        "bin/lib/JavaCC.zip",
        "bin/lib/javacc.jar",
        "javacc.jar", 
    };
    protected static final int[] ARCHIVE_LOCATIONS_VS_MAJOR_VERSION =
        new int[] {
        1,
        2,
        3,
        3,
    };
    protected static final String COM_PACKAGE = "COM.sun.labs.";
    protected static final String COM_JAVACC_CLASS = "javacc.Main";
    protected static final String COM_JJTREE_CLASS = "jjtree.Main";
    protected static final String COM_JJDOC_CLASS = "jjdoc.JJDocMain";
    protected static final String ORG_PACKAGE_3_0 = "org.netbeans.javacc.";
    protected static final String ORG_PACKAGE_3_1 = "org.javacc.";
    protected static final String ORG_JAVACC_CLASS = "parser.Main";
    protected static final String ORG_JJTREE_CLASS = COM_JJTREE_CLASS;
    protected static final String ORG_JJDOC_CLASS = COM_JJDOC_CLASS;
    private String maxMemory = null;
    public void setLookahead(int lookahead) {
        optionalAttrs.put(LOOKAHEAD, new Integer(lookahead));
    }
    public void setChoiceambiguitycheck(int choiceAmbiguityCheck) {
        optionalAttrs.put(CHOICE_AMBIGUITY_CHECK, new Integer(choiceAmbiguityCheck));
    }
    public void setOtherambiguityCheck(int otherAmbiguityCheck) {
        optionalAttrs.put(OTHER_AMBIGUITY_CHECK, new Integer(otherAmbiguityCheck));
    }
    public void setStatic(boolean staticParser) {
        optionalAttrs.put(STATIC, staticParser ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setDebugparser(boolean debugParser) {
        optionalAttrs.put(DEBUG_PARSER, debugParser ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setDebuglookahead(boolean debugLookahead) {
        optionalAttrs.put(DEBUG_LOOKAHEAD, debugLookahead ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setDebugtokenmanager(boolean debugTokenManager) {
        optionalAttrs.put(DEBUG_TOKEN_MANAGER, debugTokenManager ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setOptimizetokenmanager(boolean optimizeTokenManager) {
        optionalAttrs.put(OPTIMIZE_TOKEN_MANAGER,
                          optimizeTokenManager ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setErrorreporting(boolean errorReporting) {
        optionalAttrs.put(ERROR_REPORTING, errorReporting ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setJavaunicodeescape(boolean javaUnicodeEscape) {
        optionalAttrs.put(JAVA_UNICODE_ESCAPE, javaUnicodeEscape ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setUnicodeinput(boolean unicodeInput) {
        optionalAttrs.put(UNICODE_INPUT, unicodeInput ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setIgnorecase(boolean ignoreCase) {
        optionalAttrs.put(IGNORE_CASE, ignoreCase ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setCommontokenaction(boolean commonTokenAction) {
        optionalAttrs.put(COMMON_TOKEN_ACTION, commonTokenAction ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setUsertokenmanager(boolean userTokenManager) {
        optionalAttrs.put(USER_TOKEN_MANAGER, userTokenManager ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setUsercharstream(boolean userCharStream) {
        optionalAttrs.put(USER_CHAR_STREAM, userCharStream ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setBuildparser(boolean buildParser) {
        optionalAttrs.put(BUILD_PARSER, buildParser ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setBuildtokenmanager(boolean buildTokenManager) {
        optionalAttrs.put(BUILD_TOKEN_MANAGER, buildTokenManager ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setSanitycheck(boolean sanityCheck) {
        optionalAttrs.put(SANITY_CHECK, sanityCheck ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setForcelacheck(boolean forceLACheck) {
        optionalAttrs.put(FORCE_LA_CHECK, forceLACheck ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setCachetokens(boolean cacheTokens) {
        optionalAttrs.put(CACHE_TOKENS, cacheTokens ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setKeeplinecolumn(boolean keepLineColumn) {
        optionalAttrs.put(KEEP_LINE_COLUMN, keepLineColumn ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setJDKversion(String jdkVersion) {
        optionalAttrs.put(JDK_VERSION, jdkVersion);
    }
    public void setOutputdirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    public void setTarget(File targetFile) {
        this.targetFile = targetFile;
    }
    public void setJavacchome(File javaccHome) {
        this.javaccHome = javaccHome;
    }
    public void setMaxmemory(String max) {
        maxMemory = max;
    }
    public JavaCC() {
        cmdl.setVm(JavaEnvUtils.getJreExecutable("java"));
    }
    public void execute() throws BuildException {
        Enumeration iter = optionalAttrs.keys();
        while (iter.hasMoreElements()) {
            String name  = (String) iter.nextElement();
            Object value = optionalAttrs.get(name);
            cmdl.createArgument().setValue("-" + name + ":" + value.toString());
        }
        if (targetFile == null || !targetFile.isFile()) {
            throw new BuildException("Invalid target: " + targetFile);
        }
        if (outputDirectory == null) {
            outputDirectory = new File(targetFile.getParent());
        } else if (!outputDirectory.isDirectory()) {
            throw new BuildException("Outputdir not a directory.");
        }
        cmdl.createArgument().setValue("-OUTPUT_DIRECTORY:"
                                       + outputDirectory.getAbsolutePath());
        final File javaFile = getOutputJavaFile(outputDirectory, targetFile);
        if (javaFile.exists()
            && targetFile.lastModified() < javaFile.lastModified()) {
            log("Target is already built - skipping (" + targetFile + ")",
                Project.MSG_VERBOSE);
            return;
        }
        cmdl.createArgument().setValue(targetFile.getAbsolutePath());
        final Path classpath = cmdl.createClasspath(getProject());
        final File javaccJar = JavaCC.getArchiveFile(javaccHome);
        classpath.createPathElement().setPath(javaccJar.getAbsolutePath());
        classpath.addJavaRuntime();
        cmdl.setClassname(JavaCC.getMainClass(classpath,
                                              JavaCC.TASKDEF_TYPE_JAVACC));
        cmdl.setMaxmemory(maxMemory);
        final Commandline.Argument arg = cmdl.createVmArgument();
        arg.setValue("-Dinstall.root=" + javaccHome.getAbsolutePath());
        Execute.runCommand(this, cmdl.getCommandline());
    }
    protected static File getArchiveFile(File home) throws BuildException {
        return new File(home,
                        ARCHIVE_LOCATIONS[getArchiveLocationIndex(home)]);
    }
    protected static String getMainClass(File home, int type)
        throws BuildException {
        Path p = new Path(null);
        p.createPathElement().setLocation(getArchiveFile(home));
        p.addJavaRuntime();
        return getMainClass(p, type);
    }
    protected static String getMainClass(Path path, int type)
        throws BuildException {
        String packagePrefix = null;
        String mainClass = null;
        AntClassLoader l = null;
        try {
            l = AntClassLoader.newAntClassLoader(null, null,
                                                 path
                                                 .concatSystemClasspath("ignore"),
                                                 true);
            String javaccClass = COM_PACKAGE + COM_JAVACC_CLASS;
            InputStream is = l.getResourceAsStream(javaccClass.replace('.', '/')
                                                   + ".class");
            if (is != null) {
                packagePrefix = COM_PACKAGE;
                switch (type) {
                case TASKDEF_TYPE_JAVACC:
                    mainClass = COM_JAVACC_CLASS;
                    break;
                case TASKDEF_TYPE_JJTREE:
                    mainClass = COM_JJTREE_CLASS;
                    break;
                case TASKDEF_TYPE_JJDOC:
                    mainClass = COM_JJDOC_CLASS;
                    break;
                default:
                }
            } else {
                javaccClass = ORG_PACKAGE_3_1 + ORG_JAVACC_CLASS;
                is = l.getResourceAsStream(javaccClass.replace('.', '/')
                                           + ".class");
                if (is != null) {
                    packagePrefix = ORG_PACKAGE_3_1;
                } else {
                    javaccClass = ORG_PACKAGE_3_0 + ORG_JAVACC_CLASS;
                    is = l.getResourceAsStream(javaccClass.replace('.', '/')
                                               + ".class");
                    if (is != null) {
                        packagePrefix = ORG_PACKAGE_3_0;
                    }
                }
                if (is != null) {
                    switch (type) {
                    case TASKDEF_TYPE_JAVACC:
                        mainClass = ORG_JAVACC_CLASS;
                        break;
                    case TASKDEF_TYPE_JJTREE:
                        mainClass = ORG_JJTREE_CLASS;
                        break;
                    case TASKDEF_TYPE_JJDOC:
                        mainClass = ORG_JJDOC_CLASS;
                        break;
                    default:
                    }
                }
            }
            if (packagePrefix == null) {
                throw new BuildException("failed to load JavaCC");
            }
            if (mainClass == null) {
                throw new BuildException("unknown task type " + type);
            }
            return packagePrefix + mainClass;
        } finally {
            if (l != null) {
                l.cleanup();
            }
        }
    }
    private static int getArchiveLocationIndex(File home)
        throws BuildException {
        if (home == null || !home.isDirectory()) {
            throw new BuildException("JavaCC home must be a valid directory.");
        }
        for (int i = 0; i < ARCHIVE_LOCATIONS.length; i++) {
            File f = new File(home, ARCHIVE_LOCATIONS[i]);
            if (f.exists()) {
                return i;
            }
        }
        throw new BuildException("Could not find a path to JavaCC.zip "
                                 + "or javacc.jar from '" + home + "'.");
    }
    protected static int getMajorVersionNumber(File home)
        throws BuildException {
        return
            ARCHIVE_LOCATIONS_VS_MAJOR_VERSION[getArchiveLocationIndex(home)];
    }
    private File getOutputJavaFile(File outputdir, File srcfile) {
        String path = srcfile.getPath();
        int startBasename = path.lastIndexOf(File.separator);
        if (startBasename != -1) {
            path = path.substring(startBasename + 1);
        }
        int startExtn = path.lastIndexOf('.');
        if (startExtn != -1) {
            path = path.substring(0, startExtn) + ".java";
        } else {
            path += ".java";
        }
        if (outputdir != null) {
            path = outputdir + File.separator + path;
        }
        return new File(path);
    }
}
