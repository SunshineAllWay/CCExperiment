package org.apache.maven.repository;
public class ArtifactDoesNotExistException
    extends Exception
{
    public ArtifactDoesNotExistException( final String message )
    {
        super( message );
    }
    public ArtifactDoesNotExistException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
