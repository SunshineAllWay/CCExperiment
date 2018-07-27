package org.apache.maven.project.artifact;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.metadata.ResolutionGroup;
public interface MavenMetadataCache
{
    ResolutionGroup get( Artifact artifact, boolean resolveManagedVersions, ArtifactRepository localRepository,
                         List<ArtifactRepository> remoteRepositories );
    void put( Artifact artifact, boolean resolveManagedVersions, ArtifactRepository localRepository,
              List<ArtifactRepository> remoteRepositories, ResolutionGroup result );
    void flush();
}
