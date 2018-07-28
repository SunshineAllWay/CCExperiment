package org.apache.maven.execution;
public class BuildFailure
{
    private final Exception cause;
    private final String task;
    private final long time;
    BuildFailure( Exception cause, String task, long time )
    {
        this.cause = cause;
        this.task = task;
        this.time = time;
    }
    public String getTask()
    {
        return task;
    }
    public Exception getCause()
    {
        return cause;
    }
    public long getTime()
    {
        return time;
    }
}
