package org.apache.maven.artifact.installer;
public class ArtifactInstallationException
    extends Exception
{
    public ArtifactInstallationException( String message )
    {
        super( message );
    }
    public ArtifactInstallationException( Throwable cause )
    {
        super( cause );
    }
    public ArtifactInstallationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
