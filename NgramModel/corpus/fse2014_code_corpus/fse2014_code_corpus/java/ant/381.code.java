package org.apache.tools.ant.taskdefs.optional.clearcase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
public class CCMkelem extends ClearCase {
    private String  mComment = null;
    private String  mCfile   = null;
    private boolean mNwarn   = false;
    private boolean mPtime   = false;
    private boolean mNoco    = false;
    private boolean mCheckin = false;
    private boolean mMaster  = false;
    private String  mEltype  = null;
    public void execute() throws BuildException {
        Commandline commandLine = new Commandline();
        Project aProj = getProject();
        int result = 0;
        if (getViewPath() == null) {
            setViewPath(aProj.getBaseDir().getPath());
        }
        commandLine.setExecutable(getClearToolCommand());
        commandLine.createArgument().setValue(COMMAND_MKELEM);
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
        if (getNoCheckout() && getCheckin()) {
            throw new BuildException("Should choose either [nocheckout | checkin]");
        }
        if (getNoCheckout()) {
            cmd.createArgument().setValue(FLAG_NOCHECKOUT);
        }
        if (getCheckin()) {
            cmd.createArgument().setValue(FLAG_CHECKIN);
            if (getPreserveTime()) {
                cmd.createArgument().setValue(FLAG_PRESERVETIME);
            }
        }
        if (getMaster()) {
            cmd.createArgument().setValue(FLAG_MASTER);
        }
        if (getEltype() != null) {
            getEltypeCommand(cmd);
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
    public void setNoCheckout(boolean co) {
        mNoco = co;
    }
    public boolean getNoCheckout() {
        return mNoco;
    }
    public void setCheckin(boolean ci) {
        mCheckin = ci;
    }
    public boolean getCheckin() {
        return mCheckin;
    }
    public void setMaster(boolean master) {
        mMaster = master;
    }
    public boolean getMaster() {
        return mMaster;
    }
    public void setEltype(String eltype) {
        mEltype = eltype;
    }
    public String getEltype() {
        return mEltype;
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
    private void getEltypeCommand(Commandline cmd) {
        if (getEltype() != null) {
            cmd.createArgument().setValue(FLAG_ELTYPE);
            cmd.createArgument().setValue(getEltype());
        }
    }
    public static final String FLAG_COMMENT = "-c";
    public static final String FLAG_COMMENTFILE = "-cfile";
    public static final String FLAG_NOCOMMENT = "-nc";
    public static final String FLAG_NOWARN = "-nwarn";
    public static final String FLAG_PRESERVETIME = "-ptime";
    public static final String FLAG_NOCHECKOUT = "-nco";
    public static final String FLAG_CHECKIN = "-ci";
    public static final String FLAG_MASTER = "-master";
    public static final String FLAG_ELTYPE = "-eltype";
}
