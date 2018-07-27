package org.apache.maven.artifact.repository.metadata;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import java.util.List;
public interface RepositoryMetadataManager
{
    void resolve( RepositoryMetadata repositoryMetadata, List repositories, ArtifactRepository localRepository )
        throws RepositoryMetadataResolutionException;
    void resolveAlways( RepositoryMetadata metadata, ArtifactRepository localRepository,
                        ArtifactRepository remoteRepository )
        throws RepositoryMetadataResolutionException;
    void deploy( ArtifactMetadata metadata, ArtifactRepository localRepository,
                 ArtifactRepository deploymentRepository )
        throws RepositoryMetadataDeploymentException;
    void install( ArtifactMetadata metadata, ArtifactRepository localRepository )
        throws RepositoryMetadataInstallationException;
}
