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
    private File originalFile;
    private File transformedFile;
    private boolean versionExpressionsResolved = false;
    public ProjectArtifactMetadata( Artifact artifact )
    {
        this( artifact, null );
    }
    public ProjectArtifactMetadata( Artifact artifact,
                                    File file )
    {
        super( artifact );
        this.originalFile = file;
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
    public void storeInLocalRepository( ArtifactRepository localRepository,
                                        ArtifactRepository remoteRepository )
        throws RepositoryMetadataStoreException
    {
        File f = transformedFile == null ? originalFile : transformedFile;
        if ( f == null )
        {
            return;
        }
        File destination = new File( localRepository.getBasedir(),
                                     localRepository.pathOfLocalRepositoryMetadata( this, remoteRepository ) );
        try
        {
            FileUtils.copyFile( f, destination );
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
        if ( !m.originalFile.equals( originalFile ) )
        {
            throw new IllegalStateException( "Cannot add two different pieces of metadata for: " + getKey() );
        }
    }
    public boolean isVersionExpressionsResolved()
    {
        return versionExpressionsResolved;
    }
    public void setVersionExpressionsResolved( boolean versionExpressionsResolved )
    {
        this.versionExpressionsResolved = versionExpressionsResolved;
    }
    public void setFile( File file )
    {
        this.transformedFile = file;
    }
    public File getFile()
    {
        return transformedFile == null ? originalFile : transformedFile;
    }
}
