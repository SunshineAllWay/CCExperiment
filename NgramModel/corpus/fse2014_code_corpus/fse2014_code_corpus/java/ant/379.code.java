package org.apache.tools.ant.taskdefs.optional.clearcase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
public class CCMkbl extends ClearCase {
    private String mComment = null;
    private String mCfile = null;
    private String mBaselineRootName = null;
    private boolean mNwarn = false;
    private boolean mIdentical = true;
    private boolean mFull = false;
    private boolean mNlabel = false;
    public void execute() throws BuildException {
        Commandline commandLine = new Commandline();
        Project aProj = getProject();
        int result = 0;
        if (getViewPath() == null) {
            setViewPath(aProj.getBaseDir().getPath());
        }
        commandLine.setExecutable(getClearToolCommand());
        commandLine.createArgument().setValue(COMMAND_MKBL);
        checkOptions(commandLine);
        if (!getFailOnErr()) {
            getProject().log("Ignoring any errors that occur for: "
                    + getBaselineRootName(), Project.MSG_VERBOSE);
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
        if (getIdentical()) {
            cmd.createArgument().setValue(FLAG_IDENTICAL);
        }
       if (getFull()) {
           cmd.createArgument().setValue(FLAG_FULL);
       } else {
           cmd.createArgument().setValue(FLAG_INCREMENTAL);
       }
       if (getNlabel()) {
           cmd.createArgument().setValue(FLAG_NLABEL);
       }
        cmd.createArgument().setValue(getBaselineRootName());
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
    public void setBaselineRootName(String baselineRootName) {
        mBaselineRootName = baselineRootName;
    }
    public String getBaselineRootName() {
        return mBaselineRootName;
    }
    public void setNoWarn(boolean nwarn) {
        mNwarn = nwarn;
    }
    public boolean getNoWarn() {
        return mNwarn;
    }
    public void setIdentical(boolean identical) {
        mIdentical = identical;
    }
    public boolean getIdentical() {
        return mIdentical;
    }
    public void setFull(boolean full) {
        mFull = full;
    }
    public boolean getFull() {
        return mFull;
    }
    public void setNlabel(boolean nlabel) {
        mNlabel = nlabel;
    }
    public boolean getNlabel() {
        return mNlabel;
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
    public static final String FLAG_IDENTICAL = "-identical";
    public static final String FLAG_INCREMENTAL = "-incremental";
    public static final String FLAG_FULL = "-full";
    public static final String FLAG_NLABEL = "-nlabel";
}
