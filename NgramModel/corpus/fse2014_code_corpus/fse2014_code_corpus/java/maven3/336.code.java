package org.apache.maven.execution;
import org.apache.maven.project.MavenProject;
public class BuildFailure
    extends BuildSummary
{
    private final Throwable cause;
    public BuildFailure( MavenProject project, long time, Throwable cause )
    {
        super( project, time );
        this.cause = cause;
    }
    public Throwable getCause()
    {
        return cause;
    }
}
