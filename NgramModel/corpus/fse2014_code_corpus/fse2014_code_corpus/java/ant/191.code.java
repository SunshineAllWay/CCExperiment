package org.apache.tools.ant.taskdefs;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
public class Dirname extends Task {
    private File file;
    private String property;
    public void setFile(File file) {
        this.file = file;
    }
    public void setProperty(String property) {
        this.property = property;
    }
    public void execute() throws BuildException {
        if (property == null) {
            throw new BuildException("property attribute required", getLocation());
        }
        if (file == null) {
            throw new BuildException("file attribute required", getLocation());
        } else {
            String value = file.getParent();
            getProject().setNewProperty(property, value);
        }
    }
}