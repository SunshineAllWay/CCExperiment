package org.apache.maven.artifact.repository.metadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
public interface RepositoryMetadata
    extends org.apache.maven.artifact.metadata.ArtifactMetadata
{
    int RELEASE = 1;
    int SNAPSHOT = 2;
    int RELEASE_OR_SNAPSHOT = RELEASE | SNAPSHOT;
    ArtifactRepository getRepository();
    void setRepository( ArtifactRepository remoteRepository );
    Metadata getMetadata();
    void setMetadata( Metadata metadata );
    boolean isSnapshot();
    int getNature();
    ArtifactRepositoryPolicy getPolicy( ArtifactRepository repository );
}
