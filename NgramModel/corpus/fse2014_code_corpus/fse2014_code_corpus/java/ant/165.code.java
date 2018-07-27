package org.apache.tools.ant.taskdefs;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
public class Basename extends Task {
    private File file;
    private String property;
    private String suffix;
    public void setFile(File file) {
        this.file = file;
    }
    public void setProperty(String property) {
        this.property  = property;
    }
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    public void execute() throws BuildException {
        if (property == null) {
            throw new BuildException("property attribute required", getLocation());
        }
        if (file == null) {
            throw new BuildException("file attribute required", getLocation());
        }
        String value = file.getName();
        if (suffix != null && value.endsWith(suffix)) {
            int pos = value.length() - suffix.length();
            if (pos > 0 && suffix.charAt(0) != '.'
                && value.charAt(pos - 1) == '.') {
                pos--;
            }
            value = value.substring(0, pos);
        }
        getProject().setNewProperty(property, value);
    }
}
