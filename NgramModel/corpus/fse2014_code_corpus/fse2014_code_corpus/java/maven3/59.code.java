package org.apache.maven.artifact.manager;
@Deprecated
public class WagonConfigurationException
    extends org.apache.maven.repository.legacy.WagonConfigurationException
{
    public WagonConfigurationException( String repositoryId, String message, Throwable cause )
    {
        super( repositoryId, message, cause );
    }
    public WagonConfigurationException( String repositoryId, String message )
    {
        super( repositoryId, message );
    }
}
