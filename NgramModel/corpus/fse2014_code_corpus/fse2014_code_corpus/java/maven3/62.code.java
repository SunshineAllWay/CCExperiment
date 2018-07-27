package org.apache.maven.artifact.repository;
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.repository.Proxy;
import org.apache.maven.wagon.repository.Repository;
@Deprecated
public class DefaultArtifactRepository
    extends Repository
    implements ArtifactRepository
{
    private ArtifactRepositoryLayout layout;
    private ArtifactRepositoryPolicy snapshots;
    private ArtifactRepositoryPolicy releases;
    private boolean blacklisted;
    private Authentication authentication;
    private Proxy proxy;
    public DefaultArtifactRepository( String id, String url, ArtifactRepositoryLayout layout )
    {
        this( id, url, layout, null, null );
    }
    public DefaultArtifactRepository( String id, String url, ArtifactRepositoryLayout layout, boolean uniqueVersion )
    {
        super( id, url );
        this.layout = layout;
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
    public void setLayout( ArtifactRepositoryLayout layout )
    {
        this.layout = layout;
    }
    public ArtifactRepositoryLayout getLayout()
    {
        return layout;
    }
    public void setSnapshotUpdatePolicy( ArtifactRepositoryPolicy snapshots )
    {
        this.snapshots = snapshots;
    }
    public ArtifactRepositoryPolicy getSnapshots()
    {
        return snapshots;
    }
    public void setReleaseUpdatePolicy( ArtifactRepositoryPolicy releases )
    {
        this.releases = releases;
    }
    public ArtifactRepositoryPolicy getReleases()
    {
        return releases;
    }
    public String getKey()
    {
        return getId();
    }
    public boolean isBlacklisted()
    {
        return blacklisted;
    }
    public void setBlacklisted( boolean blacklisted )
    {
        this.blacklisted = blacklisted;
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "       id: " ).append( getId() ).append( "\n" );
        sb.append( "      url: " ).append( getUrl() ).append( "\n" );
        sb.append( "   layout: " ).append( layout != null ? layout.getId() : "none" ).append( "\n" );
        if ( snapshots != null )
        {
            sb.append( "snapshots: [enabled => " ).append( snapshots.isEnabled() );
            sb.append( ", update => " ).append( snapshots.getUpdatePolicy() ).append( "]\n" );
        }
        if ( releases != null )
        {
            sb.append( " releases: [enabled => " ).append( releases.isEnabled() );
            sb.append( ", update => " ).append( releases.getUpdatePolicy() ).append( "]\n" );
        }
        return sb.toString();
    }
    public Artifact find( Artifact artifact )
    {
        File artifactFile = new File( getBasedir(), pathOf( artifact ) );
        artifact.setFile( artifactFile );
        if ( artifactFile.exists() )
        {
            artifact.setResolved( true );
        }
        return artifact;
    }
    public List<String> findVersions( Artifact artifact )
    {
        return Collections.emptyList();
    }
    public boolean isProjectAware()
    {
        return false;
    }
    public Authentication getAuthentication()
    {
        return authentication;
    }
    public void setAuthentication( Authentication authentication )
    {
        this.authentication = authentication;
    }
    public Proxy getProxy()
    {
        return proxy;
    }
    public void setProxy( Proxy proxy )
    {
        this.proxy = proxy;
    }
    public boolean isUniqueVersion()
    {
        return true;
    }
}
