package org.apache.tools.ant;
import java.util.EventObject;
public class BuildEvent extends EventObject {
    private static final long serialVersionUID = 4538050075952288486L;
    private final Project project;
    private final Target target;
    private final Task task;
    private String message;
    private int priority = Project.MSG_VERBOSE;
    private Throwable exception;
    public BuildEvent(Project project) {
        super(project);
        this.project = project;
        this.target = null;
        this.task = null;
    }
    public BuildEvent(Target target) {
        super(target);
        this.project = target.getProject();
        this.target = target;
        this.task = null;
    }
    public BuildEvent(Task task) {
        super(task);
        this.project = task.getProject();
        this.target = task.getOwningTarget();
        this.task = task;
    }
    public void setMessage(String message, int priority) {
        this.message = message;
        this.priority = priority;
    }
    public void setException(Throwable exception) {
        this.exception = exception;
    }
    public Project getProject() {
        return project;
    }
    public Target getTarget() {
        return target;
    }
    public Task getTask() {
        return task;
    }
    public String getMessage() {
        return message;
    }
    public int getPriority() {
        return priority;
    }
    public Throwable getException() {
        return exception;
    }
}
