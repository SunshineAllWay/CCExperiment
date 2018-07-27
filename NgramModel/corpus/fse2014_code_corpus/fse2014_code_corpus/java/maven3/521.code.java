package org.apache.maven.repository.legacy.metadata;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.RepositoryRequest;
public interface MetadataResolutionRequest
    extends RepositoryRequest
{
    boolean isOffline();
    MetadataResolutionRequest setOffline( boolean offline );
    Artifact getArtifact();
    MetadataResolutionRequest setArtifact( Artifact artifact );
    ArtifactRepository getLocalRepository();
    MetadataResolutionRequest setLocalRepository( ArtifactRepository localRepository );
    List<ArtifactRepository> getRemoteRepositories();
    MetadataResolutionRequest setRemoteRepositories( List<ArtifactRepository> remoteRepositories );
    boolean isResolveManagedVersions();
    MetadataResolutionRequest setResolveManagedVersions( boolean resolveManagedVersions );
}
