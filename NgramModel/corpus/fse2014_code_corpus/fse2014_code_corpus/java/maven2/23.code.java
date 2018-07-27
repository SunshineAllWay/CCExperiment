package org.apache.maven.artifact.repository;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
public interface ArtifactRepository
{
    String pathOf( Artifact artifact );
    String pathOfRemoteRepositoryMetadata( ArtifactMetadata artifactMetadata );
    String pathOfLocalRepositoryMetadata( ArtifactMetadata metadata, ArtifactRepository repository );
    String getUrl();
    String getBasedir();
    String getProtocol();
    String getId();
    ArtifactRepositoryPolicy getSnapshots();
    ArtifactRepositoryPolicy getReleases();
    ArtifactRepositoryLayout getLayout();
    String getKey();
    boolean isUniqueVersion();
    void setBlacklisted( boolean blackListed );
    boolean isBlacklisted();
}
