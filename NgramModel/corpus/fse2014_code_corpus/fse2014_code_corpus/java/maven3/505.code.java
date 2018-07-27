package org.apache.maven.project.artifact;
import java.io.File;
import java.io.IOException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.AbstractArtifactMetadata;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataStoreException;
import org.codehaus.plexus.util.FileUtils;
public class ProjectArtifactMetadata
    extends AbstractArtifactMetadata
{
    private final File file;
    public ProjectArtifactMetadata( Artifact artifact )
    {
        this( artifact, null );
    }
    public ProjectArtifactMetadata( Artifact artifact, File file )
    {
        super( artifact );
        this.file = file;
    }
    public File getFile()
    {
        return file;
    }
    public String getRemoteFilename()
    {
        return getFilename();
    }
    public String getLocalFilename( ArtifactRepository repository )
    {
        return getFilename();
    }
    private String getFilename()
    {
        return getArtifactId() + "-" + artifact.getVersion() + ".pom";
    }
    public void storeInLocalRepository( ArtifactRepository localRepository, ArtifactRepository remoteRepository )
        throws RepositoryMetadataStoreException
    {
        File destination =
            new File( localRepository.getBasedir(), localRepository.pathOfLocalRepositoryMetadata( this,
                                                                                                   remoteRepository ) );
        try
        {
            FileUtils.copyFile( file, destination );
        }
        catch ( IOException e )
        {
            throw new RepositoryMetadataStoreException( "Error copying POM to the local repository.", e );
        }
    }
    public String toString()
    {
        return "project information for " + artifact.getArtifactId() + " " + artifact.getVersion();
    }
    public boolean storedInArtifactVersionDirectory()
    {
        return true;
    }
    public String getBaseVersion()
    {
        return artifact.getBaseVersion();
    }
    public Object getKey()
    {
        return "project " + artifact.getGroupId() + ":" + artifact.getArtifactId();
    }
    public void merge( ArtifactMetadata metadata )
    {
        ProjectArtifactMetadata m = (ProjectArtifactMetadata) metadata;
        if ( !m.file.equals( file ) )
        {
            throw new IllegalStateException( "Cannot add two different pieces of metadata for: " + getKey() );
        }
    }
    public void merge( org.apache.maven.repository.legacy.metadata.ArtifactMetadata metadata )
    {
        this.merge( (ArtifactMetadata) metadata );
    }
}
