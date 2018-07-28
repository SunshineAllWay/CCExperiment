package org.apache.maven.artifact.repository;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.wagon.repository.Repository;
public class DefaultArtifactRepository
    extends Repository
    implements ArtifactRepository
{
    private final ArtifactRepositoryLayout layout;
    private ArtifactRepositoryPolicy snapshots;
    private ArtifactRepositoryPolicy releases;
    private boolean uniqueVersion;
    private boolean blacklisted;
    public DefaultArtifactRepository( String id, String url, ArtifactRepositoryLayout layout )
    {
        this( id, url, layout, null, null );
    }
    public DefaultArtifactRepository( String id, String url, ArtifactRepositoryLayout layout, boolean uniqueVersion )
    {
        super( id, url );
        this.layout = layout;
        this.uniqueVersion = uniqueVersion;
    }
    public DefaultArtifactRepository( String id, String url, ArtifactRepositoryLayout layout,
                                      ArtifactRepositoryPolicy snapshots, ArtifactRepositoryPolicy releases )
    {
        super( id, url );
        this.layout = layout;
        if ( snapshots == null )
        {
            snapshots = new ArtifactRepositoryPolicy( true, ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS,
                                                      ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE );
        }
        this.snapshots = snapshots;
        if ( releases == null )
        {
            releases = new ArtifactRepositoryPolicy( true, ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS,
                                                     ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE );
        }
        this.releases = releases;
    }
    public String pathOf( Artifact artifact )
    {
        return layout.pathOf( artifact );
    }
    public String pathOfRemoteRepositoryMetadata( ArtifactMetadata artifactMetadata )
    {
        return layout.pathOfRemoteRepositoryMetadata( artifactMetadata );
    }
    public String pathOfLocalRepositoryMetadata( ArtifactMetadata metadata, ArtifactRepository repository )
    {
        return layout.pathOfLocalRepositoryMetadata( metadata, repository );
    }
    public ArtifactRepositoryLayout getLayout()
    {
        return layout;
    }
    public ArtifactRepositoryPolicy getSnapshots()
    {
        return snapshots;
    }
    public ArtifactRepositoryPolicy getReleases()
    {
        return releases;
    }
    public String getKey()
    {
        return getId();
    }
    public boolean isUniqueVersion()
    {
        return uniqueVersion;
    }
    public boolean isBlacklisted()
    {
        return blacklisted;
    }
    public void setBlacklisted( boolean blacklisted )
    {
        this.blacklisted = blacklisted;
    }
}
