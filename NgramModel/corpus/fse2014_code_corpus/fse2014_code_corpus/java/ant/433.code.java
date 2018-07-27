package org.apache.tools.ant.taskdefs.optional.extension;
import org.apache.tools.ant.BuildException;
public class ExtraAttribute {
    private String name;
    private String value;
    public void setName(final String name) {
        this.name = name;
    }
    public void setValue(final String value) {
        this.value = value;
    }
    String getName() {
        return name;
    }
    String getValue() {
        return value;
    }
    public void validate() throws BuildException {
        if (null == name) {
            final String message = "Missing name from parameter.";
            throw new BuildException(message);
        } else if (null == value) {
            final String message = "Missing value from parameter " + name + ".";
            throw new BuildException(message);
        }
    }
}
