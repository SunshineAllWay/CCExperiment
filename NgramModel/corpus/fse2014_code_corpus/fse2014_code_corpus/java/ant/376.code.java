package org.apache.tools.ant.taskdefs.optional.clearcase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
public class CCCheckout extends ClearCase {
    private boolean mReserved = true;
    private String mOut = null;
    private boolean mNdata = false;
    private String mBranch = null;
    private boolean mVersion = false;
    private boolean mNwarn = false;
    private String mComment = null;
    private String mCfile = null;
    private boolean mNotco = true;
    public void execute() throws BuildException {
        Commandline commandLine = new Commandline();
        Project aProj = getProject();
        int result = 0;
        if (getViewPath() == null) {
            setViewPath(aProj.getBaseDir().getPath());
        }
        commandLine.setExecutable(getClearToolCommand());
        commandLine.createArgument().setValue(COMMAND_CHECKOUT);
        checkOptions(commandLine);
        if (!getNotco() && lsCheckout()) {
            getProject().log("Already checked out in this view: "
                    + getViewPathBasename(), Project.MSG_VERBOSE);
            return;
        }
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
    private boolean lsCheckout() {
        Commandline cmdl = new Commandline();
        String result;
        cmdl.setExecutable(getClearToolCommand());
        cmdl.createArgument().setValue(COMMAND_LSCO);
        cmdl.createArgument().setValue("-cview");
        cmdl.createArgument().setValue("-short");
        cmdl.createArgument().setValue("-d");
        cmdl.createArgument().setValue(getViewPath());
        result = runS(cmdl);
        return (result != null && result.length() > 0) ? true : false;
    }
    private void checkOptions(Commandline cmd) {
        if (getReserved()) {
            cmd.createArgument().setValue(FLAG_RESERVED);
        } else {
            cmd.createArgument().setValue(FLAG_UNRESERVED);
        }
        if (getOut() != null) {
            getOutCommand(cmd);
        } else {
            if (getNoData()) {
                cmd.createArgument().setValue(FLAG_NODATA);
            }
        }
        if (getBranch() != null) {
            getBranchCommand(cmd);
        } else {
            if (getVersion()) {
                cmd.createArgument().setValue(FLAG_VERSION);
            }
        }
        if (getNoWarn()) {
            cmd.createArgument().setValue(FLAG_NOWARN);
        }
        if (getComment() != null) {
            getCommentCommand(cmd);
        } else {
            if (getCommentFile() != null) {
                getCommentFileCommand(cmd);
            } else {
                cmd.createArgument().setValue(FLAG_NOCOMMENT);
            }
        }
        cmd.createArgument().setValue(getViewPath());
    }
    public void setReserved(boolean reserved) {
        mReserved = reserved;
    }
    public boolean getReserved() {
        return mReserved;
    }
    public void setNotco(boolean notco) {
        mNotco = notco;
    }
    public boolean getNotco() {
        return mNotco;
    }
    public void setOut(String outf) {
        mOut = outf;
    }
    public String getOut() {
        return mOut;
    }
    public void setNoData(boolean ndata) {
        mNdata = ndata;
    }
    public boolean getNoData() {
        return mNdata;
    }
    public void setBranch(String branch) {
        mBranch = branch;
    }
    public String getBranch() {
        return mBranch;
    }
    public void setVersion(boolean version) {
        mVersion = version;
    }
    public boolean getVersion() {
        return mVersion;
    }
    public void setNoWarn(boolean nwarn) {
        mNwarn = nwarn;
    }
    public boolean getNoWarn() {
        return mNwarn;
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
    private void getOutCommand(Commandline cmd) {
        if (getOut() != null) {
            cmd.createArgument().setValue(FLAG_OUT);
            cmd.createArgument().setValue(getOut());
        }
    }
    private void getBranchCommand(Commandline cmd) {
        if (getBranch() != null) {
            cmd.createArgument().setValue(FLAG_BRANCH);
            cmd.createArgument().setValue(getBranch());
        }
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
    public static final String FLAG_RESERVED = "-reserved";
    public static final String FLAG_UNRESERVED = "-unreserved";
    public static final String FLAG_OUT = "-out";
    public static final String FLAG_NODATA = "-ndata";
    public static final String FLAG_BRANCH = "-branch";
    public static final String FLAG_VERSION = "-version";
    public static final String FLAG_NOWARN = "-nwarn";
    public static final String FLAG_COMMENT = "-c";
    public static final String FLAG_COMMENTFILE = "-cfile";
    public static final String FLAG_NOCOMMENT = "-nc";
}
