package org.apache.maven.repository;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.transfer.ArtifactTransferException;
public class TestRepositoryConnector
    implements RepositoryConnector
{
    private RemoteRepository repository;
    private File basedir;
    public TestRepositoryConnector( RemoteRepository repository )
    {
        this.repository = repository;
        try
        {
            basedir = FileUtils.toFile( new URL( repository.getUrl() ) );
        }
        catch ( MalformedURLException e )
        {
            throw new IllegalStateException( e );
        }
    }
    public void close()
    {
    }
    public void get( Collection<? extends ArtifactDownload> artifactDownloads,
                     Collection<? extends MetadataDownload> metadataDownloads )
    {
        if ( artifactDownloads != null )
        {
            for ( ArtifactDownload download : artifactDownloads )
            {
                File remoteFile = new File( basedir, path( download.getArtifact() ) );
                try
                {
                    FileUtils.copyFile( remoteFile, download.getFile() );
                }
                catch ( IOException e )
                {
                    download.setException( new ArtifactTransferException( download.getArtifact(), repository, e ) );
                }
            }
        }
    }
    private String path( Artifact artifact )
    {
        StringBuilder path = new StringBuilder( 128 );
        path.append( artifact.getGroupId().replace( '.', '/' ) ).append( '/' );
        path.append( artifact.getArtifactId() ).append( '/' );
        path.append( artifact.getBaseVersion() ).append( '/' );
        path.append( artifact.getArtifactId() ).append( '-' ).append( artifact.getVersion() );
        if ( artifact.getClassifier().length() > 0 )
        {
            path.append( '-' ).append( artifact.getClassifier() );
        }
        path.append( '.' ).append( artifact.getExtension() );
        return path.toString();
    }
    public void put( Collection<? extends ArtifactUpload> artifactUploads,
                     Collection<? extends MetadataUpload> metadataUploads )
    {
    }
}
