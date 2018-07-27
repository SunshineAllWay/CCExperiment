package org.apache.maven.artifact.repository.metadata;
public class RepositoryMetadataDeploymentException
    extends Throwable
{
    public RepositoryMetadataDeploymentException( String message )
    {
        super( message );
    }
    public RepositoryMetadataDeploymentException( String message, Exception e )
    {
        super( message, e );
    }
}
