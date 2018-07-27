package org.apache.tools.ant.taskdefs.rmic;
import java.io.File;
import java.util.Random;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Rmic;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.StringUtils;
public abstract class DefaultRmicAdapter implements RmicAdapter {
    private Rmic attributes;
    private FileNameMapper mapper;
    private static final Random RAND = new Random();
    public static final String RMI_STUB_SUFFIX = "_Stub";
    public static final String RMI_SKEL_SUFFIX = "_Skel";
    public static final String RMI_TIE_SUFFIX = "_Tie";
    public static final String STUB_COMPAT = "-vcompat";
    public static final String STUB_1_1 = "-v1.1";
    public static final String STUB_1_2 = "-v1.2";
    public static final String STUB_OPTION_1_1 = "1.1";
    public static final String STUB_OPTION_1_2 = "1.2";
    public static final String STUB_OPTION_COMPAT = "compat";
    public DefaultRmicAdapter() {
    }
    public void setRmic(final Rmic attributes) {
        this.attributes = attributes;
        mapper = new RmicFileNameMapper();
    }
    public Rmic getRmic() {
        return attributes;
    }
    protected String getStubClassSuffix() {
        return RMI_STUB_SUFFIX;
    }
    protected String getSkelClassSuffix() {
        return RMI_SKEL_SUFFIX;
    }
    protected String getTieClassSuffix() {
        return RMI_TIE_SUFFIX;
    }
    public FileNameMapper getMapper() {
        return mapper;
    }
    public Path getClasspath() {
        return getCompileClasspath();
    }
    protected Path getCompileClasspath() {
        Path classpath = new Path(attributes.getProject());
        classpath.setLocation(attributes.getBase());
        Path cp = attributes.getClasspath();
        if (cp == null) {
            cp = new Path(attributes.getProject());
        }
        if (attributes.getIncludeantruntime()) {
            classpath.addExisting(cp.concatSystemClasspath("last"));
        } else {
            classpath.addExisting(cp.concatSystemClasspath("ignore"));
        }
        if (attributes.getIncludejavaruntime()) {
            classpath.addJavaRuntime();
        }
        return classpath;
    }
    protected Commandline setupRmicCommand() {
        return setupRmicCommand(null);
    }
    protected Commandline setupRmicCommand(String[] options) {
        Commandline cmd = new Commandline();
        if (options != null) {
            for (int i = 0; i < options.length; i++) {
                cmd.createArgument().setValue(options[i]);
            }
        }
        Path classpath = getCompileClasspath();
        cmd.createArgument().setValue("-d");
        cmd.createArgument().setFile(attributes.getOutputDir());
        if (attributes.getExtdirs() != null) {
            cmd.createArgument().setValue("-extdirs");
            cmd.createArgument().setPath(attributes.getExtdirs());
        }
        cmd.createArgument().setValue("-classpath");
        cmd.createArgument().setPath(classpath);
        String stubOption = addStubVersionOptions();
        if (stubOption != null) {
            cmd.createArgument().setValue(stubOption);
        }
        if (null != attributes.getSourceBase()) {
            cmd.createArgument().setValue("-keepgenerated");
        }
        if (attributes.getIiop()) {
            attributes.log("IIOP has been turned on.", Project.MSG_INFO);
            cmd.createArgument().setValue("-iiop");
            if (attributes.getIiopopts() != null) {
                attributes.log("IIOP Options: " + attributes.getIiopopts(),
                               Project.MSG_INFO);
                cmd.createArgument().setValue(attributes.getIiopopts());
            }
        }
        if (attributes.getIdl())  {
            cmd.createArgument().setValue("-idl");
            attributes.log("IDL has been turned on.", Project.MSG_INFO);
            if (attributes.getIdlopts() != null) {
                cmd.createArgument().setValue(attributes.getIdlopts());
                attributes.log("IDL Options: " + attributes.getIdlopts(),
                               Project.MSG_INFO);
            }
        }
        if (attributes.getDebug()) {
            cmd.createArgument().setValue("-g");
        }
        String[] compilerArgs = attributes.getCurrentCompilerArgs();
        compilerArgs = preprocessCompilerArgs(compilerArgs);
        cmd.addArguments(compilerArgs);
        logAndAddFilesToCompile(cmd);
        return cmd;
     }
    protected String addStubVersionOptions() {
        String stubVersion = attributes.getStubVersion();
        String stubOption = null;
        if (null != stubVersion) {
            if (STUB_OPTION_1_1.equals(stubVersion)) {
                stubOption = STUB_1_1;
            } else if (STUB_OPTION_1_2.equals(stubVersion)) {
                stubOption = STUB_1_2;
            } else if (STUB_OPTION_COMPAT.equals(stubVersion)) {
                stubOption = STUB_COMPAT;
            } else {
                attributes.log("Unknown stub option " + stubVersion);
            }
        }
        if (stubOption == null
            && !attributes.getIiop()
            && !attributes.getIdl()) {
            stubOption = STUB_COMPAT;
        }
        return stubOption;
    }
    protected String[] preprocessCompilerArgs(String[] compilerArgs) {
        return compilerArgs;
    }
    protected String[] filterJvmCompilerArgs(String[] compilerArgs) {
        int len = compilerArgs.length;
        List args = new ArrayList(len);
        for (int i = 0; i < len; i++) {
            String arg = compilerArgs[i];
            if (!arg.startsWith("-J")) {
                args.add(arg);
            } else {
                attributes.log("Dropping " + arg + " from compiler arguments");
            }
        }
        int count = args.size();
        return (String[]) args.toArray(new String[count]);
    }
    protected void logAndAddFilesToCompile(Commandline cmd) {
        Vector compileList = attributes.getCompileList();
        attributes.log("Compilation " + cmd.describeArguments(),
                       Project.MSG_VERBOSE);
        StringBuffer niceSourceList = new StringBuffer("File");
        int cListSize = compileList.size();
        if (cListSize != 1) {
            niceSourceList.append("s");
        }
        niceSourceList.append(" to be compiled:");
        for (int i = 0; i < cListSize; i++) {
            String arg = (String) compileList.elementAt(i);
            cmd.createArgument().setValue(arg);
            niceSourceList.append("    ");
            niceSourceList.append(arg);
        }
        attributes.log(niceSourceList.toString(), Project.MSG_VERBOSE);
    }
    private class RmicFileNameMapper implements FileNameMapper {
        RmicFileNameMapper() {
        }
        public void setFrom(String s) {
        }
        public void setTo(String s) {
        }
        public String[] mapFileName(String name) {
            if (name == null
                || !name.endsWith(".class")
                || name.endsWith(getStubClassSuffix() + ".class")
                || name.endsWith(getSkelClassSuffix() + ".class")
                || name.endsWith(getTieClassSuffix() + ".class")) {
                return null;
            }
            String base = StringUtils.removeSuffix(name, ".class");
            String classname = base.replace(File.separatorChar, '.');
            if (attributes.getVerify()
                && !attributes.isValidRmiRemote(classname)) {
                return null;
            }
            String[] target = new String[] {name + ".tmp." + RAND.nextLong()};
            if (!attributes.getIiop() && !attributes.getIdl()) {
                if (STUB_OPTION_1_2.equals(attributes.getStubVersion())) {
                    target = new String[] {
                        base + getStubClassSuffix() + ".class"
                    };
                } else {
                    target = new String[] {
                        base + getStubClassSuffix() + ".class",
                        base + getSkelClassSuffix() + ".class",
                    };
                }
            } else if (!attributes.getIdl()) {
                int lastSlash = base.lastIndexOf(File.separatorChar);
                String dirname = "";
                int index = -1;
                if (lastSlash == -1) {
                    index = 0;
                } else {
                    index = lastSlash + 1;
                    dirname = base.substring(0, index);
                }
                String filename = base.substring(index);
                try {
                    Class c = attributes.getLoader().loadClass(classname);
                    if (c.isInterface()) {
                        target = new String[] {
                            dirname + "_" + filename + getStubClassSuffix()
                            + ".class"
                        };
                    } else {
                        Class interf = attributes.getRemoteInterface(c);
                        String iName = interf.getName();
                        String iDir = "";
                        int iIndex = -1;
                        int lastDot = iName.lastIndexOf(".");
                        if (lastDot == -1) {
                            iIndex = 0;
                        } else {
                            iIndex = lastDot + 1;
                            iDir = iName.substring(0, iIndex);
                            iDir = iDir.replace('.', File.separatorChar);
                        }
                        target = new String[] {
                            dirname + "_" + filename + getTieClassSuffix()
                            + ".class",
                            iDir + "_" + iName.substring(iIndex)
                            + getStubClassSuffix() + ".class"
                        };
                    }
                } catch (ClassNotFoundException e) {
                    attributes.log("Unable to verify class " + classname
                                   + ". It could not be found.",
                                   Project.MSG_WARN);
                } catch (NoClassDefFoundError e) {
                    attributes.log("Unable to verify class " + classname
                                   + ". It is not defined.", Project.MSG_WARN);
                } catch (Throwable t) {
                    attributes.log("Unable to verify class " + classname
                                   + ". Loading caused Exception: "
                                   + t.getMessage(), Project.MSG_WARN);
                }
            }
            return target;
        }
    }
}
