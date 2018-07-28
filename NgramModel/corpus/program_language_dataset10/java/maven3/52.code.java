package org.apache.maven.artifact.deployer;
import java.io.File;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
public interface ArtifactDeployer
{
    String ROLE = ArtifactDeployer.class.getName();
    @Deprecated
    void deploy( String basedir, String finalName, Artifact artifact, ArtifactRepository deploymentRepository,
                 ArtifactRepository localRepository )
        throws ArtifactDeploymentException;
    void deploy( File source, Artifact artifact, ArtifactRepository deploymentRepository,
                 ArtifactRepository localRepository )
        throws ArtifactDeploymentException;
}
