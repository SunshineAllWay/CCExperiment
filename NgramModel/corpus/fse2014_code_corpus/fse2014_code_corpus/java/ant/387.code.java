package org.apache.tools.ant.taskdefs.optional.clearcase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
public class CCUpdate extends ClearCase {
    private boolean mGraphical = false;
    private boolean mOverwrite = false;
    private boolean mRename = false;
    private boolean mCtime = false;
    private boolean mPtime = false;
    private String mLog = null;
    public void execute() throws BuildException {
        Commandline commandLine = new Commandline();
        Project aProj = getProject();
        int result = 0;
        if (getViewPath() == null) {
            setViewPath(aProj.getBaseDir().getPath());
        }
        commandLine.setExecutable(getClearToolCommand());
        commandLine.createArgument().setValue(COMMAND_UPDATE);
        checkOptions(commandLine);
        getProject().log(commandLine.toString(), Project.MSG_DEBUG);
        if (!getFailOnErr()) {
            getProject().log("Ignoring any errors that occur for: "
                    + getViewPathBasename(), Project.MSG_VERBOSE);
        }
        result = run(commandLine);
        if (Execute.isFailure(result) && getFailOnErr()) {
            String msg = "Failed executing: " + commandLine.toString();
            throw new BuildException(msg, getLocation());
        }
    }
    private void checkOptions(Commandline cmd) {
        if (getGraphical()) {
            cmd.createArgument().setValue(FLAG_GRAPHICAL);
        } else {
            if (getOverwrite()) {
                cmd.createArgument().setValue(FLAG_OVERWRITE);
            } else {
                if (getRename()) {
                    cmd.createArgument().setValue(FLAG_RENAME);
                } else {
                    cmd.createArgument().setValue(FLAG_NOVERWRITE);
                }
            }
            if (getCurrentTime()) {
                cmd.createArgument().setValue(FLAG_CURRENTTIME);
            } else {
                if (getPreserveTime()) {
                    cmd.createArgument().setValue(FLAG_PRESERVETIME);
                }
            }
            getLogCommand(cmd);
        }
        cmd.createArgument().setValue(getViewPath());
    }
    public void setGraphical(boolean graphical) {
        mGraphical = graphical;
    }
    public boolean getGraphical() {
        return mGraphical;
    }
    public void setOverwrite(boolean ow) {
        mOverwrite = ow;
    }
    public boolean getOverwrite() {
        return mOverwrite;
    }
    public void setRename(boolean ren) {
        mRename = ren;
    }
    public boolean getRename() {
        return mRename;
    }
    public void setCurrentTime(boolean ct) {
        mCtime = ct;
    }
    public boolean getCurrentTime() {
        return mCtime;
    }
    public void setPreserveTime(boolean pt) {
        mPtime = pt;
    }
    public boolean getPreserveTime() {
        return mPtime;
    }
    public void setLog(String log) {
        mLog = log;
    }
    public String getLog() {
        return mLog;
    }
    private void getLogCommand(Commandline cmd) {
        if (getLog() == null) {
            return;
        } else {
            cmd.createArgument().setValue(FLAG_LOG);
            cmd.createArgument().setValue(getLog());
        }
    }
    public static final String FLAG_GRAPHICAL = "-graphical";
    public static final String FLAG_LOG = "-log";
    public static final String FLAG_OVERWRITE = "-overwrite";
    public static final String FLAG_NOVERWRITE = "-noverwrite";
    public static final String FLAG_RENAME = "-rename";
    public static final String FLAG_CURRENTTIME = "-ctime";
    public static final String FLAG_PRESERVETIME = "-ptime";
}
