package org.apache.maven.repository;
public class ArtifactTransferFailedException
    extends Exception
{
    public ArtifactTransferFailedException( final String message )
    {
        super( message );
    }
    public ArtifactTransferFailedException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
