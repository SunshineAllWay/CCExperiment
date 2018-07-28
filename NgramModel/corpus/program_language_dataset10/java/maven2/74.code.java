package org.apache.maven.artifact.installer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataInstallationException;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.transform.ArtifactTransformationManager;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
public class DefaultArtifactInstaller
    extends AbstractLogEnabled
    implements ArtifactInstaller
{
    private ArtifactTransformationManager transformationManager;
    private RepositoryMetadataManager repositoryMetadataManager;
    public void install( String basedir, String finalName, Artifact artifact, ArtifactRepository localRepository )
        throws ArtifactInstallationException
    {
        String extension = artifact.getArtifactHandler().getExtension();
        File source = new File( basedir, finalName + "." + extension );
        install( source, artifact, localRepository );
    }
    public void install( File source, Artifact artifact, ArtifactRepository localRepository )
        throws ArtifactInstallationException
    {
        boolean useArtifactFile = false;
        File oldArtifactFile = artifact.getFile();
        if ( "pom".equals( artifact.getType() ) )
        {
            artifact.setFile( source );
            useArtifactFile = true;
        }
        try
        {
            transformationManager.transformForInstall( artifact, localRepository );
            if ( useArtifactFile )
            {
                source = artifact.getFile();
                artifact.setFile( oldArtifactFile );
            }
            String localPath = localRepository.pathOf( artifact );
            File destination = new File( localRepository.getBasedir(), localPath );
            if ( !destination.getParentFile().exists() )
            {
                destination.getParentFile().mkdirs();
            }
            boolean copy =
                !destination.exists() || "pom".equals( artifact.getType() )
                    || source.lastModified() != destination.lastModified() || source.length() != destination.length();
            if ( copy )
            {
                getLogger().info( "Installing " + source + " to " + destination );
                FileUtils.copyFile( source, destination );
                destination.setLastModified( source.lastModified() );
            }
            else
            {
                getLogger().info( "Skipped re-installing " + source + " to " + destination + ", seems unchanged" );
            }
            if ( useArtifactFile )
            {
                artifact.setFile( destination );
            }
            for ( Iterator i = artifact.getMetadataList().iterator(); i.hasNext(); )
            {
                ArtifactMetadata metadata = (ArtifactMetadata) i.next();
                repositoryMetadataManager.install( metadata, localRepository );
            }
        }
        catch ( IOException e )
        {
            throw new ArtifactInstallationException( "Error installing artifact: " + e.getMessage(), e );
        }
        catch ( RepositoryMetadataInstallationException e )
        {
            throw new ArtifactInstallationException( "Error installing artifact's metadata: " + e.getMessage(), e );
        }
    }
}