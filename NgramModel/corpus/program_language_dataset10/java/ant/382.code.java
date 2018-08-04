package org.apache.tools.ant.taskdefs.optional.clearcase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
public class CCMklabel extends ClearCase {
    private boolean mReplace = false;
    private boolean mRecurse = false;
    private String mVersion = null;
    private String mTypeName = null;
    private String mVOB = null;
    private String mComment = null;
    private String mCfile = null;
    public void execute() throws BuildException {
        Commandline commandLine = new Commandline();
        Project aProj = getProject();
        int result = 0;
        if (getTypeName() == null) {
            throw new BuildException("Required attribute TypeName not specified");
        }
        if (getViewPath() == null) {
            setViewPath(aProj.getBaseDir().getPath());
        }
        commandLine.setExecutable(getClearToolCommand());
        commandLine.createArgument().setValue(COMMAND_MKLABEL);
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
        if (getReplace()) {
            cmd.createArgument().setValue(FLAG_REPLACE);
        }
        if (getRecurse()) {
            cmd.createArgument().setValue(FLAG_RECURSE);
        }
        if (getVersion() != null) {
            getVersionCommand(cmd);
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
        if (getTypeName() != null) {
            getTypeCommand(cmd);
        }
        cmd.createArgument().setValue(getViewPath());
    }
    public void setReplace(boolean replace) {
        mReplace = replace;
    }
    public boolean getReplace() {
        return mReplace;
    }
    public void setRecurse(boolean recurse) {
        mRecurse = recurse;
    }
    public boolean getRecurse() {
        return mRecurse;
    }
    public void setVersion(String version) {
        mVersion = version;
    }
    public String getVersion() {
        return mVersion;
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
    public void setTypeName(String tn) {
        mTypeName = tn;
    }
    public String getTypeName() {
        return mTypeName;
    }
    public void setVOB(String vob) {
        mVOB = vob;
    }
    public String getVOB() {
        return mVOB;
    }
    private void getVersionCommand(Commandline cmd) {
        if (getVersion() != null) {
            cmd.createArgument().setValue(FLAG_VERSION);
            cmd.createArgument().setValue(getVersion());
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
    private void getTypeCommand(Commandline cmd) {
        String typenm = null;
        if (getTypeName() != null) {
            typenm = getTypeName();
            if (getVOB() != null) {
                typenm += "@" + getVOB();
            }
            cmd.createArgument().setValue(typenm);
        }
    }
    public static final String FLAG_REPLACE = "-replace";
    public static final String FLAG_RECURSE = "-recurse";
    public static final String FLAG_VERSION = "-version";
    public static final String FLAG_COMMENT = "-c";
    public static final String FLAG_COMMENTFILE = "-cfile";
    public static final String FLAG_NOCOMMENT = "-nc";
}