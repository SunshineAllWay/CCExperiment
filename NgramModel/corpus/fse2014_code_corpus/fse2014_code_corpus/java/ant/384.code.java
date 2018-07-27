package org.apache.tools.ant.taskdefs.optional.clearcase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
public class CCRmtype extends ClearCase {
    private String mTypeKind = null;
    private String mTypeName = null;
    private String mVOB = null;
    private String mComment = null;
    private String mCfile = null;
    private boolean mRmall = false;
    private boolean mIgnore = false;
    public void execute() throws BuildException {
        Commandline commandLine = new Commandline();
        int result = 0;
        if (getTypeKind() == null) {
            throw new BuildException("Required attribute TypeKind not specified");
        }
        if (getTypeName() == null) {
            throw new BuildException("Required attribute TypeName not specified");
        }
        commandLine.setExecutable(getClearToolCommand());
        commandLine.createArgument().setValue(COMMAND_RMTYPE);
        checkOptions(commandLine);
        if (!getFailOnErr()) {
            getProject().log("Ignoring any errors that occur for: "
                    + getTypeSpecifier(), Project.MSG_VERBOSE);
        }
        result = run(commandLine);
        if (Execute.isFailure(result) && getFailOnErr()) {
            String msg = "Failed executing: " + commandLine.toString();
            throw new BuildException(msg, getLocation());
        }
    }
    private void checkOptions(Commandline cmd) {
        if (getIgnore()) {
            cmd.createArgument().setValue(FLAG_IGNORE);
        }
        if (getRmAll()) {
            cmd.createArgument().setValue(FLAG_RMALL);
            cmd.createArgument().setValue(FLAG_FORCE);
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
        cmd.createArgument().setValue(getTypeSpecifier());
    }
    public void setIgnore(boolean ignore) {
        mIgnore = ignore;
    }
    public boolean getIgnore() {
        return mIgnore;
    }
    public void setRmAll(boolean rmall) {
        mRmall = rmall;
    }
    public boolean getRmAll() {
        return mRmall;
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
    public void setTypeKind(String tk) {
        mTypeKind = tk;
    }
    public String getTypeKind() {
        return mTypeKind;
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
    private String getTypeSpecifier() {
        String tkind = getTypeKind();
        String tname = getTypeName();
        String typeSpec = null;
        typeSpec = tkind + ":" + tname;
        if (getVOB() != null) {
            typeSpec += "@" + getVOB();
        }
        return typeSpec;
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
    public static final String FLAG_IGNORE = "-ignore";
    public static final String FLAG_RMALL = "-rmall";
    public static final String FLAG_FORCE = "-force";
    public static final String FLAG_COMMENT = "-c";
    public static final String FLAG_COMMENTFILE = "-cfile";
    public static final String FLAG_NOCOMMENT = "-nc";
}
