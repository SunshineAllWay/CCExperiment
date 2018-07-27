package org.apache.tools.ant.taskdefs.optional.clearcase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
public class CCCheckin extends ClearCase {
    private String mComment = null;
    private String mCfile = null;
    private boolean mNwarn = false;
    private boolean mPtime = false;
    private boolean mKeep = false;
    private boolean mIdentical = true;
    public void execute() throws BuildException {
        Commandline commandLine = new Commandline();
        Project aProj = getProject();
        int result = 0;
        if (getViewPath() == null) {
            setViewPath(aProj.getBaseDir().getPath());
        }
        commandLine.setExecutable(getClearToolCommand());
        commandLine.createArgument().setValue(COMMAND_CHECKIN);
        checkOptions(commandLine);
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
        if (getComment() != null) {
            getCommentCommand(cmd);
        } else {
            if (getCommentFile() != null) {
                getCommentFileCommand(cmd);
            } else {
                cmd.createArgument().setValue(FLAG_NOCOMMENT);
            }
        }
        if (getNoWarn()) {
            cmd.createArgument().setValue(FLAG_NOWARN);
        }
        if (getPreserveTime()) {
            cmd.createArgument().setValue(FLAG_PRESERVETIME);
        }
        if (getKeepCopy()) {
            cmd.createArgument().setValue(FLAG_KEEPCOPY);
        }
        if (getIdentical()) {
            cmd.createArgument().setValue(FLAG_IDENTICAL);
        }
        cmd.createArgument().setValue(getViewPath());
    }
    public void setComment(String comment) {
        mComment = comment;
    }
    public String getComment() {
        return mComment;
    }
    public void setCommentFile(String cfile) {
        mCfile = cfile;
    }
    public String getCommentFile() {
        return mCfile;
    }
    public void setNoWarn(boolean nwarn) {
        mNwarn = nwarn;
    }
    public boolean getNoWarn() {
        return mNwarn;
    }
    public void setPreserveTime(boolean ptime) {
        mPtime = ptime;
    }
    public boolean getPreserveTime() {
        return mPtime;
    }
    public void setKeepCopy(boolean keep) {
        mKeep = keep;
    }
    public boolean getKeepCopy() {
        return mKeep;
    }
    public void setIdentical(boolean identical) {
        mIdentical = identical;
    }
    public boolean getIdentical() {
        return mIdentical;
    }
    private void getCommentCommand(Commandline cmd) {
        if (getComment() != null) {
            cmd.createArgument().setValue(FLAG_COMMENT);
            cmd.createArgument().setValue(getComment());
        }
    }
    private void getCommentFileCommand(Commandline cmd) {
        if (getCommentFile() != null) {
            cmd.createArgument().setValue(FLAG_COMMENTFILE);
            cmd.createArgument().setValue(getCommentFile());
        }
    }
    public static final String FLAG_COMMENT = "-c";
    public static final String FLAG_COMMENTFILE = "-cfile";
    public static final String FLAG_NOCOMMENT = "-nc";
    public static final String FLAG_NOWARN = "-nwarn";
    public static final String FLAG_PRESERVETIME = "-ptime";
    public static final String FLAG_KEEPCOPY = "-keep";
    public static final String FLAG_IDENTICAL = "-identical";
}
