package org.apache.maven.artifact.repository.metadata;
public class RepositoryMetadataInstallationException
    extends Throwable
{
    public RepositoryMetadataInstallationException( String message )
    {
        super( message );
    }
    public RepositoryMetadataInstallationException( String message, Exception e )
    {
        super( message, e );
    }
}
