package org.apache.maven.execution;
import org.apache.maven.project.MavenProject;
public abstract class BuildSummary
{
    private final MavenProject project;
    private final long time;
    protected BuildSummary( MavenProject project, long time )
    {
        if ( project == null )
        {
            throw new IllegalArgumentException( "project missing" );
        }
        this.project = project;
        this.time = time;
    }
    public MavenProject getProject()
    {
        return project;
    }
    public long getTime()
    {
        return time;
    }
}
