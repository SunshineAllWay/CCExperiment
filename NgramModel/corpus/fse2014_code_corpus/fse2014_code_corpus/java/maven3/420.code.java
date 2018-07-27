package org.apache.maven.plugin;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
public class InvalidPluginException
    extends Exception
{
    public InvalidPluginException( String message, ProjectBuildingException e )
    {
        super( message, e );
    }
    public InvalidPluginException( String message, InvalidDependencyVersionException e )
    {
        super( message, e );
    }
    public InvalidPluginException( String message )
    {
        super( message );
    }
}
