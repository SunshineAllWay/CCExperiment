package org.apache.tools.ant.taskdefs;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.util.StringUtils;
import org.apache.tools.ant.util.FileUtils;
public abstract class AbstractCvsTask extends Task {
    public static final int DEFAULT_COMPRESSION_LEVEL = 3;
    private static final int MAXIMUM_COMRESSION_LEVEL = 9;
    private Commandline cmd = new Commandline();
    private ArrayList modules = new ArrayList();
    private Vector vecCommandlines = new Vector();
    private String cvsRoot;
    private String cvsRsh;
    private String cvsPackage;
    private String tag;
    private static final String DEFAULT_COMMAND = "checkout";
    private String command = null;
    private boolean quiet = false;
    private boolean reallyquiet = false;
    private int compression = 0;
    private boolean noexec = false;
    private int port = 0;
    private File passFile = null;
    private File dest;
    private boolean append = false;
    private File output;
    private File error;
    private boolean failOnError = false;
    private ExecuteStreamHandler executeStreamHandler;
    private OutputStream outputStream;
    private OutputStream errorStream;
    public AbstractCvsTask() {
        super();
    }
    public void setExecuteStreamHandler(ExecuteStreamHandler handler) {
        this.executeStreamHandler = handler;
    }
    protected ExecuteStreamHandler getExecuteStreamHandler() {
        if (this.executeStreamHandler == null) {
            setExecuteStreamHandler(new PumpStreamHandler(getOutputStream(),
                                                          getErrorStream()));
        }
        return this.executeStreamHandler;
    }
    protected void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }
    protected OutputStream getOutputStream() {
        if (this.outputStream == null) {
            if (output != null) {
                try {
                    setOutputStream(new PrintStream(
                                        new BufferedOutputStream(
                                            new FileOutputStream(output
                                                                 .getPath(),
                                                                 append))));
                } catch (IOException e) {
                    throw new BuildException(e, getLocation());
                }
            } else {
                setOutputStream(new LogOutputStream(this, Project.MSG_INFO));
            }
        }
        return this.outputStream;
    }
    protected void setErrorStream(OutputStream errorStream) {
        this.errorStream = errorStream;
    }
    protected OutputStream getErrorStream() {
        if (this.errorStream == null) {
            if (error != null) {
                try {
                    setErrorStream(new PrintStream(
                                       new BufferedOutputStream(
                                           new FileOutputStream(error.getPath(),
                                                                append))));
                } catch (IOException e) {
                    throw new BuildException(e, getLocation());
                }
            } else {
                setErrorStream(new LogOutputStream(this, Project.MSG_WARN));
            }
        }
        return this.errorStream;
    }
    protected void runCommand(Commandline toExecute) throws BuildException {
        Environment env = new Environment();
        if (port > 0) {
            Environment.Variable var = new Environment.Variable();
            var.setKey("CVS_CLIENT_PORT");
            var.setValue(String.valueOf(port));
            env.addVariable(var);
            var = new Environment.Variable();
            var.setKey("CVS_PSERVER_PORT");
            var.setValue(String.valueOf(port));
            env.addVariable(var);
        }
        if (passFile == null) {
            File defaultPassFile = new File(
                System.getProperty("cygwin.user.home",
                    System.getProperty("user.home"))
                + File.separatorChar + ".cvspass");
            if (defaultPassFile.exists()) {
                this.setPassfile(defaultPassFile);
            }
        }
        if (passFile != null) {
            if (passFile.isFile() && passFile.canRead()) {
                Environment.Variable var = new Environment.Variable();
                var.setKey("CVS_PASSFILE");
                var.setValue(String.valueOf(passFile));
                env.addVariable(var);
                log("Using cvs passfile: " + String.valueOf(passFile),
                    Project.MSG_VERBOSE);
            } else if (!passFile.canRead()) {
                log("cvs passfile: " + String.valueOf(passFile)
                    + " ignored as it is not readable",
                    Project.MSG_WARN);
            } else {
                log("cvs passfile: " + String.valueOf(passFile)
                    + " ignored as it is not a file",
                    Project.MSG_WARN);
            }
        }
        if (cvsRsh != null) {
            Environment.Variable var = new Environment.Variable();
            var.setKey("CVS_RSH");
            var.setValue(String.valueOf(cvsRsh));
            env.addVariable(var);
        }
        Execute exe = new Execute(getExecuteStreamHandler(), null);
        exe.setAntRun(getProject());
        if (dest == null) {
            dest = getProject().getBaseDir();
        }
        if (!dest.exists()) {
            dest.mkdirs();
        }
        exe.setWorkingDirectory(dest);
        exe.setCommandline(toExecute.getCommandline());
        exe.setEnvironment(env.getVariables());
        try {
            String actualCommandLine = executeToString(exe);
            log(actualCommandLine, Project.MSG_VERBOSE);
            int retCode = exe.execute();
            log("retCode=" + retCode, Project.MSG_DEBUG);
            if (failOnError && Execute.isFailure(retCode)) {
                throw new BuildException("cvs exited with error code "
                                         + retCode
                                         + StringUtils.LINE_SEP
                                         + "Command line was ["
                                         + actualCommandLine + "]",
                                         getLocation());
            }
        } catch (IOException e) {
            if (failOnError) {
                throw new BuildException(e, getLocation());
            }
            log("Caught exception: " + e.getMessage(), Project.MSG_WARN);
        } catch (BuildException e) {
            if (failOnError) {
                throw(e);
            }
            Throwable t = e.getCause();
            if (t == null) {
                t = e;
            }
            log("Caught exception: " + t.getMessage(), Project.MSG_WARN);
        } catch (Exception e) {
            if (failOnError) {
                throw new BuildException(e, getLocation());
            }
            log("Caught exception: " + e.getMessage(), Project.MSG_WARN);
        }
    }
    public void execute() throws BuildException {
        String savedCommand = getCommand();
        if (this.getCommand() == null && vecCommandlines.size() == 0) {
            this.setCommand(AbstractCvsTask.DEFAULT_COMMAND);
        }
        String c = this.getCommand();
        Commandline cloned = null;
        if (c != null) {
            cloned = (Commandline) cmd.clone();
            cloned.createArgument(true).setLine(c);
            this.addConfiguredCommandline(cloned, true);
        }
        try {
            for (int i = 0; i < vecCommandlines.size(); i++) {
                this.runCommand((Commandline) vecCommandlines.elementAt(i));
            }
        } finally {
            if (cloned != null) {
                removeCommandline(cloned);
            }
            setCommand(savedCommand);
            FileUtils.close(outputStream);
            FileUtils.close(errorStream);
        }
    }
    private String executeToString(Execute execute) {
        String cmdLine = Commandline.describeCommand(execute
                .getCommandline());
        StringBuffer stringBuffer = removeCvsPassword(cmdLine);
        String newLine = StringUtils.LINE_SEP;
        String[] variableArray = execute.getEnvironment();
        if (variableArray != null) {
            stringBuffer.append(newLine);
            stringBuffer.append(newLine);
            stringBuffer.append("environment:");
            stringBuffer.append(newLine);
            for (int z = 0; z < variableArray.length; z++) {
                stringBuffer.append(newLine);
                stringBuffer.append("\t");
                stringBuffer.append(variableArray[z]);
            }
        }
        return stringBuffer.toString();
    }
    private StringBuffer removeCvsPassword(String cmdLine) {
        StringBuffer stringBuffer = new StringBuffer(cmdLine);
        int start = cmdLine.indexOf("-d:");
        if (start >= 0) {
            int stop = cmdLine.indexOf("@", start);
            int startproto = cmdLine.indexOf(":", start);
            int startuser = cmdLine.indexOf(":", startproto + 1);
            int startpass = cmdLine.indexOf(":", startuser + 1);
            stop = cmdLine.indexOf("@", start);
            if (stop >= 0 && startpass > startproto && startpass < stop) {
                for (int i = startpass + 1; i < stop; i++) {
                    stringBuffer.replace(i, i + 1, "*");
                }
            }
        }
        return stringBuffer;
    }
    public void setCvsRoot(String root) {
        if (root != null) {
            if (root.trim().equals("")) {
                root = null;
            }
        }
        this.cvsRoot = root;
    }
    public String getCvsRoot() {
        return this.cvsRoot;
    }
    public void setCvsRsh(String rsh) {
        if (rsh != null) {
            if (rsh.trim().equals("")) {
                rsh = null;
            }
        }
        this.cvsRsh = rsh;
    }
    public String getCvsRsh() {
        return this.cvsRsh;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public int getPort() {
        return this.port;
    }
    public void setPassfile(File passFile) {
        this.passFile = passFile;
    }
    public File getPassFile() {
        return this.passFile;
    }
    public void setDest(File dest) {
        this.dest = dest;
    }
    public File getDest() {
        return this.dest;
    }
    public void setPackage(String p) {
        this.cvsPackage = p;
    }
    public String getPackage() {
        return this.cvsPackage;
    }
    public String getTag() {
        return tag;
    }
    public void setTag(String p) {
        if (p != null && p.trim().length() > 0) {
            tag = p;
            addCommandArgument("-r" + p);
        }
    }
    public void addCommandArgument(String arg) {
        this.addCommandArgument(cmd, arg);
    }
    public void addCommandArgument(Commandline c, String arg) {
        c.createArgument().setValue(arg);
    }
    public void setDate(String p) {
        if (p != null && p.trim().length() > 0) {
            addCommandArgument("-D");
            addCommandArgument(p);
        }
    }
    public void setCommand(String c) {
        this.command = c;
    }
    public String getCommand() {
        return this.command;
    }
    public void setQuiet(boolean q) {
        quiet = q;
    }
    public void setReallyquiet(boolean q) {
        reallyquiet = q;
    }
    public void setNoexec(boolean ne) {
        noexec = ne;
    }
    public void setOutput(File output) {
        this.output = output;
    }
    public void setError(File error) {
        this.error = error;
    }
    public void setAppend(boolean value) {
        this.append = value;
    }
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }
    protected void configureCommandline(Commandline c) {
        if (c == null) {
            return;
        }
        c.setExecutable("cvs");
        if (cvsPackage != null) {
            c.createArgument().setLine(cvsPackage);
        }
        for (Iterator iter = modules.iterator(); iter.hasNext(); ) {
            Module m = (Module) iter.next();
            c.createArgument().setValue(m.getName());
        }
        if (this.compression > 0
            && this.compression <= MAXIMUM_COMRESSION_LEVEL) {
            c.createArgument(true).setValue("-z" + this.compression);
        }
        if (quiet && !reallyquiet) {
            c.createArgument(true).setValue("-q");
        }
        if (reallyquiet) {
            c.createArgument(true).setValue("-Q");
        }
        if (noexec) {
            c.createArgument(true).setValue("-n");
        }
        if (cvsRoot != null) {
            c.createArgument(true).setLine("-d" + cvsRoot);
        }
    }
    protected void removeCommandline(Commandline c) {
        vecCommandlines.removeElement(c);
    }
    public void addConfiguredCommandline(Commandline c) {
        this.addConfiguredCommandline(c, false);
    }
    public void addConfiguredCommandline(Commandline c,
                                         boolean insertAtStart) {
        if (c == null) {
            return;
        }
        this.configureCommandline(c);
        if (insertAtStart) {
            vecCommandlines.insertElementAt(c, 0);
        } else {
            vecCommandlines.addElement(c);
        }
    }
    public void setCompressionLevel(int level) {
        this.compression = level;
    }
    public void setCompression(boolean usecomp) {
        setCompressionLevel(usecomp
            ? AbstractCvsTask.DEFAULT_COMPRESSION_LEVEL : 0);
    }
    public void addModule(Module m) {
        modules.add(m);
    }
    protected List getModules() {
        return (List) modules.clone();
    }
    public static final class Module {
        private String name;
        public void setName(String s) {
            name = s;
        }
        public String getName() {
            return name;
        }
    }
}