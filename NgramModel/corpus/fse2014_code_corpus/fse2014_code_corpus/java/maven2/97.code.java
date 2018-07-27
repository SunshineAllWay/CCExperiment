package org.apache.maven.artifact.transform;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import java.util.List;
public class ReleaseArtifactTransformation
    extends AbstractVersionTransformation
{
    public void transformForResolve( Artifact artifact, List<ArtifactRepository> remoteRepositories, ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        if ( Artifact.RELEASE_VERSION.equals( artifact.getVersion() ) )
        {
            try
            {
                String version = resolveVersion( artifact, localRepository, remoteRepositories );
                if ( Artifact.RELEASE_VERSION.equals( version ) )
                {
                    throw new ArtifactNotFoundException( "Unable to determine the release version", artifact );
                }
                artifact.setBaseVersion( version );
                artifact.updateVersion( version, localRepository );
            }
            catch ( RepositoryMetadataResolutionException e )
            {
                throw new ArtifactResolutionException( e.getMessage(), artifact, e );
            }
        }
    }
    public void transformForInstall( Artifact artifact, ArtifactRepository localRepository )
    {
        ArtifactMetadata metadata = createMetadata( artifact );
        artifact.addMetadata( metadata );
    }
    public void transformForDeployment( Artifact artifact, ArtifactRepository remoteRepository,
                                        ArtifactRepository localRepository )
    {
        ArtifactMetadata metadata = createMetadata( artifact );
        artifact.addMetadata( metadata );
    }
    private ArtifactMetadata createMetadata( Artifact artifact )
    {
        Versioning versioning = new Versioning();
        versioning.updateTimestamp();
        versioning.addVersion( artifact.getVersion() );
        if ( artifact.isRelease() )
        {
            versioning.setRelease( artifact.getVersion() );
        }
        return new ArtifactRepositoryMetadata( artifact, versioning );
    }
    protected String constructVersion( Versioning versioning, String baseVersion )
    {
        return versioning.getRelease();
    }
}
