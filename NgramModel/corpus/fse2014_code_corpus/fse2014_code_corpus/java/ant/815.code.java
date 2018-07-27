package org.apache.tools.ant.util;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
public final class TaskLogger {
    private Task task;
    public TaskLogger(final Task task) {
        this.task = task;
    }
    public void info(final String message) {
        task.log(message, Project.MSG_INFO);
    }
    public void error(final String message) {
        task.log(message, Project.MSG_ERR);
    }
    public void warning(final String message) {
        task.log(message, Project.MSG_WARN);
    }
    public void verbose(final String message) {
        task.log(message, Project.MSG_VERBOSE);
    }
    public void debug(final String message) {
        task.log(message, Project.MSG_DEBUG);
    }
}
