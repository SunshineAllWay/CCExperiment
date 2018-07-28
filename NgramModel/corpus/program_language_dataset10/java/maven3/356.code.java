package org.apache.maven.lifecycle;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
public class LifecycleExecutionException
    extends Exception
{
    private MavenProject project;
    public LifecycleExecutionException( String message )
    {
        super( message );
    }
    public LifecycleExecutionException( Throwable cause )
    {
        super( cause );
    }
    public LifecycleExecutionException( String message, Throwable cause )
    {
        super( message, cause );
    }
    public LifecycleExecutionException( String message, MavenProject project )
    {
        super( message );
        this.project = project;
    }
    public LifecycleExecutionException( String message, MojoExecution execution, MavenProject project )
    {
        super( message );
        this.project = project;
    }
    public LifecycleExecutionException( String message, MojoExecution execution, MavenProject project, Throwable cause )
    {
        super( message, cause );
        this.project = project;
    }
    public LifecycleExecutionException( MojoExecution execution, MavenProject project, Throwable cause )
    {
        this( createMessage( execution, project, cause ), execution, project, cause );
    }
    public MavenProject getProject()
    {
        return project;
    }
    private static String createMessage( MojoExecution execution, MavenProject project, Throwable cause )
    {
        StringBuilder buffer = new StringBuilder( 256 );
        buffer.append( "Failed to execute goal" );
        if ( execution != null )
        {
            buffer.append( ' ' );
            buffer.append( execution.getGroupId() );
            buffer.append( ':' );
            buffer.append( execution.getArtifactId() );
            buffer.append( ':' );
            buffer.append( execution.getVersion() );
            buffer.append( ':' );
            buffer.append( execution.getGoal() );
            buffer.append( " (" );
            buffer.append( execution.getExecutionId() );
            buffer.append( ")" );
        }
        if ( project != null )
        {
            buffer.append( " on project " );
            buffer.append( project.getArtifactId() );
        }
        if ( cause != null )
        {
            buffer.append( ": " ).append( cause.getMessage() );
        }
        return buffer.toString();
    }
}
