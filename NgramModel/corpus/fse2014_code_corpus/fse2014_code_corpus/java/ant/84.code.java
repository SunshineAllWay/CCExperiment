package org.apache.tools.ant;
import org.apache.tools.ant.dispatch.DispatchUtils;
import java.util.Enumeration;
import java.io.IOException;
public abstract class Task extends ProjectComponent {
    protected Target target;
    protected String taskName;
    protected String taskType;
    protected RuntimeConfigurable wrapper;
    private boolean invalid;
    public Task() {
    }
    public void setOwningTarget(Target target) {
        this.target = target;
    }
    public Target getOwningTarget() {
        return target;
    }
    public void setTaskName(String name) {
        this.taskName = name;
    }
    public String getTaskName() {
        return taskName;
    }
    public void setTaskType(String type) {
        this.taskType = type;
    }
    public void init() throws BuildException {
    }
    public void execute() throws BuildException {
    }
    public RuntimeConfigurable getRuntimeConfigurableWrapper() {
        if (wrapper == null) {
            wrapper = new RuntimeConfigurable(this, getTaskName());
        }
        return wrapper;
    }
    public void setRuntimeConfigurableWrapper(RuntimeConfigurable wrapper) {
        this.wrapper = wrapper;
    }
    public void maybeConfigure() throws BuildException {
        if (!invalid) {
            if (wrapper != null) {
                wrapper.maybeConfigure(getProject());
            }
        } else {
            getReplacement();
        }
    }
    public void reconfigure() {
        if (wrapper != null) {
            wrapper.reconfigure(getProject());
        }
    }
    protected void handleOutput(String output) {
        log(output, Project.MSG_INFO);
    }
    protected void handleFlush(String output) {
        handleOutput(output);
    }
    protected int handleInput(byte[] buffer, int offset, int length)
        throws IOException {
        return getProject().defaultInput(buffer, offset, length);
    }
    protected void handleErrorOutput(String output) {
        log(output, Project.MSG_WARN);
    }
    protected void handleErrorFlush(String output) {
        handleErrorOutput(output);
    }
    public void log(String msg) {
        log(msg, Project.MSG_INFO);
    }
    public void log(String msg, int msgLevel) {
        if (getProject() != null) {
            getProject().log(this, msg, msgLevel);
        } else {
            super.log(msg, msgLevel);
        }
    }
    public void log(Throwable t, int msgLevel) {
        if (t != null) {
            log(t.getMessage(), t, msgLevel);
        }
    }
    public void log(String msg, Throwable t, int msgLevel) {
        if (getProject() != null) {
            getProject().log(this, msg, t, msgLevel);
        } else {
            super.log(msg, msgLevel);
        }
    }
    public final void perform() {
        if (!invalid) {
            getProject().fireTaskStarted(this);
            Throwable reason = null;
            try {
                maybeConfigure();
                DispatchUtils.execute(this);
            } catch (BuildException ex) {
                if (ex.getLocation() == Location.UNKNOWN_LOCATION) {
                    ex.setLocation(getLocation());
                }
                reason = ex;
                throw ex;
            } catch (Exception ex) {
                reason = ex;
                BuildException be = new BuildException(ex);
                be.setLocation(getLocation());
                throw be;
            } catch (Error ex) {
                reason = ex;
                throw ex;
            } finally {
                getProject().fireTaskFinished(this, reason);
            }
        } else {
            UnknownElement ue = getReplacement();
            Task task = ue.getTask();
            task.perform();
        }
    }
    final void markInvalid() {
        invalid = true;
    }
    protected final boolean isInvalid() {
        return invalid;
    }
    private UnknownElement replacement;
    private UnknownElement getReplacement() {
        if (replacement == null) {
            replacement = new UnknownElement(taskType);
            replacement.setProject(getProject());
            replacement.setTaskType(taskType);
            replacement.setTaskName(taskName);
            replacement.setLocation(getLocation());
            replacement.setOwningTarget(target);
            replacement.setRuntimeConfigurableWrapper(wrapper);
            wrapper.setProxy(replacement);
            replaceChildren(wrapper, replacement);
            target.replaceChild(this, replacement);
            replacement.maybeConfigure();
        }
        return replacement;
    }
    private void replaceChildren(RuntimeConfigurable wrapper,
                                 UnknownElement parentElement) {
        Enumeration e = wrapper.getChildren();
        while (e.hasMoreElements()) {
            RuntimeConfigurable childWrapper =
                (RuntimeConfigurable) e.nextElement();
            UnknownElement childElement =
                new UnknownElement(childWrapper.getElementTag());
            parentElement.addChild(childElement);
            childElement.setProject(getProject());
            childElement.setRuntimeConfigurableWrapper(childWrapper);
            childWrapper.setProxy(childElement);
            replaceChildren(childWrapper, childElement);
        }
    }
    public String getTaskType() {
        return taskType;
    }
    protected RuntimeConfigurable getWrapper() {
        return wrapper;
    }
    public final void bindToOwner(Task owner) {
        setProject(owner.getProject());
        setOwningTarget(owner.getOwningTarget());
        setTaskName(owner.getTaskName());
        setDescription(owner.getDescription());
        setLocation(owner.getLocation());
        setTaskType(owner.getTaskType());
    }
}
