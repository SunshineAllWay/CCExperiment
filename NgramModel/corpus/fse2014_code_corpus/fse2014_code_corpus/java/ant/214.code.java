package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ExitException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.ExitStatusException;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PropertySet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Assertions;
import org.apache.tools.ant.types.Permissions;
import org.apache.tools.ant.types.RedirectorElement;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.util.KeepAliveInputStream;
public class Java extends Task {
    private CommandlineJava cmdl = new CommandlineJava();
    private Environment env = new Environment();
    private boolean fork = false;
    private boolean newEnvironment = false;
    private File dir = null;
    private boolean failOnError = false;
    private Long timeout = null;
    private String inputString;
    private File input;
    private File output;
    private File error;
    protected Redirector redirector = new Redirector(this);
    protected RedirectorElement redirectorElement;
    private String resultProperty;
    private Permissions perm = null;
    private boolean spawn = false;
    private boolean incompatibleWithSpawn = false;
    private static final String TIMEOUT_MESSAGE = 
        "Timeout: killed the sub-process";
    public Java() {
    }
    public Java(Task owner) {
        bindToOwner(owner);
    }
    public void execute() throws BuildException {
        File savedDir = dir;
        Permissions savedPermissions = perm;
        int err = -1;
        try {
            checkConfiguration();
            err = executeJava();
            if (err != 0) {
                if (failOnError) {
                    throw new ExitStatusException("Java returned: " + err,
                            err,
                            getLocation());
                } else {
                    log("Java Result: " + err, Project.MSG_ERR);
                }
            }
            maybeSetResultPropertyValue(err);
        } finally {
            dir = savedDir;
            perm = savedPermissions;
        }
    }
    public int executeJava() throws BuildException {
        return executeJava(getCommandLine());
    }
    protected void checkConfiguration() throws BuildException {
        String classname = getCommandLine().getClassname();
        if (classname == null && getCommandLine().getJar() == null) {
            throw new BuildException("Classname must not be null.");
        }
        if (!fork && getCommandLine().getJar() != null) {
            throw new BuildException("Cannot execute a jar in non-forked mode."
                                     + " Please set fork='true'. ");
        }
        if (spawn && !fork) {
            throw new BuildException("Cannot spawn a java process in non-forked mode."
                                     + " Please set fork='true'. ");
        }
        if (getCommandLine().getClasspath() != null
            && getCommandLine().getJar() != null) {
            log("When using 'jar' attribute classpath-settings are ignored. "
                + "See the manual for more information.", Project.MSG_VERBOSE);
        }
        if (spawn && incompatibleWithSpawn) {
            getProject().log("spawn does not allow attributes related to input, "
            + "output, error, result", Project.MSG_ERR);
            getProject().log("spawn also does not allow timeout", Project.MSG_ERR);
            getProject().log("finally, spawn is not compatible "
                + "with a nested I/O <redirector>", Project.MSG_ERR);
            throw new BuildException("You have used an attribute "
                + "or nested element which is not compatible with spawn");
        }
        if (getCommandLine().getAssertions() != null && !fork) {
            log("Assertion statements are currently ignored in non-forked mode");
        }
        if (fork) {
            if (perm != null) {
                log("Permissions can not be set this way in forked mode.", Project.MSG_WARN);
            }
            log(getCommandLine().describeCommand(), Project.MSG_VERBOSE);
        } else {
            if (getCommandLine().getVmCommand().size() > 1) {
                log("JVM args ignored when same JVM is used.",
                    Project.MSG_WARN);
            }
            if (dir != null) {
                log("Working directory ignored when same JVM is used.",
                    Project.MSG_WARN);
            }
            if (newEnvironment || null != env.getVariables()) {
                log("Changes to environment variables are ignored when same "
                    + "JVM is used.", Project.MSG_WARN);
            }
            if (getCommandLine().getBootclasspath() != null) {
                log("bootclasspath ignored when same JVM is used.",
                    Project.MSG_WARN);
            }
            if (perm == null) {
                perm = new Permissions(true);
                log("running " + this.getCommandLine().getClassname()
                    + " with default permissions (exit forbidden)", Project.MSG_VERBOSE);
            }
            log("Running in same VM " + getCommandLine().describeJavaCommand(),
                Project.MSG_VERBOSE);
        }
        setupRedirector();
    }
    protected int executeJava(CommandlineJava commandLine) {
        try {
            if (fork) {
                if (!spawn) {
                    return fork(commandLine.getCommandline());
                } else {
                    spawn(commandLine.getCommandline());
                    return 0;
                }
            } else {
                try {
                    run(commandLine);
                    return 0;
                } catch (ExitException ex) {
                    return ex.getStatus();
                }
            }
        } catch (BuildException e) {
            if (e.getLocation() == null && getLocation() != null) {
                e.setLocation(getLocation());
            }
            if (failOnError) {
                throw e;
            } else {
                if (TIMEOUT_MESSAGE.equals(e.getMessage())) {
                    log(TIMEOUT_MESSAGE);
                } else {
                    log(e);
                }
                return -1;
            }
        } catch (ThreadDeath t) {
            throw t; 
        } catch (Throwable t) {
            if (failOnError) {
                throw new BuildException(t, getLocation());
            } else {
                log(t);
                return -1;
            }
        }
    }
    public void setSpawn(boolean spawn) {
        this.spawn = spawn;
    }
    public void setClasspath(Path s) {
        createClasspath().append(s);
    }
    public Path createClasspath() {
        return getCommandLine().createClasspath(getProject()).createPath();
    }
    public Path createBootclasspath() {
        return getCommandLine().createBootclasspath(getProject()).createPath();
    }
    public Permissions createPermissions() {
        perm = (perm == null) ? new Permissions() : perm;
        return perm;
    }
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }
    public void setJar(File jarfile) throws BuildException {
        if (getCommandLine().getClassname() != null) {
            throw new BuildException("Cannot use 'jar' and 'classname' "
                                     + "attributes in same command.");
        }
        getCommandLine().setJar(jarfile.getAbsolutePath());
    }
    public void setClassname(String s) throws BuildException {
        if (getCommandLine().getJar() != null) {
            throw new BuildException("Cannot use 'jar' and 'classname' "
                                     + "attributes in same command");
        }
        getCommandLine().setClassname(s);
    }
    public void setArgs(String s) {
        log("The args attribute is deprecated. "
            + "Please use nested arg elements.", Project.MSG_WARN);
        getCommandLine().createArgument().setLine(s);
    }
    public void setCloneVm(boolean cloneVm) {
        getCommandLine().setCloneVm(cloneVm);
    }
    public Commandline.Argument createArg() {
        return getCommandLine().createArgument();
    }
    public void setResultProperty(String resultProperty) {
        this.resultProperty = resultProperty;
        incompatibleWithSpawn = true;
    }
    protected void maybeSetResultPropertyValue(int result) {
        String res = Integer.toString(result);
        if (resultProperty != null) {
            getProject().setNewProperty(resultProperty, res);
        }
    }
    public void setFork(boolean s) {
        this.fork = s;
    }
    public void setJvmargs(String s) {
        log("The jvmargs attribute is deprecated. "
            + "Please use nested jvmarg elements.", Project.MSG_WARN);
        getCommandLine().createVmArgument().setLine(s);
    }
    public Commandline.Argument createJvmarg() {
        return getCommandLine().createVmArgument();
    }
    public void setJvm(String s) {
        getCommandLine().setVm(s);
    }
    public void addSysproperty(Environment.Variable sysp) {
        getCommandLine().addSysproperty(sysp);
    }
    public void addSyspropertyset(PropertySet sysp) {
        getCommandLine().addSyspropertyset(sysp);
    }
    public void setFailonerror(boolean fail) {
        failOnError = fail;
        incompatibleWithSpawn |= fail;
    }
    public void setDir(File d) {
        this.dir = d;
    }
    public void setOutput(File out) {
        this.output = out;
        incompatibleWithSpawn = true;
    }
    public void setInput(File input) {
        if (inputString != null) {
            throw new BuildException("The \"input\" and \"inputstring\" "
                + "attributes cannot both be specified");
        }
        this.input = input;
        incompatibleWithSpawn = true;
    }
    public void setInputString(String inputString) {
        if (input != null) {
            throw new BuildException("The \"input\" and \"inputstring\" "
                + "attributes cannot both be specified");
        }
        this.inputString = inputString;
        incompatibleWithSpawn = true;
    }
    public void setLogError(boolean logError) {
        redirector.setLogError(logError);
        incompatibleWithSpawn |= logError;
    }
    public void setError(File error) {
        this.error = error;
        incompatibleWithSpawn = true;
    }
    public void setOutputproperty(String outputProp) {
        redirector.setOutputProperty(outputProp);
        incompatibleWithSpawn = true;
    }
    public void setErrorProperty(String errorProperty) {
        redirector.setErrorProperty(errorProperty);
        incompatibleWithSpawn = true;
    }
    public void setMaxmemory(String max) {
        getCommandLine().setMaxmemory(max);
    }
    public void setJVMVersion(String value) {
        getCommandLine().setVmversion(value);
    }
    public void addEnv(Environment.Variable var) {
        env.addVariable(var);
    }
    public void setNewenvironment(boolean newenv) {
        newEnvironment = newenv;
    }
    public void setAppend(boolean append) {
        redirector.setAppend(append);
        incompatibleWithSpawn = true;
    }
    public void setTimeout(Long value) {
        timeout = value;
        incompatibleWithSpawn |= timeout != null;
    }
    public void addAssertions(Assertions asserts) {
        if (getCommandLine().getAssertions() != null) {
            throw new BuildException("Only one assertion declaration is allowed");
        }
        getCommandLine().setAssertions(asserts);
    }
    public void addConfiguredRedirector(RedirectorElement redirectorElement) {
        if (this.redirectorElement != null) {
            throw new BuildException("cannot have > 1 nested redirectors");
        }
        this.redirectorElement = redirectorElement;
        incompatibleWithSpawn = true;
    }
    protected void handleOutput(String output) {
        if (redirector.getOutputStream() != null) {
            redirector.handleOutput(output);
        } else {
            super.handleOutput(output);
        }
    }
    public int handleInput(byte[] buffer, int offset, int length)
        throws IOException {
        return redirector.handleInput(buffer, offset, length);
    }
    protected void handleFlush(String output) {
        if (redirector.getOutputStream() != null) {
            redirector.handleFlush(output);
        } else {
            super.handleFlush(output);
        }
    }
    protected void handleErrorOutput(String output) {
        if (redirector.getErrorStream() != null) {
            redirector.handleErrorOutput(output);
        } else {
            super.handleErrorOutput(output);
        }
    }
    protected void handleErrorFlush(String output) {
        if (redirector.getErrorStream() != null) {
            redirector.handleErrorFlush(output);
        } else {
            super.handleErrorFlush(output);
        }
    }
    protected void setupRedirector() {
        redirector.setInput(input);
        redirector.setInputString(inputString);
        redirector.setOutput(output);
        redirector.setError(error);
        if (redirectorElement != null) {
            redirectorElement.configure(redirector);
        }
        if (!spawn && input == null && inputString == null) {
            redirector.setInputStream(
                new KeepAliveInputStream(getProject().getDefaultInputStream()));
        }
    }
    private void run(CommandlineJava command) throws BuildException {
        try {
            ExecuteJava exe = new ExecuteJava();
            exe.setJavaCommand(command.getJavaCommand());
            exe.setClasspath(command.getClasspath());
            exe.setSystemProperties(command.getSystemProperties());
            exe.setPermissions(perm);
            exe.setTimeout(timeout);
            redirector.createStreams();
            exe.execute(getProject());
            redirector.complete();
            if (exe.killedProcess()) {
                throw new BuildException(TIMEOUT_MESSAGE);
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    private int fork(String[] command) throws BuildException {
        Execute exe
            = new Execute(redirector.createHandler(), createWatchdog());
        setupExecutable(exe, command);
        try {
            int rc = exe.execute();
            redirector.complete();
            if (exe.killedProcess()) {
                throw new BuildException(TIMEOUT_MESSAGE);
            }
            return rc;
        } catch (IOException e) {
            throw new BuildException(e, getLocation());
        }
    }
    private void spawn(String[] command) throws BuildException {
        Execute exe = new Execute();
        setupExecutable(exe, command);
        try {
            exe.spawn();
        } catch (IOException e) {
            throw new BuildException(e, getLocation());
        }
    }
    private void setupExecutable(Execute exe, String[] command) {
        exe.setAntRun(getProject());
        setupWorkingDir(exe);
        setupEnvironment(exe);
        setupCommandLine(exe, command);
    }
    private void setupEnvironment(Execute exe) {
        String[] environment = env.getVariables();
        if (environment != null) {
            for (int i = 0; i < environment.length; i++) {
                log("Setting environment variable: " + environment[i],
                    Project.MSG_VERBOSE);
            }
        }
        exe.setNewenvironment(newEnvironment);
        exe.setEnvironment(environment);
    }
    private void setupWorkingDir(Execute exe) {
        if (dir == null) {
            dir = getProject().getBaseDir();
        } else if (!dir.exists() || !dir.isDirectory()) {
            throw new BuildException(dir.getAbsolutePath()
                                     + " is not a valid directory",
                                     getLocation());
        }
        exe.setWorkingDirectory(dir);
    }
    private void setupCommandLine(Execute exe, String[] command) {
        if (Os.isFamily("openvms")) {
            setupCommandLineForVMS(exe, command);
        } else {
            exe.setCommandline(command);
        }
    }
    private void setupCommandLineForVMS(Execute exe, String[] command) {
        ExecuteJava.setupCommandLineForVMS(exe, command);
    }
    protected void run(String classname, Vector args) throws BuildException {
        CommandlineJava cmdj = new CommandlineJava();
        cmdj.setClassname(classname);
        for (int i = 0; i < args.size(); i++) {
            cmdj.createArgument().setValue((String) args.elementAt(i));
        }
        run(cmdj);
    }
    public void clearArgs() {
        getCommandLine().clearJavaArgs();
    }
    protected ExecuteWatchdog createWatchdog() throws BuildException {
        if (timeout == null) {
            return null;
        }
        return new ExecuteWatchdog(timeout.longValue());
    }
    private void log(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        t.printStackTrace(w);
        w.close();
        log(sw.toString(), Project.MSG_ERR);
    }
    public CommandlineJava getCommandLine() {
        return cmdl;
    }
    public CommandlineJava.SysProperties getSysProperties() {
        return getCommandLine().getSystemProperties();
    }
}
