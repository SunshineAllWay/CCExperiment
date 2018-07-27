package org.apache.tools.ant.taskdefs.optional.javacc;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.JavaEnvUtils;
public class JJTree extends Task {
    private static final String OUTPUT_FILE       = "OUTPUT_FILE";
    private static final String BUILD_NODE_FILES  = "BUILD_NODE_FILES";
    private static final String MULTI             = "MULTI";
    private static final String NODE_DEFAULT_VOID = "NODE_DEFAULT_VOID";
    private static final String NODE_FACTORY      = "NODE_FACTORY";
    private static final String NODE_SCOPE_HOOK   = "NODE_SCOPE_HOOK";
    private static final String NODE_USES_PARSER  = "NODE_USES_PARSER";
    private static final String STATIC            = "STATIC";
    private static final String VISITOR           = "VISITOR";
    private static final String NODE_PACKAGE      = "NODE_PACKAGE";
    private static final String VISITOR_EXCEPTION = "VISITOR_EXCEPTION";
    private static final String NODE_PREFIX       = "NODE_PREFIX";
    private final Hashtable optionalAttrs = new Hashtable();
    private String outputFile = null;
    private static final String DEFAULT_SUFFIX = ".jj";
    private File outputDirectory = null;
    private File targetFile      = null;
    private File javaccHome      = null;
    private CommandlineJava cmdl = new CommandlineJava();
    private String maxMemory = null;
    public void setBuildnodefiles(boolean buildNodeFiles) {
        optionalAttrs.put(BUILD_NODE_FILES, buildNodeFiles ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setMulti(boolean multi) {
        optionalAttrs.put(MULTI, multi ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setNodedefaultvoid(boolean nodeDefaultVoid) {
        optionalAttrs.put(NODE_DEFAULT_VOID, nodeDefaultVoid ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setNodefactory(boolean nodeFactory) {
        optionalAttrs.put(NODE_FACTORY, nodeFactory ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setNodescopehook(boolean nodeScopeHook) {
        optionalAttrs.put(NODE_SCOPE_HOOK, nodeScopeHook ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setNodeusesparser(boolean nodeUsesParser) {
        optionalAttrs.put(NODE_USES_PARSER, nodeUsesParser ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setStatic(boolean staticParser) {
        optionalAttrs.put(STATIC, staticParser ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setVisitor(boolean visitor) {
        optionalAttrs.put(VISITOR, visitor ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setNodepackage(String nodePackage) {
        optionalAttrs.put(NODE_PACKAGE, nodePackage);
    }
    public void setVisitorException(String visitorException) {
        optionalAttrs.put(VISITOR_EXCEPTION, visitorException);
    }
    public void setNodeprefix(String nodePrefix) {
        optionalAttrs.put(NODE_PREFIX, nodePrefix);
    }
    public void setOutputdirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    public void setOutputfile(String outputFile) {
        this.outputFile = outputFile;
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
    public JJTree() {
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
        File javaFile = null;
        if (outputDirectory == null) {
            cmdl.createArgument().setValue("-OUTPUT_DIRECTORY:"
                                           + getDefaultOutputDirectory());
            javaFile = new File(createOutputFileName(targetFile, outputFile,
                                                     null));
        } else {
            if (!outputDirectory.isDirectory()) {
                throw new BuildException("'outputdirectory' " + outputDirectory
                                         + " is not a directory.");
            }
            cmdl.createArgument().setValue("-OUTPUT_DIRECTORY:"
                                           + outputDirectory.getAbsolutePath()
                                             .replace('\\', '/'));
            javaFile = new File(createOutputFileName(targetFile, outputFile,
                                                     outputDirectory
                                                     .getPath()));
        }
        if (javaFile.exists()
            && targetFile.lastModified() < javaFile.lastModified()) {
            log("Target is already built - skipping (" + targetFile + ")",
                Project.MSG_VERBOSE);
            return;
        }
        if (outputFile != null) {
            cmdl.createArgument().setValue("-" + OUTPUT_FILE + ":"
                                           + outputFile.replace('\\', '/'));
        }
        cmdl.createArgument().setValue(targetFile.getAbsolutePath());
        final Path classpath = cmdl.createClasspath(getProject());
        final File javaccJar = JavaCC.getArchiveFile(javaccHome);
        classpath.createPathElement().setPath(javaccJar.getAbsolutePath());
        classpath.addJavaRuntime();
        cmdl.setClassname(JavaCC.getMainClass(classpath,
                                              JavaCC.TASKDEF_TYPE_JJTREE));
        cmdl.setMaxmemory(maxMemory);
        final Commandline.Argument arg = cmdl.createVmArgument();
        arg.setValue("-Dinstall.root=" + javaccHome.getAbsolutePath());
        final Execute process =
            new Execute(new LogStreamHandler(this,
                                             Project.MSG_INFO,
                                             Project.MSG_INFO),
                        null);
        log(cmdl.describeCommand(), Project.MSG_VERBOSE);
        process.setCommandline(cmdl.getCommandline());
        try {
            if (process.execute() != 0) {
                throw new BuildException("JJTree failed.");
            }
        } catch (IOException e) {
            throw new BuildException("Failed to launch JJTree", e);
        }
    }
    private String createOutputFileName(File destFile, String optionalOutputFile,
                                        String outputDir) {
        optionalOutputFile = validateOutputFile(optionalOutputFile,
                                                outputDir);
        String jjtreeFile = destFile.getAbsolutePath().replace('\\', '/');
        if ((optionalOutputFile == null) || optionalOutputFile.equals("")) {
            int filePos = jjtreeFile.lastIndexOf("/");
            if (filePos >= 0) {
                jjtreeFile = jjtreeFile.substring(filePos + 1);
            }
            int suffixPos = jjtreeFile.lastIndexOf('.');
            if (suffixPos == -1) {
                optionalOutputFile = jjtreeFile + DEFAULT_SUFFIX;
            } else {
                String currentSuffix = jjtreeFile.substring(suffixPos);
                if (currentSuffix.equals(DEFAULT_SUFFIX)) {
                    optionalOutputFile = jjtreeFile + DEFAULT_SUFFIX;
                } else {
                    optionalOutputFile = jjtreeFile.substring(0, suffixPos)
                        + DEFAULT_SUFFIX;
                }
            }
        }
        if ((outputDir == null) || outputDir.equals("")) {
            outputDir = getDefaultOutputDirectory();
        }
        return (outputDir + "/" + optionalOutputFile).replace('\\', '/');
    }
    private String validateOutputFile(String destFile,
                                      String outputDir)
        throws BuildException {
        if (destFile == null) {
            return null;
        }
        if ((outputDir == null)
            && (destFile.startsWith("/") || destFile.startsWith("\\"))) {
            String relativeOutputFile = makeOutputFileRelative(destFile);
            setOutputfile(relativeOutputFile);
            return relativeOutputFile;
        }
        String root = getRoot(new File(destFile)).getAbsolutePath();
        if ((root.length() > 1)
            && destFile.startsWith(root.substring(0, root.length() - 1))) {
            throw new BuildException("Drive letter in 'outputfile' not "
                                     + "supported: " + destFile);
        }
        return destFile;
    }
    private String makeOutputFileRelative(String destFile) {
        StringBuffer relativePath = new StringBuffer();
        String defaultOutputDirectory = getDefaultOutputDirectory();
        int nextPos = defaultOutputDirectory.indexOf('/');
        int startPos = nextPos + 1;
        while (startPos > -1 && startPos < defaultOutputDirectory.length()) {
            relativePath.append("/..");
            nextPos = defaultOutputDirectory.indexOf('/', startPos);
            if (nextPos == -1) {
                startPos = nextPos;
            } else {
                startPos = nextPos + 1;
            }
        }
        relativePath.append(destFile);
        return relativePath.toString();
    }
    private String getDefaultOutputDirectory() {
        return getProject().getBaseDir().getAbsolutePath().replace('\\', '/');
    }
    private File getRoot(File file) {
        File root = file.getAbsoluteFile();
        while (root.getParent() != null) {
            root = root.getParentFile();
        }
        return root;
    }
}
