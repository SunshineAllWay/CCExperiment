package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Diagnostics;
import org.apache.tools.ant.Task;
public class DiagnosticsTask extends Task {
    private static final String[] ARGS = new String[0];
    public void execute() throws BuildException {
        Diagnostics.main(ARGS);
    }
}
