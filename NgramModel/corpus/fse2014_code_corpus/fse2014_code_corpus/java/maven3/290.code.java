package org.apache.maven.artifact.repository.metadata;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.Restriction;
import org.apache.maven.artifact.versioning.VersionRange;
public class ArtifactRepositoryMetadata
    extends AbstractRepositoryMetadata
{
    private Artifact artifact;
    public ArtifactRepositoryMetadata( Artifact artifact )
    {
        this( artifact, null );
    }
    public ArtifactRepositoryMetadata( Artifact artifact,
                                       Versioning versioning )
    {
        super( createMetadata( artifact, versioning ) );
        this.artifact = artifact;
    }
    public boolean storedInGroupDirectory()
    {
        return false;
    }
    public boolean storedInArtifactVersionDirectory()
    {
        return false;
    }
    public String getGroupId()
    {
        return artifact.getGroupId();
    }
    public String getArtifactId()
    {
        return artifact.getArtifactId();
    }
    public String getBaseVersion()
    {
        return null;
    }
    public Object getKey()
    {
        return "artifact " + artifact.getGroupId() + ":" + artifact.getArtifactId();
    }
    public boolean isSnapshot()
    {
        return false;
    }
    public int getNature()
    {
        if ( artifact.getVersion() != null )
        {
            return artifact.isSnapshot() ? SNAPSHOT : RELEASE;
        }
        VersionRange range = artifact.getVersionRange();
        if ( range != null )
        {
            for ( Restriction restriction : range.getRestrictions() )
            {
                if ( isSnapshot( restriction.getLowerBound() ) || isSnapshot( restriction.getUpperBound() ) )
                {
                    return RELEASE_OR_SNAPSHOT;
                }
            }
        }
        return RELEASE;
    }
    private boolean isSnapshot( ArtifactVersion version )
    {
        return version != null && ArtifactUtils.isSnapshot( version.getQualifier() );
    }
    public ArtifactRepository getRepository()
    {
        return null;
    }
    public void setRepository( ArtifactRepository remoteRepository )
    {
    }
}
