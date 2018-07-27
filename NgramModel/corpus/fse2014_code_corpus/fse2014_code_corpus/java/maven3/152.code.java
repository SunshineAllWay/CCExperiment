package org.apache.maven.repository.legacy.resolver.transform;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.RepositoryRequest;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
public interface ArtifactTransformation
{
    String ROLE = ArtifactTransformation.class.getName();
    void transformForResolve( Artifact artifact, RepositoryRequest request )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    void transformForResolve( Artifact artifact,
                              List<ArtifactRepository> remoteRepositories,
                              ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    void transformForInstall( Artifact artifact,
                              ArtifactRepository localRepository )
        throws ArtifactInstallationException;
    void transformForDeployment( Artifact artifact,
                                 ArtifactRepository remoteRepository,
                                 ArtifactRepository localRepository )
        throws ArtifactDeploymentException;
}
