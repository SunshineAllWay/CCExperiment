package org.apache.maven.artifact.installer;
import java.io.File;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
public interface ArtifactInstaller
{
    String ROLE = ArtifactInstaller.class.getName();
    @Deprecated
    void install( String basedir, String finalName, Artifact artifact, ArtifactRepository localRepository )
        throws ArtifactInstallationException;
    void install( File source, Artifact artifact, ArtifactRepository localRepository )
        throws ArtifactInstallationException;
}
