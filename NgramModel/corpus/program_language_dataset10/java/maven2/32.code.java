package org.apache.maven.artifact.repository.metadata;
public class RepositoryMetadataResolutionException
    extends Exception
{
    public RepositoryMetadataResolutionException( String message )
    {
        super( message );
    }
    public RepositoryMetadataResolutionException( String message, Exception e )
    {
        super( message, e );
    }
}
