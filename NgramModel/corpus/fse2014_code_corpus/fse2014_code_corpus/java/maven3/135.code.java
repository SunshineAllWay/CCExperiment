package org.apache.maven.repository.legacy;
import java.io.File;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
public interface UpdateCheckManager
{
    boolean isUpdateRequired( Artifact artifact, ArtifactRepository repository );
    void touch( Artifact artifact, ArtifactRepository repository, String error );
    String getError( Artifact artifact, ArtifactRepository repository );
    boolean isUpdateRequired( RepositoryMetadata metadata, ArtifactRepository repository, File file );
    void touch( RepositoryMetadata metadata, ArtifactRepository repository, File file );
}
