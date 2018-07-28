package org.apache.maven.artifact.deployer;
public class ArtifactDeploymentException
    extends Exception
{
    public ArtifactDeploymentException( String message )
    {
        super( message );
    }
    public ArtifactDeploymentException( Throwable cause )
    {
        super( cause );
    }
    public ArtifactDeploymentException( String message,
                                        Throwable cause )
    {
        super( message, cause );
    }
}
