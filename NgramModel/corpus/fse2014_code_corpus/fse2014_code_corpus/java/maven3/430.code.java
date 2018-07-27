package org.apache.maven.plugin;
import org.apache.maven.project.DuplicateArtifactAttachmentException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
public class PluginExecutionException
    extends PluginManagerException
{
    private final MojoExecution mojoExecution;
    public PluginExecutionException( MojoExecution mojoExecution, MavenProject project, String message )
    {
        super( mojoExecution.getMojoDescriptor(), project, message );
        this.mojoExecution = mojoExecution;
    }
    public PluginExecutionException( MojoExecution mojoExecution, MavenProject project, String message, Throwable cause )
    {
        super( mojoExecution.getMojoDescriptor(), project, message, cause );
        this.mojoExecution = mojoExecution;
    }
    public PluginExecutionException( MojoExecution mojoExecution, MavenProject project, Exception cause )
    {
        super( mojoExecution.getMojoDescriptor(), project, constructMessage( mojoExecution, cause ), cause );
        this.mojoExecution = mojoExecution;
    }
    public PluginExecutionException( MojoExecution mojoExecution, MavenProject project,
                                     DuplicateArtifactAttachmentException cause )
    {
        super( mojoExecution.getMojoDescriptor(), project, constructMessage( mojoExecution, cause ), cause );
        this.mojoExecution = mojoExecution;
    }
    public MojoExecution getMojoExecution()
    {
        return mojoExecution;
    }
    private static String constructMessage( MojoExecution mojoExecution, Throwable cause )
    {
        String message;
        if ( mojoExecution != null )
        {
            message =
                "Execution " + mojoExecution.getExecutionId() + " of goal " + mojoExecution.getMojoDescriptor().getId()
                    + " failed";
        }
        else
        {
            message = "Mojo execution failed";
        }
        if ( cause != null && StringUtils.isNotEmpty( cause.getMessage() ) )
        {
            message += ": " + cause.getMessage();
        }
        else
        {
            message += ".";
        }
        return message;
    }
}
