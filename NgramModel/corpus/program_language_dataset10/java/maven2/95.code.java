package org.apache.maven.artifact.transform;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import java.util.List;
public class DefaultArtifactTransformationManager
    implements ArtifactTransformationManager, Initializable
{
    private List<ArtifactTransformation> artifactTransformations;
    public void initialize()
        throws InitializationException
    {
        ArtifactTransformation transforms[] = artifactTransformations.toArray( new ArtifactTransformation[] {} );
        for ( int x = 0; x < transforms.length; x++ )
        {
            if ( transforms[x].getClass().getName().indexOf( "Snapshot" ) != -1 )
            {
                artifactTransformations.remove( transforms[x] );
                artifactTransformations.add( transforms[x] );
            }
        }
    }
    public void transformForResolve( Artifact artifact, List<ArtifactRepository> remoteRepositories, ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        for ( ArtifactTransformation transform : artifactTransformations )
        {
            transform.transformForResolve( artifact, remoteRepositories, localRepository );
        }
    }
    public void transformForInstall( Artifact artifact, ArtifactRepository localRepository )
        throws ArtifactInstallationException
    {
        for ( ArtifactTransformation transform : artifactTransformations )
        {
            transform.transformForInstall( artifact, localRepository );
        }
    }
    public void transformForDeployment( Artifact artifact, ArtifactRepository remoteRepository,
                                        ArtifactRepository localRepository )
        throws ArtifactDeploymentException
    {
        for ( ArtifactTransformation transform : artifactTransformations )
        {
            transform.transformForDeployment( artifact, remoteRepository, localRepository );
        }
    }
}
