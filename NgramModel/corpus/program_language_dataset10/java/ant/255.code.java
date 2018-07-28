package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.util.StringUtils;
public class Retry extends Task implements TaskContainer {
    private Task nestedTask;
    private int retryCount = 1;
    public synchronized void addTask(Task t) {
        if (nestedTask != null) {
            throw new BuildException(
                "The retry task container accepts a single nested task"
                + " (which may be a sequential task container)");
        }
        nestedTask = t;
    }
    public void setRetryCount(int n) {
        retryCount = n;
    }
    public void execute() throws BuildException {
        StringBuffer errorMessages = new StringBuffer();
        for (int i = 0; i <= retryCount; i++) {
            try {
                nestedTask.perform();
                break;
            } catch (Exception e) {
                errorMessages.append(e.getMessage());
                if (i >= retryCount) {
                    StringBuffer exceptionMessage = new StringBuffer();
                    exceptionMessage.append("Task [").append(nestedTask.getTaskName());
                    exceptionMessage.append("] failed after [").append(retryCount);
                    exceptionMessage.append("] attempts; giving up.").append(StringUtils.LINE_SEP);
                    exceptionMessage.append("Error messages:").append(StringUtils.LINE_SEP);
                    exceptionMessage.append(errorMessages);
                    throw new BuildException(exceptionMessage.toString(), getLocation());
                }
                log("Attempt [" + i + "]: error occurred; retrying...", e, Project.MSG_INFO);
                errorMessages.append(StringUtils.LINE_SEP);
            }
        }
    }
}