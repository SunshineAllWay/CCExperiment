package org.apache.maven.lifecycle.internal;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
public interface ExecutionEventCatapult
{
    void fire( ExecutionEvent.Type eventType, MavenSession session, MojoExecution mojoExecution );
    void fire( ExecutionEvent.Type eventType, MavenSession session, MojoExecution mojoExecution, Exception exception );
}
