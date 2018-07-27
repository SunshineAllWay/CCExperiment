package org.apache.tools.ant.taskdefs.optional.clearcase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
public class CCLock extends ClearCase {
    private boolean mReplace = false;
    private boolean mObsolete = false;
    private String mComment = null;
    private String mNusers = null;
    private String mPname = null;
    private String mObjselect = null;
    public void execute() throws BuildException {
        Commandline commandLine = new Commandline();
        Project aProj = getProject();
        int result = 0;
        if (getViewPath() == null) {
            setViewPath(aProj.getBaseDir().getPath());
        }
        commandLine.setExecutable(getClearToolCommand());
        commandLine.createArgument().setValue(COMMAND_LOCK);
        checkOptions(commandLine);
        if (!getFailOnErr()) {
            getProject().log("Ignoring any errors that occur for: "
                    + getOpType(), Project.MSG_VERBOSE);
        }
        result = run(commandLine);
        if (Execute.isFailure(result) && getFailOnErr()) {
            String msg = "Failed executing: " + commandLine.toString();
            throw new BuildException(msg, getLocation());
        }
    }
private void checkOptions(Commandline cmd) {
        if (getReplace()) {
            cmd.createArgument().setValue(FLAG_REPLACE);
        }
        if (getObsolete()) {
            cmd.createArgument().setValue(FLAG_OBSOLETE);
        } else {
            getNusersCommand(cmd);
        }
        getCommentCommand(cmd);
        if (getObjselect() == null && getPname() == null) {
            throw new BuildException("Should select either an element "
            + "(pname) or an object (objselect)");
        }
        getPnameCommand(cmd);
        if (getObjselect() != null) {
            cmd.createArgument().setValue(getObjselect());
        }
}
    public void setReplace(boolean replace) {
        mReplace = replace;
    }
    public boolean getReplace() {
        return mReplace;
    }
    public void setObsolete(boolean obsolete) {
        mObsolete = obsolete;
    }
    public boolean getObsolete() {
        return mObsolete;
    }
    public void setNusers(String nusers) {
        mNusers = nusers;
    }
    public String getNusers() {
        return mNusers;
    }
    public void setComment(String comment) {
        mComment = comment;
    }
    public String getComment() {
        return mComment;
    }
    public void setPname(String pname) {
        mPname = pname;
    }
    public String getPname() {
        return mPname;
    }
    public void setObjSel(String objsel) {
        mObjselect = objsel;
    }
    public void setObjselect(String objselect) {
        mObjselect = objselect;
    }
    public String getObjselect() {
        return mObjselect;
    }
    private void getNusersCommand(Commandline cmd) {
        if (getNusers() == null) {
            return;
        } else {
            cmd.createArgument().setValue(FLAG_NUSERS);
            cmd.createArgument().setValue(getNusers());
        }
    }
    private void getCommentCommand(Commandline cmd) {
        if (getComment() == null) {
            return;
        } else {
            cmd.createArgument().setValue(FLAG_COMMENT);
            cmd.createArgument().setValue(getComment());
        }
    }
    private void getPnameCommand(Commandline cmd) {
        if (getPname() == null) {
            return;
        } else {
            cmd.createArgument().setValue(FLAG_PNAME);
            cmd.createArgument().setValue(getPname());
        }
    }
    private String getOpType() {
        if (getPname() != null) {
            return getPname();
        } else {
            return getObjselect();
        }
    }
    public static final String FLAG_REPLACE = "-replace";
    public static final String FLAG_NUSERS = "-nusers";
    public static final String FLAG_OBSOLETE = "-obsolete";
    public static final String FLAG_COMMENT = "-comment";
    public static final String FLAG_PNAME = "-pname";
}
