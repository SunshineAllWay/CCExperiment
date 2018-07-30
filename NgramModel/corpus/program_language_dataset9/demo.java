package test;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.taskdefs.Echo;
import java.util.*;
public class SpecialSeq extends Task implements TaskContainer {
    private Vector nestedTasks = new Vector();
    private FileSet fileset;
    private Echo nestedEcho;

    public void addTask(Task nestedTask) {
        nestedTasks.addElement(nestedTask);
    }

    public void execute() throws BuildException {
        if (fileset == null || fileset.getDir(getProject()) == null) {
            throw new BuildException("Fileset was not configured");
        }
        for (Enumeration e = nestedTasks.elements(); e.hasMoreElements();) {
            Task nestedTask = (Task) e.nextElement();
            nestedTask.perform();
        }
        nestedEcho.reconfigure();
        nestedEcho.perform();
    }

    public void addFileset(FileSet fileset) {
        this.fileset = fileset;
    }
    
    public void addNested(Echo nestedEcho) {
        this.nestedEcho = nestedEcho;
    }
}
