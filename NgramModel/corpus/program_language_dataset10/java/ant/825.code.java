package org.apache.tools.ant.util;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
public class WorkerAnt extends Thread {
    private Task task;
    private Object notify;
    private volatile boolean finished = false;
    private volatile BuildException buildException;
    private volatile Throwable exception;
    public static final String ERROR_NO_TASK = "No task defined";
    public WorkerAnt(Task task, Object notify) {
        this.task = task;
        this.notify = notify != null ? notify : this;
    }
    public WorkerAnt(Task task) {
        this(task, null);
    }
    public synchronized BuildException getBuildException() {
        return buildException;
    }
    public synchronized Throwable getException() {
        return exception;
    }
    public Task getTask() {
        return task;
    }
    public synchronized boolean isFinished() {
        return finished;
    }
    public void waitUntilFinished(long timeout) throws InterruptedException {
        synchronized (notify) {
            if (!finished) {
                notify.wait(timeout);
            }
        }
    }
    public void rethrowAnyBuildException() {
        BuildException ex = getBuildException();
        if (ex != null) {
            throw ex;
        }
    }
    private synchronized void caught(Throwable thrown) {
        exception = thrown;
        buildException = (thrown instanceof BuildException)
            ? (BuildException) thrown
            : new BuildException(thrown);
    }
    public void run() {
        try {
            if (task != null) {
                task.execute();
            }
        } catch (Throwable thrown) {
            caught(thrown);
        } finally {
            synchronized (notify) {
                finished = true;
                notify.notifyAll();
            }
        }
    }
}
