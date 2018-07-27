package org.apache.maven.artifact.transform;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import java.util.List;
public interface ArtifactTransformation
{
    String ROLE = ArtifactTransformation.class.getName();
    void transformForResolve( Artifact artifact, List<ArtifactRepository> remoteRepositories, ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    void transformForInstall( Artifact artifact, ArtifactRepository localRepository )
        throws ArtifactInstallationException;
    void transformForDeployment( Artifact artifact, ArtifactRepository remoteRepository,
                                 ArtifactRepository localRepository )
        throws ArtifactDeploymentException;
}
