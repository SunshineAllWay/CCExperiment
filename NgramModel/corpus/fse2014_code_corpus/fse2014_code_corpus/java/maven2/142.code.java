package org.apache.maven.lifecycle;
import java.util.List;
import org.apache.maven.BuildFailureException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ReactorManager;
import org.apache.maven.monitor.event.EventDispatcher;
public interface LifecycleExecutor
{
    String ROLE = LifecycleExecutor.class.getName();
    void execute( MavenSession session, ReactorManager rm, EventDispatcher dispatcher )
        throws LifecycleExecutionException, BuildFailureException;
    List getLifecycles();
}
