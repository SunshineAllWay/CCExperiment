package org.apache.tools.ant.taskdefs;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.Task;
public class LogStreamHandler extends PumpStreamHandler {
    public LogStreamHandler(Task task, int outlevel, int errlevel) {
        this((ProjectComponent) task, outlevel, errlevel);
    }
    public LogStreamHandler(ProjectComponent pc, int outlevel, int errlevel) {
        super(new LogOutputStream(pc, outlevel),
              new LogOutputStream(pc, errlevel));
    }
    public void stop() {
        super.stop();
        try {
            getErr().close();
            getOut().close();
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
