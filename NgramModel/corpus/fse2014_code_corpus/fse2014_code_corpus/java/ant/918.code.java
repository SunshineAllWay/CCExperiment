package org.apache.tools.ant;
import org.apache.tools.ant.dispatch.DispatchTask;
public class PickOneTask extends DispatchTask {
    public void list() {
        throw new BuildException("list");
    }
    public void show() {
        throw new BuildException("show");
    }
}
