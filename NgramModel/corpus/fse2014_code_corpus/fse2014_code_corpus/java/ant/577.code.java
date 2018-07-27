package org.apache.tools.ant.taskdefs.optional.windows;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.ExecuteOn;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.FileSet;
import java.io.File;
public class Attrib extends ExecuteOn {
    private static final String ATTR_READONLY = "R";
    private static final String ATTR_ARCHIVE  = "A";
    private static final String ATTR_SYSTEM   = "S";
    private static final String ATTR_HIDDEN   = "H";
    private static final String SET    = "+";
    private static final String UNSET  = "-";
    private boolean haveAttr = false;
    public Attrib() {
        super.setExecutable("attrib");
        super.setParallel(false);
    }
    public void setFile(File src) {
        FileSet fs = new FileSet();
        fs.setFile(src);
        addFileset(fs);
    }
    public void setReadonly(boolean value) {
        addArg(value, ATTR_READONLY);
    }
    public void setArchive(boolean value) {
        addArg(value, ATTR_ARCHIVE);
    }
    public void setSystem(boolean value) {
        addArg(value, ATTR_SYSTEM);
    }
    public void setHidden(boolean value) {
        addArg(value, ATTR_HIDDEN);
    }
    protected void checkConfiguration() {
        if (!haveAttr()) {
            throw new BuildException("Missing attribute parameter",
                                     getLocation());
        }
        super.checkConfiguration();
    }
    public void setExecutable(String e) {
        throw new BuildException(getTaskType()
            + " doesn\'t support the executable attribute", getLocation());
    }
    public void setCommand(String e) {
        throw new BuildException(getTaskType()
            + " doesn\'t support the command attribute", getLocation());
    }
    public void setAddsourcefile(boolean b) {
        throw new BuildException(getTaskType()
            + " doesn\'t support the addsourcefile attribute", getLocation());
    }
    public void setSkipEmptyFilesets(boolean skip) {
        throw new BuildException(getTaskType() + " doesn\'t support the "
                                 + "skipemptyfileset attribute",
                                 getLocation());
    }
    public void setParallel(boolean parallel) {
        throw new BuildException(getTaskType()
                                 + " doesn\'t support the parallel attribute",
                                 getLocation());
    }
    public void setMaxParallel(int max) {
        throw new BuildException(getTaskType()
                                 + " doesn\'t support the maxparallel attribute",
                                 getLocation());
    }
    protected boolean isValidOs() {
        return getOs() == null && getOsFamily() == null ?
            Os.isFamily(Os.FAMILY_WINDOWS) : super.isValidOs();
    }
    private static String getSignString(boolean attr) {
        return (attr ? SET : UNSET);
    }
    private void addArg(boolean sign, String attribute) {
        createArg().setValue(getSignString(sign) + attribute);
        haveAttr = true;
    }
    private boolean haveAttr() {
        return haveAttr;
    }
}
