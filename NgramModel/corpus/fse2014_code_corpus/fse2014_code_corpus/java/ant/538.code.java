package org.apache.tools.ant.taskdefs.optional.sos;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;
public abstract class SOS extends Task implements SOSCmd {
    private static final int ERROR_EXIT_STATUS = 255;
    private String sosCmdDir = null;
    private String sosUsername = null;
    private String sosPassword = "";
    private String projectPath = null;
    private String vssServerPath = null;
    private String sosServerPath = null;
    private String sosHome = null;
    private String localPath = null;
    private String version = null;
    private String label = null;
    private String comment = null;
    private String filename = null;
    private boolean noCompress = false;
    private boolean noCache = false;
    private boolean recursive = false;
    private boolean verbose = false;
    protected Commandline commandLine;
    public final void setNoCache(boolean nocache) {
        noCache = nocache;
    }
    public final void setNoCompress(boolean nocompress) {
        noCompress = nocompress;
    }
    public final void setSosCmd(String dir) {
        sosCmdDir = FileUtils.translatePath(dir);
    }
    public final void setUsername(String username) {
        sosUsername = username;
    }
    public final void setPassword(String password) {
        sosPassword = password;
    }
    public final void setProjectPath(String projectpath) {
        if (projectpath.startsWith(SOSCmd.PROJECT_PREFIX)) {
            projectPath = projectpath;
        } else {
            projectPath = SOSCmd.PROJECT_PREFIX + projectpath;
        }
    }
    public final void setVssServerPath(String vssServerPath) {
        this.vssServerPath = vssServerPath;
    }
    public final void setSosHome(String sosHome) {
        this.sosHome = sosHome;
    }
    public final void setSosServerPath(String sosServerPath) {
        this.sosServerPath = sosServerPath;
    }
    public final void setLocalPath(Path path) {
        localPath = path.toString();
    }
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    protected void setInternalFilename(String file) {
        filename = file;
    }
    protected void setInternalRecursive(boolean recurse) {
        recursive = recurse;
    }
    protected void setInternalComment(String text) {
        comment = text;
    }
    protected void setInternalLabel(String text) {
        label = text;
    }
    protected void setInternalVersion(String text) {
        version = text;
    }
    protected String getSosCommand() {
        if (sosCmdDir == null) {
            return COMMAND_SOS_EXE;
        } else {
            return sosCmdDir + File.separator + COMMAND_SOS_EXE;
        }
    }
    protected String getComment() {
        return comment;
    }
    protected String getVersion() {
        return version;
    }
    protected String getLabel() {
        return label;
    }
    protected String getUsername() {
        return sosUsername;
    }
    protected String getPassword() {
        return sosPassword;
    }
    protected String getProjectPath() {
        return projectPath;
    }
    protected String getVssServerPath() {
        return vssServerPath;
    }
    protected String getSosHome() {
        return sosHome;
    }
    protected String getSosServerPath() {
        return sosServerPath;
    }
    protected String getFilename() {
        return filename;
    }
    protected String getNoCompress() {
        return noCompress ? FLAG_NO_COMPRESSION : "";
    }
    protected String getNoCache() {
        return noCache ? FLAG_NO_CACHE : "";
    }
    protected String getVerbose() {
        return verbose ? FLAG_VERBOSE : "";
    }
    protected String getRecursive() {
        return recursive ? FLAG_RECURSION : "";
    }
    protected String getLocalPath() {
        if (localPath == null) {
            return getProject().getBaseDir().getAbsolutePath();
        } else {
            File dir = getProject().resolveFile(localPath);
            if (!dir.exists()) {
                boolean done = dir.mkdirs();
                if (!done) {
                    String msg = "Directory " + localPath + " creation was not "
                        + "successful for an unknown reason";
                    throw new BuildException(msg, getLocation());
                }
                getProject().log("Created dir: " + dir.getAbsolutePath());
            }
            return dir.getAbsolutePath();
        }
    }
    abstract Commandline buildCmdLine();
    public void execute()
        throws BuildException {
        int result = 0;
        buildCmdLine();
        result = run(commandLine);
        if (result == ERROR_EXIT_STATUS) {  
            String msg = "Failed executing: " + commandLine.toString();
            throw new BuildException(msg, getLocation());
        }
    }
    protected int run(Commandline cmd) {
        try {
            Execute exe = new Execute(new LogStreamHandler(this,
                    Project.MSG_INFO,
                    Project.MSG_WARN));
            exe.setAntRun(getProject());
            exe.setWorkingDirectory(getProject().getBaseDir());
            exe.setCommandline(cmd.getCommandline());
            exe.setVMLauncher(false);  
            return exe.execute();
        } catch (java.io.IOException e) {
            throw new BuildException(e, getLocation());
        }
    }
    protected void getRequiredAttributes() {
        commandLine.setExecutable(getSosCommand());
        if (getSosServerPath() == null) {
            throw new BuildException("sosserverpath attribute must be set!", getLocation());
        }
        commandLine.createArgument().setValue(FLAG_SOS_SERVER);
        commandLine.createArgument().setValue(getSosServerPath());
        if (getUsername() == null) {
            throw new BuildException("username attribute must be set!", getLocation());
        }
        commandLine.createArgument().setValue(FLAG_USERNAME);
        commandLine.createArgument().setValue(getUsername());
        commandLine.createArgument().setValue(FLAG_PASSWORD);
        commandLine.createArgument().setValue(getPassword());
        if (getVssServerPath() == null) {
            throw new BuildException("vssserverpath attribute must be set!", getLocation());
        }
        commandLine.createArgument().setValue(FLAG_VSS_SERVER);
        commandLine.createArgument().setValue(getVssServerPath());
        if (getProjectPath() == null) {
            throw new BuildException("projectpath attribute must be set!", getLocation());
        }
        commandLine.createArgument().setValue(FLAG_PROJECT);
        commandLine.createArgument().setValue(getProjectPath());
    }
    protected void getOptionalAttributes() {
        commandLine.createArgument().setValue(getVerbose());
        commandLine.createArgument().setValue(getNoCompress());
        if (getSosHome() == null) {
            commandLine.createArgument().setValue(getNoCache());
        } else {
            commandLine.createArgument().setValue(FLAG_SOS_HOME);
            commandLine.createArgument().setValue(getSosHome());
        }
        if (getLocalPath() != null) {
            commandLine.createArgument().setValue(FLAG_WORKING_DIR);
            commandLine.createArgument().setValue(getLocalPath());
        }
    }
}
