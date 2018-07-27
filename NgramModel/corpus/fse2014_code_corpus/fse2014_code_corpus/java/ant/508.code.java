package org.apache.tools.ant.taskdefs.optional.net;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.email.EmailTask;
public class MimeMail extends EmailTask {
    public void execute()
        throws BuildException {
        log("DEPRECATED - The " + getTaskName() + " task is deprecated. "
            + "Use the mail task instead.");
        super.execute();
    }
}
