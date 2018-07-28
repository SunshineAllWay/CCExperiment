package org.apache.maven.artifact.repository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
public interface ArtifactRepositoryFactory
{
    String ROLE = ArtifactRepositoryFactory.class.getName();
    ArtifactRepository createDeploymentArtifactRepository( String id, String url, ArtifactRepositoryLayout layout,
                                                           boolean uniqueVersion );
    ArtifactRepository createArtifactRepository( String id, String url, ArtifactRepositoryLayout repositoryLayout,
                                                 ArtifactRepositoryPolicy snapshots,
                                                 ArtifactRepositoryPolicy releases );
    void setGlobalUpdatePolicy( String snapshotPolicy );
    void setGlobalChecksumPolicy( String checksumPolicy );
}
