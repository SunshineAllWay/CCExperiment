package org.apache.maven.lifecycle.internal;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
class DefaultExecutionEvent
    implements ExecutionEvent
{
    private final Type type;
    private final MavenSession session;
    private final MojoExecution mojoExecution;
    private final Exception exception;
    public DefaultExecutionEvent( Type type, MavenSession session, MojoExecution mojoExecution, Exception exception )
    {
        this.type = type;
        this.session = session;
        this.mojoExecution = mojoExecution;
        this.exception = exception;
    }
    public Type getType()
    {
        return type;
    }
    public MavenSession getSession()
    {
        return session;
    }
    public MavenProject getProject()
    {
        return session.getCurrentProject();
    }
    public MojoExecution getMojoExecution()
    {
        return mojoExecution;
    }
    public Exception getException()
    {
        return exception;
    }
}
