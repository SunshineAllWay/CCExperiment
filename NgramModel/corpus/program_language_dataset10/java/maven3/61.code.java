package org.apache.maven.artifact.repository;
import org.apache.maven.artifact.UnknownRepositoryLayoutException;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
public interface ArtifactRepositoryFactory
{
    String ROLE = ArtifactRepositoryFactory.class.getName();
    String DEFAULT_LAYOUT_ID = "default";
    String LOCAL_REPOSITORY_ID = "local";
    @Deprecated
    ArtifactRepositoryLayout getLayout( String layoutId )
        throws UnknownRepositoryLayoutException;
    @Deprecated
    ArtifactRepository createDeploymentArtifactRepository( String id, String url, String layoutId, boolean uniqueVersion )
        throws UnknownRepositoryLayoutException;
    ArtifactRepository createDeploymentArtifactRepository( String id, String url, ArtifactRepositoryLayout layout,
                                                           boolean uniqueVersion );
    ArtifactRepository createArtifactRepository( String id, String url, String layoutId,
                                                 ArtifactRepositoryPolicy snapshots, ArtifactRepositoryPolicy releases )
        throws UnknownRepositoryLayoutException;
    ArtifactRepository createArtifactRepository( String id, String url, ArtifactRepositoryLayout repositoryLayout,
                                                 ArtifactRepositoryPolicy snapshots, ArtifactRepositoryPolicy releases );
    void setGlobalUpdatePolicy( String snapshotPolicy );
    void setGlobalChecksumPolicy( String checksumPolicy );
}
