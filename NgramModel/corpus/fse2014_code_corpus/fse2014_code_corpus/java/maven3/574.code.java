package org.apache.maven.lifecycle.internal.stub;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ExecutionEvent.Type;
import org.apache.maven.lifecycle.internal.ExecutionEventCatapult;
import org.apache.maven.plugin.MojoExecution;
public class ExecutionEventCatapultStub
    implements ExecutionEventCatapult
{
    public void fire( Type eventType, MavenSession session, MojoExecution mojoExecution )
    {
    }
    public void fire( Type eventType, MavenSession session, MojoExecution mojoExecution, Exception exception )
    {
    }
}
