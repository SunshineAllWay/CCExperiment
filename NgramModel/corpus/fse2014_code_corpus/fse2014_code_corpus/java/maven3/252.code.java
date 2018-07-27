package org.apache.maven;
import org.apache.maven.execution.MavenSession;
public abstract class AbstractMavenLifecycleParticipant
{
    public void afterProjectsRead( MavenSession session )
        throws MavenExecutionException
    {
    }
    public void afterSessionStart( MavenSession session )
        throws MavenExecutionException
    {
    }
}
