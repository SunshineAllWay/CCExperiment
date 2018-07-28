package org.apache.maven.repository.legacy.resolver.transform;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultRepositoryRequest;
import org.apache.maven.artifact.repository.RepositoryRequest;
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException;
import org.apache.maven.artifact.repository.metadata.SnapshotArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.repository.legacy.WagonManager;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
public abstract class AbstractVersionTransformation
    extends AbstractLogEnabled
    implements ArtifactTransformation
{
    @Requirement
    protected RepositoryMetadataManager repositoryMetadataManager;
    @Requirement
    protected WagonManager wagonManager;
    public void transformForResolve( Artifact artifact, List<ArtifactRepository> remoteRepositories,
                                     ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        RepositoryRequest request = new DefaultRepositoryRequest();
        request.setLocalRepository( localRepository );
        request.setRemoteRepositories( remoteRepositories );
        transformForResolve( artifact, request );
    }
    protected String resolveVersion( Artifact artifact, ArtifactRepository localRepository,
                                     List<ArtifactRepository> remoteRepositories )
        throws RepositoryMetadataResolutionException
    {
        RepositoryRequest request = new DefaultRepositoryRequest();
        request.setLocalRepository( localRepository );
        request.setRemoteRepositories( remoteRepositories );
        return resolveVersion( artifact, request );
    }
    protected String resolveVersion( Artifact artifact, RepositoryRequest request )
        throws RepositoryMetadataResolutionException
    {
        RepositoryMetadata metadata;
        if ( !artifact.isSnapshot() || Artifact.LATEST_VERSION.equals( artifact.getBaseVersion() ) )
        {
            metadata = new ArtifactRepositoryMetadata( artifact );
        }
        else
        {
            metadata = new SnapshotArtifactRepositoryMetadata( artifact );
        }
        repositoryMetadataManager.resolve( metadata, request );
        artifact.addMetadata( metadata );
        Metadata repoMetadata = metadata.getMetadata();
        String version = null;
        if ( repoMetadata != null && repoMetadata.getVersioning() != null )
        {
            version = constructVersion( repoMetadata.getVersioning(), artifact.getBaseVersion() );
        }
        if ( version == null )
        {
            version = artifact.getBaseVersion();
        }
        if ( getLogger().isDebugEnabled() )
        {
            if ( !version.equals( artifact.getBaseVersion() ) )
            {
                String message = artifact.getArtifactId() + ": resolved to version " + version;
                if ( artifact.getRepository() != null )
                {
                    message += " from repository " + artifact.getRepository().getId();
                }
                else
                {
                    message += " from local repository";
                }
                getLogger().debug( message );
            }
            else
            {
                getLogger().debug( artifact.getArtifactId() + ": using locally installed snapshot" );
            }
        }
        return version;
    }
    protected abstract String constructVersion( Versioning versioning, String baseVersion );
}
