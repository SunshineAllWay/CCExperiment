package org.apache.maven.artifact.repository.metadata;
import java.util.List;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.RepositoryRequest;
public interface RepositoryMetadataManager
{
    void resolve( RepositoryMetadata repositoryMetadata, RepositoryRequest repositoryRequest )
        throws RepositoryMetadataResolutionException;
    void resolve( RepositoryMetadata repositoryMetadata, List<ArtifactRepository> repositories,
                  ArtifactRepository localRepository )
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
