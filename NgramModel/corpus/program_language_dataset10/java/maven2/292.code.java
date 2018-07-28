package org.apache.maven.project.artifact;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.project.MavenProject;
import java.io.File;
import java.util.Collection;
import java.util.List;
public class ActiveProjectArtifact
    implements Artifact
{
    private final Artifact artifact;
    private final MavenProject project;
    public ActiveProjectArtifact( MavenProject project, Artifact artifact )
    {
        this.artifact = artifact;
        this.project = project;
        artifact.setFile( project.getArtifact().getFile() );
        artifact.setResolved( true );
    }
    public File getFile()
    {
        return project.getArtifact().getFile();
    }
    public String getGroupId()
    {
        return artifact.getGroupId();
    }
    public String getArtifactId()
    {
        return artifact.getArtifactId();
    }
    public String getVersion()
    {
        return artifact.getVersion();
    }
    public void setVersion( String version )
    {
        artifact.setVersion( version );
    }
    public String getScope()
    {
        return artifact.getScope();
    }
    public String getType()
    {
        return artifact.getType();
    }
    public String getClassifier()
    {
        return artifact.getClassifier();
    }
    public boolean hasClassifier()
    {
        return artifact.hasClassifier();
    }
    public void setFile( File destination )
    {
        artifact.setFile( destination );
        project.getArtifact().setFile( destination );
    }
    public String getBaseVersion()
    {
        return artifact.getBaseVersion();
    }
    public void setBaseVersion( String baseVersion )
    {
        artifact.setBaseVersion( baseVersion );
    }
    public String getId()
    {
        return artifact.getId();
    }
    public String getDependencyConflictId()
    {
        return artifact.getDependencyConflictId();
    }
    public void addMetadata( ArtifactMetadata metadata )
    {
        artifact.addMetadata( metadata );
    }
    public Collection<ArtifactMetadata> getMetadataList()
    {
        return artifact.getMetadataList();
    }
    public void setRepository( ArtifactRepository remoteRepository )
    {
        artifact.setRepository( remoteRepository );
    }
    public ArtifactRepository getRepository()
    {
        return artifact.getRepository();
    }
    public void updateVersion( String version, ArtifactRepository localRepository )
    {
        artifact.updateVersion( version, localRepository );
    }
    public String getDownloadUrl()
    {
        return artifact.getDownloadUrl();
    }
    public void setDownloadUrl( String downloadUrl )
    {
        artifact.setDownloadUrl( downloadUrl );
    }
    public ArtifactFilter getDependencyFilter()
    {
        return artifact.getDependencyFilter();
    }
    public void setDependencyFilter( ArtifactFilter artifactFilter )
    {
        artifact.setDependencyFilter( artifactFilter );
    }
    public ArtifactHandler getArtifactHandler()
    {
        return artifact.getArtifactHandler();
    }
    public List<String> getDependencyTrail()
    {
        return artifact.getDependencyTrail();
    }
    public void setDependencyTrail( List<String> dependencyTrail )
    {
        artifact.setDependencyTrail( dependencyTrail );
    }
    public void setScope( String scope )
    {
        artifact.setScope( scope );
    }
    public VersionRange getVersionRange()
    {
        return artifact.getVersionRange();
    }
    public void setVersionRange( VersionRange newRange )
    {
        artifact.setVersionRange( newRange );
    }
    public void selectVersion( String version )
    {
        artifact.selectVersion( version );
    }
    public void setGroupId( String groupId )
    {
        artifact.setGroupId( groupId );
    }
    public void setArtifactId( String artifactId )
    {
        artifact.setArtifactId( artifactId );
    }
    public boolean isSnapshot()
    {
        return artifact.isSnapshot();
    }
    public int compareTo( Artifact a )
    {
        return artifact.compareTo( a );
    }
    public void setResolved( boolean resolved )
    {
        artifact.setResolved( resolved );
    }
    public boolean isResolved()
    {
        return artifact.isResolved();
    }
    public void setResolvedVersion( String version )
    {
        artifact.setResolvedVersion( version );
    }
    public void setArtifactHandler( ArtifactHandler handler )
    {
        artifact.setArtifactHandler( handler );
    }
    public String toString()
    {
        return "active project artifact:\n\tartifact = " + artifact + ";\n\tproject: " + project;
    }
    public boolean isRelease()
    {
        return artifact.isRelease();
    }
    public void setRelease( boolean release )
    {
        artifact.setRelease( release );
    }
    public List<ArtifactVersion> getAvailableVersions()
    {
        return artifact.getAvailableVersions();
    }
    public void setAvailableVersions( List<ArtifactVersion> versions )
    {
        artifact.setAvailableVersions( versions );
    }
    public boolean isOptional()
    {
        return artifact.isOptional();
    }
    public ArtifactVersion getSelectedVersion()
        throws OverConstrainedVersionException
    {
        return artifact.getSelectedVersion();
    }
    public boolean isSelectedVersionKnown()
        throws OverConstrainedVersionException
    {
        return artifact.isSelectedVersionKnown();
    }
    public void setOptional( boolean optional )
    {
        artifact.setOptional( optional );
    }
    public int hashCode()
    {
        int result = 17;
        result = 37 * result + getGroupId().hashCode();
        result = 37 * result + getArtifactId().hashCode();
        result = 37 * result + getType().hashCode();
        if ( getVersion() != null )
        {
            result = 37 * result + getVersion().hashCode();
        }
        result = 37 * result + ( getClassifier() != null ? getClassifier().hashCode() : 0 );
        return result;
    }
    public boolean equals( Object o )
    {
        if ( o == this )
        {
            return true;
        }
        if ( !( o instanceof Artifact ) )
        {
            return false;
        }
        Artifact a = (Artifact) o;
        if ( !a.getGroupId().equals( getGroupId() ) )
        {
            return false;
        }
        else if ( !a.getArtifactId().equals( getArtifactId() ) )
        {
            return false;
        }
        else if ( !a.getVersion().equals( getVersion() ) )
        {
            return false;
        }
        else if ( !a.getType().equals( getType() ) )
        {
            return false;
        }
        else if ( a.getClassifier() == null ? getClassifier() != null : !a.getClassifier().equals( getClassifier() ) )
        {
            return false;
        }
        return true;
    }
    public ArtifactMetadata getMetadata( Class<?> metadataClass )
    {
        return artifact.getMetadata( metadataClass );
    }
}
