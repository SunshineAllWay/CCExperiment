package org.apache.maven.artifact.metadata;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import java.util.List;
public interface ArtifactMetadataSource
{
    String ROLE = ArtifactMetadataSource.class.getName();
    ResolutionGroup retrieve( Artifact artifact, ArtifactRepository localRepository, List remoteRepositories )
        throws ArtifactMetadataRetrievalException;
    Artifact retrieveRelocatedArtifact( Artifact artifact, ArtifactRepository localRepository, List remoteRepositories )
        throws ArtifactMetadataRetrievalException;
    List retrieveAvailableVersions( Artifact artifact, ArtifactRepository localRepository, List remoteRepositories )
        throws ArtifactMetadataRetrievalException;
}