package org.apache.maven.artifact.repository.layout;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
public interface ArtifactRepositoryLayout2
    extends ArtifactRepositoryLayout
{
    ArtifactRepository newMavenArtifactRepository( String id, String url, ArtifactRepositoryPolicy snapshots,
                                                   ArtifactRepositoryPolicy releases );
}
