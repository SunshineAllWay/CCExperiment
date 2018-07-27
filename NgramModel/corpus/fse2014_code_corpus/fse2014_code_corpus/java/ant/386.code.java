package org.apache.tools.ant.taskdefs.optional.clearcase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
public class CCUnlock extends ClearCase {
    private String mComment = null;
    private String mPname = null;
    public void execute() throws BuildException {
        Commandline commandLine = new Commandline();
        Project aProj = getProject();
        int result = 0;
        if (getViewPath() == null) {
            setViewPath(aProj.getBaseDir().getPath());
        }
        commandLine.setExecutable(getClearToolCommand());
        commandLine.createArgument().setValue(COMMAND_UNLOCK);
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
        getCommentCommand(cmd);
        if (getObjSelect() == null && getPname() == null) {
            throw new BuildException("Should select either an element "
            + "(pname) or an object (objselect)");
        }
        getPnameCommand(cmd);
        if (getObjSelect() != null) {
            cmd.createArgument().setValue(getObjSelect());
        }
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
    public void setObjselect(String objselect) {
        setObjSelect(objselect);
    }
    public void setObjSel(String objsel) {
        setObjSelect(objsel);
    }
    public String getObjselect() {
        return getObjSelect();
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
            return getObjSelect();
        }
    }
    public static final String FLAG_COMMENT = "-comment";
    public static final String FLAG_PNAME = "-pname";
}
