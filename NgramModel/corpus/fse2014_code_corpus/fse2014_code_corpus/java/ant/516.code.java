package org.apache.tools.ant.taskdefs.optional.perforce;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
public class P4Counter extends P4Base {
    public String counter = null;
    public String property = null;
    public boolean shouldSetValue = false;
    public boolean shouldSetProperty = false;
    public int value = 0;
    public void setName(String counter) {
        this.counter = counter;
    }
    public void setValue(int value) {
        this.value = value;
        shouldSetValue = true;
    }
    public void setProperty(String property) {
        this.property = property;
        shouldSetProperty = true;
    }
    public void execute() throws BuildException {
        if ((counter == null) || counter.length() == 0) {
            throw new BuildException("No counter specified to retrieve");
        }
        if (shouldSetValue && shouldSetProperty) {
            throw new BuildException("Cannot both set the value of the property and retrieve the "
                + "value of the property.");
        }
        String command = "counter " + P4CmdOpts + " " + counter;
        if (!shouldSetProperty) {
            command = "-s " + command;
        }
        if (shouldSetValue) {
            command += " " + value;
        }
        if (shouldSetProperty) {
            final Project myProj = getProject();
            P4Handler handler = new P4HandlerAdapter() {
                public void process(String line) {
                    log("P4Counter retrieved line \"" + line + "\"", Project.MSG_VERBOSE);
                    try {
                        value = Integer.parseInt(line);
                        myProj.setProperty(property, "" + value);
                    } catch (NumberFormatException nfe) {
                        throw new BuildException("Perforce error. "
                        + "Could not retrieve counter value.");
                    }
                }
            };
            execP4Command(command, handler);
        } else {
            execP4Command(command, new SimpleP4OutputHandler(this));
        }
    }
}
