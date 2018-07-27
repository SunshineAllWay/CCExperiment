package org.apache.maven;
import org.apache.maven.plugin.MojoFailureException;
public class ProjectBuildFailureException
    extends BuildFailureException
{
    private final String projectId;
    public ProjectBuildFailureException( String projectId, MojoFailureException cause )
    {
        super( "Build for project: " + projectId + " failed during execution of mojo.", cause );
        this.projectId = projectId;
    }
    public MojoFailureException getMojoFailureException()
    {
        return (MojoFailureException) getCause();
    }
    public String getProjectId()
    {
        return projectId;
    }
}
