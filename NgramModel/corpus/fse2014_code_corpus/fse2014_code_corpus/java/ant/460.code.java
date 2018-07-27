package org.apache.tools.ant.taskdefs.optional.jdepend;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.LoaderUtils;
public class JDependTask extends Task {
    private Path sourcesPath; 
    private Path classesPath; 
    private File outputFile;
    private File dir;
    private Path compileClasspath;
    private boolean haltonerror = false;
    private boolean fork = false;
    private Long timeout = null;
    private String jvm = null;
    private String format = "text";
    private PatternSet defaultPatterns = new PatternSet();
    private static Constructor packageFilterC;
    private static Method setFilter;
    private boolean includeRuntime = false;
    private Path runtimeClasses = null;
    static {
        try {
            Class packageFilter =
                Class.forName("jdepend.framework.PackageFilter");
            packageFilterC =
                packageFilter.getConstructor(new Class[] {java.util.Collection.class});
            setFilter =
                jdepend.textui.JDepend.class.getDeclaredMethod("setFilter",
                                                               new Class[] {packageFilter});
        } catch (Throwable t) {
            if (setFilter == null) {
                packageFilterC = null;
            }
        }
    }
    public void setIncluderuntime(boolean b) {
        includeRuntime = b;
    }
    public void setTimeout(Long value) {
        timeout = value;
    }
    public Long getTimeout() {
        return timeout;
    }
    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
    public File getOutputFile() {
        return outputFile;
    }
    public void setHaltonerror(boolean haltonerror) {
        this.haltonerror = haltonerror;
    }
    public boolean getHaltonerror() {
        return haltonerror;
    }
    public void setFork(boolean value) {
        fork = value;
    }
    public boolean getFork() {
        return fork;
    }
    public void setJvm(String value) {
        jvm = value;
    }
    public Path createSourcespath() {
        if (sourcesPath == null) {
            sourcesPath = new Path(getProject());
        }
        return sourcesPath.createPath();
    }
    public Path getSourcespath() {
        return sourcesPath;
    }
    public Path createClassespath() {
        if (classesPath == null) {
            classesPath = new Path(getProject());
        }
        return classesPath.createPath();
    }
    public Path getClassespath() {
        return classesPath;
    }
    public void setDir(File dir) {
        this.dir = dir;
    }
    public File getDir() {
        return dir;
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
    public Commandline.Argument createJvmarg(CommandlineJava commandline) {
        return commandline.createVmArgument();
    }
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }
    public PatternSet.NameEntry createExclude() {
        return defaultPatterns.createExclude();
    }
    public PatternSet getExcludes() {
        return defaultPatterns;
    }
    public void setFormat(FormatAttribute ea) {
        format = ea.getValue();
    }
    public static class FormatAttribute extends EnumeratedAttribute {
        private String [] formats = new String[]{"xml", "text"};
        public String[] getValues() {
            return formats;
        }
    }
    private static final int SUCCESS = 0;
    private static final int ERRORS = 1;
    private void addClasspathEntry(String resource) {
        if (resource.startsWith("/")) {
            resource = resource.substring(1);
        } else {
            resource = "org/apache/tools/ant/taskdefs/optional/jdepend/"
                + resource;
        }
        File f = LoaderUtils.getResourceSource(getClass().getClassLoader(),
                                               resource);
        if (f != null) {
            log("Found " + f.getAbsolutePath(), Project.MSG_DEBUG);
            runtimeClasses.createPath().setLocation(f);
        } else {
            log("Couldn\'t find " + resource, Project.MSG_DEBUG);
        }
    }
    public void execute() throws BuildException {
        CommandlineJava commandline = new CommandlineJava();
        if ("text".equals(format)) {
            commandline.setClassname("jdepend.textui.JDepend");
        } else
            if ("xml".equals(format)) {
                commandline.setClassname("jdepend.xmlui.JDepend");
            }
        if (jvm != null) {
            commandline.setVm(jvm);
        }
        if (getSourcespath() == null && getClassespath() == null) {
            throw new BuildException("Missing classespath required argument");
        } else if (getClassespath() == null) {
            String msg =
                "sourcespath is deprecated in JDepend >= 2.5 "
                + "- please convert to classespath";
            log(msg);
        }
        int exitValue = JDependTask.ERRORS;
        boolean wasKilled = false;
        if (!getFork()) {
            exitValue = executeInVM(commandline);
        } else {
            ExecuteWatchdog watchdog = createWatchdog();
            exitValue = executeAsForked(commandline, watchdog);
            if (watchdog != null) {
                wasKilled = watchdog.killedProcess();
            }
        }
        boolean errorOccurred = exitValue == JDependTask.ERRORS || wasKilled;
        if (errorOccurred) {
            String errorMessage = "JDepend FAILED"
                + (wasKilled ? " - Timed out" : "");
            if  (getHaltonerror()) {
                throw new BuildException(errorMessage, getLocation());
            } else {
                log(errorMessage, Project.MSG_ERR);
            }
        }
    }
    public int executeInVM(CommandlineJava commandline) throws BuildException {
        jdepend.textui.JDepend jdepend;
        if ("xml".equals(format)) {
            jdepend = new jdepend.xmlui.JDepend();
        } else {
            jdepend = new jdepend.textui.JDepend();
        }
        FileWriter fw = null;
        PrintWriter pw = null;
        if (getOutputFile() != null) {
            try {
                fw = new FileWriter(getOutputFile().getPath());
            } catch (IOException e) {
                String msg = "JDepend Failed when creating the output file: "
                    + e.getMessage();
                log(msg);
                throw new BuildException(msg);
            }
            pw = new PrintWriter(fw);
            jdepend.setWriter(pw);
            log("Output to be stored in " + getOutputFile().getPath());
        }
        try {
            if (getClassespath() != null) {
                String[] cP = getClassespath().list();
                for (int i = 0; i < cP.length; i++) {
                    File f = new File(cP[i]);
                    if (!f.exists()) {
                        String msg = "\""
                            + f.getPath()
                            + "\" does not represent a valid"
                            + " file or directory. JDepend would fail.";
                        log(msg);
                        throw new BuildException(msg);
                    }
                    try {
                        jdepend.addDirectory(f.getPath());
                    } catch (IOException e) {
                        String msg =
                            "JDepend Failed when adding a class directory: "
                            + e.getMessage();
                        log(msg);
                        throw new BuildException(msg);
                    }
                }
            } else if (getSourcespath() != null) {
                String[] sP = getSourcespath().list();
                for (int i = 0; i < sP.length; i++) {
                    File f = new File(sP[i]);
                    if (!f.exists() || !f.isDirectory()) {
                        String msg = "\""
                            + f.getPath()
                            + "\" does not represent a valid"
                            + " directory. JDepend would fail.";
                        log(msg);
                        throw new BuildException(msg);
                    }
                    try {
                        jdepend.addDirectory(f.getPath());
                    } catch (IOException e) {
                        String msg =
                            "JDepend Failed when adding a source directory: "
                            + e.getMessage();
                        log(msg);
                        throw new BuildException(msg);
                    }
                }
            }
            String[] patterns = defaultPatterns.getExcludePatterns(getProject());
            if (patterns != null && patterns.length > 0) {
                if (setFilter != null) {
                    Vector v = new Vector();
                    for (int i = 0; i < patterns.length; i++) {
                        v.addElement(patterns[i]);
                    }
                    try {
                        Object o = packageFilterC.newInstance(new Object[] {v});
                        setFilter.invoke(jdepend, new Object[] {o});
                    } catch (Throwable e) {
                        log("excludes will be ignored as JDepend doesn't like me: "
                            + e.getMessage(), Project.MSG_WARN);
                    }
                } else {
                    log("Sorry, your version of JDepend doesn't support excludes",
                        Project.MSG_WARN);
                }
            }
            jdepend.analyze();
            if (pw.checkError()) {
                throw new IOException("Encountered an error writing JDepend"
                                      + " output");
            }
        } catch (IOException ex) {
            throw new BuildException(ex);
        } finally {
            FileUtils.close(pw);
            FileUtils.close(fw);
        }
        return SUCCESS;
    }
    public int executeAsForked(CommandlineJava commandline,
                               ExecuteWatchdog watchdog) throws BuildException {
        runtimeClasses = new Path(getProject());
        addClasspathEntry("/jdepend/textui/JDepend.class");
        createClasspath();
        if (getClasspath().toString().length() > 0) {
            createJvmarg(commandline).setValue("-classpath");
            createJvmarg(commandline).setValue(getClasspath().toString());
        }
        if (includeRuntime) {
            Map env = Execute.getEnvironmentVariables();
            String cp = (String) env.get("CLASSPATH");
            if (cp != null) {
                commandline.createClasspath(getProject()).createPath()
                    .append(new Path(getProject(), cp));
            }
            log("Implicitly adding " + runtimeClasses + " to CLASSPATH",
                Project.MSG_VERBOSE);
            commandline.createClasspath(getProject()).createPath()
                .append(runtimeClasses);
        }
        if (getOutputFile() != null) {
            commandline.createArgument().setValue("-file");
            commandline.createArgument().setValue(outputFile.getPath());
        }
        if (getSourcespath() != null) {
            String[] sP = getSourcespath().list();
            for (int i = 0; i < sP.length; i++) {
                File f = new File(sP[i]);
                if (!f.exists() || !f.isDirectory()) {
                    throw new BuildException("\"" + f.getPath()
                                             + "\" does not represent a valid"
                                             + " directory. JDepend would"
                                             + " fail.");
                }
                commandline.createArgument().setValue(f.getPath());
            }
        }
        if (getClassespath() != null) {
            String[] cP = getClassespath().list();
            for (int i = 0; i < cP.length; i++) {
                File f = new File(cP[i]);
                if (!f.exists()) {
                    throw new BuildException("\"" + f.getPath()
                                             + "\" does not represent a valid"
                                             + " file or directory. JDepend would"
                                             + " fail.");
                }
                commandline.createArgument().setValue(f.getPath());
            }
        }
        Execute execute = new Execute(new LogStreamHandler(this,
            Project.MSG_INFO, Project.MSG_WARN), watchdog);
        execute.setCommandline(commandline.getCommandline());
        if (getDir() != null) {
            execute.setWorkingDirectory(getDir());
            execute.setAntRun(getProject());
        }
        if (getOutputFile() != null) {
            log("Output to be stored in " + getOutputFile().getPath());
        }
        log(commandline.describeCommand(), Project.MSG_VERBOSE);
        try {
            return execute.execute();
        } catch (IOException e) {
            throw new BuildException("Process fork failed.", e, getLocation());
        }
    }
    protected ExecuteWatchdog createWatchdog() throws BuildException {
        if (getTimeout() == null) {
            return null;
        }
        return new ExecuteWatchdog(getTimeout().longValue());
    }
}
