package org.apache.maven.artifact.repository;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.repository.Proxy;
public interface ArtifactRepository
{
    String pathOf( Artifact artifact );
    String pathOfRemoteRepositoryMetadata( ArtifactMetadata artifactMetadata );
    String pathOfLocalRepositoryMetadata( ArtifactMetadata metadata, ArtifactRepository repository );
    String getUrl();
    void setUrl( String url );
    String getBasedir();
    String getProtocol();
    String getId();
    void setId( String id );
    ArtifactRepositoryPolicy getSnapshots();
    void setSnapshotUpdatePolicy( ArtifactRepositoryPolicy policy );
    ArtifactRepositoryPolicy getReleases();
    void setReleaseUpdatePolicy( ArtifactRepositoryPolicy policy );
    ArtifactRepositoryLayout getLayout();
    void setLayout( ArtifactRepositoryLayout layout );
    String getKey();
    @Deprecated
    boolean isUniqueVersion();
    @Deprecated
    boolean isBlacklisted();
    @Deprecated
    void setBlacklisted( boolean blackListed );
    Artifact find( Artifact artifact );
    List<String> findVersions( Artifact artifact );
    boolean isProjectAware();
    void setAuthentication( Authentication authentication );
    Authentication getAuthentication();
    void setProxy( Proxy proxy );
    Proxy getProxy();
}
