package org.apache.maven.repository.legacy.resolver.transform;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.RepositoryRequest;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.codehaus.plexus.component.annotations.Component;
@Component( role = ArtifactTransformation.class, hint = "latest" )
public class LatestArtifactTransformation
    extends AbstractVersionTransformation
{
    public void transformForResolve( Artifact artifact, RepositoryRequest request )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        if ( Artifact.LATEST_VERSION.equals( artifact.getVersion() ) )
        {
            try
            {
                String version = resolveVersion( artifact, request );
                if ( Artifact.LATEST_VERSION.equals( version ) )
                {
                    throw new ArtifactNotFoundException( "Unable to determine the latest version", artifact );
                }
                artifact.setBaseVersion( version );
                artifact.updateVersion( version, request.getLocalRepository() );
            }
            catch ( RepositoryMetadataResolutionException e )
            {
                throw new ArtifactResolutionException( e.getMessage(), artifact, e );
            }
        }
    }
    public void transformForInstall( Artifact artifact, ArtifactRepository localRepository )
    {
    }
    public void transformForDeployment( Artifact artifact, ArtifactRepository remoteRepository,
                                        ArtifactRepository localRepository )
    {
    }
    protected String constructVersion( Versioning versioning, String baseVersion )
    {
        return versioning.getLatest();
    }
}
