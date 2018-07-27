package org.apache.maven.repository.legacy.metadata;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.ArtifactVersion;
public interface ArtifactMetadataSource
{
    ResolutionGroup retrieve( MetadataResolutionRequest request )
        throws ArtifactMetadataRetrievalException;
    ResolutionGroup retrieve( Artifact artifact, ArtifactRepository localRepository,
                              List<ArtifactRepository> remoteRepositories )
        throws ArtifactMetadataRetrievalException;
    List<ArtifactVersion> retrieveAvailableVersions( Artifact artifact, ArtifactRepository localRepository,
                                                     List<ArtifactRepository> remoteRepositories )
        throws ArtifactMetadataRetrievalException;
    List<ArtifactVersion> retrieveAvailableVersionsFromDeploymentRepository( Artifact artifact,
                                                                             ArtifactRepository localRepository,
                                                                             ArtifactRepository remoteRepository )
        throws ArtifactMetadataRetrievalException;
}