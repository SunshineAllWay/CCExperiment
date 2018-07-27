package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;
public class Chmod extends ExecuteOn {
    private FileSet defaultSet = new FileSet();
    private boolean defaultSetDefined = false;
    private boolean havePerm = false;
    public Chmod() {
        super.setExecutable("chmod");
        super.setParallel(true);
        super.setSkipEmptyFilesets(true);
    }
    public void setProject(Project project) {
        super.setProject(project);
        defaultSet.setProject(project);
    }
    public void setFile(File src) {
        FileSet fs = new FileSet();
        fs.setFile(src);
        addFileset(fs);
    }
    public void setDir(File src) {
        defaultSet.setDir(src);
    }
    public void setPerm(String perm) {
        createArg().setValue(perm);
        havePerm = true;
    }
    public PatternSet.NameEntry createInclude() {
        defaultSetDefined = true;
        return defaultSet.createInclude();
    }
    public PatternSet.NameEntry createExclude() {
        defaultSetDefined = true;
        return defaultSet.createExclude();
    }
    public PatternSet createPatternSet() {
        defaultSetDefined = true;
        return defaultSet.createPatternSet();
    }
    public void setIncludes(String includes) {
        defaultSetDefined = true;
        defaultSet.setIncludes(includes);
    }
    public void setExcludes(String excludes) {
        defaultSetDefined = true;
        defaultSet.setExcludes(excludes);
    }
    public void setDefaultexcludes(boolean useDefaultExcludes) {
        defaultSetDefined = true;
        defaultSet.setDefaultexcludes(useDefaultExcludes);
    }
    protected void checkConfiguration() {
        if (!havePerm) {
            throw new BuildException("Required attribute perm not set in chmod",
                                     getLocation());
        }
        if (defaultSetDefined && defaultSet.getDir(getProject()) != null) {
            addFileset(defaultSet);
        }
        super.checkConfiguration();
    }
    public void execute() throws BuildException {
        if (defaultSetDefined || defaultSet.getDir(getProject()) == null) {
            try {
                super.execute();
            } finally {
                if (defaultSetDefined && defaultSet.getDir(getProject()) != null) {
                    filesets.removeElement(defaultSet);
                }
            }
        } else if (isValidOs()) {
            Execute execute = prepareExec();
            Commandline cloned = (Commandline) cmdl.clone();
            cloned.createArgument().setValue(defaultSet.getDir(getProject())
                                             .getPath());
            try {
                execute.setCommandline(cloned.getCommandline());
                runExecute(execute);
            } catch (IOException e) {
                throw new BuildException("Execute failed: " + e, e, getLocation());
            } finally {
                logFlush();
            }
        }
    }
    public void setExecutable(String e) {
        throw new BuildException(getTaskType()
            + " doesn\'t support the executable attribute", getLocation());
    }
    public void setCommand(Commandline cmdl) {
        throw new BuildException(getTaskType()
            + " doesn\'t support the command attribute", getLocation());
    }
    public void setSkipEmptyFilesets(boolean skip) {
        throw new BuildException(getTaskType()
            + " doesn\'t support the skipemptyfileset attribute", getLocation());
    }
    public void setAddsourcefile(boolean b) {
        throw new BuildException(getTaskType()
            + " doesn\'t support the addsourcefile attribute", getLocation());
    }
    protected boolean isValidOs() {
        return getOs() == null && getOsFamily() == null
            ? Os.isFamily(Os.FAMILY_UNIX) : super.isValidOs();
    }
}
