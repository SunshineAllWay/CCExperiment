package org.apache.maven.artifact.repository.layout;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
public class DefaultRepositoryLayout
    implements ArtifactRepositoryLayout
{
    private static final char PATH_SEPARATOR = '/';
    private static final char GROUP_SEPARATOR = '.';
    private static final char ARTIFACT_SEPARATOR = '-';
    public String pathOf( Artifact artifact )
    {
        ArtifactHandler artifactHandler = artifact.getArtifactHandler();
        StringBuffer path = new StringBuffer();
        path.append( formatAsDirectory( artifact.getGroupId() ) ).append( PATH_SEPARATOR );
        path.append( artifact.getArtifactId() ).append( PATH_SEPARATOR );
        path.append( artifact.getBaseVersion() ).append( PATH_SEPARATOR );
        path.append( artifact.getArtifactId() ).append( ARTIFACT_SEPARATOR ).append( artifact.getVersion() );
        if ( artifact.hasClassifier() )
        {
            path.append( ARTIFACT_SEPARATOR ).append( artifact.getClassifier() );
        }
        if ( artifactHandler.getExtension() != null && artifactHandler.getExtension().length() > 0 )
        {
            path.append( GROUP_SEPARATOR ).append( artifactHandler.getExtension() );
        }
        return path.toString();
    }
    public String pathOfLocalRepositoryMetadata( ArtifactMetadata metadata, ArtifactRepository repository )
    {
        return pathOfRepositoryMetadata( metadata, metadata.getLocalFilename( repository ) );
    }
    private String pathOfRepositoryMetadata( ArtifactMetadata metadata, String filename )
    {
        StringBuffer path = new StringBuffer();
        path.append( formatAsDirectory( metadata.getGroupId() ) ).append( PATH_SEPARATOR );
        if ( !metadata.storedInGroupDirectory() )
        {
            path.append( metadata.getArtifactId() ).append( PATH_SEPARATOR );
            if ( metadata.storedInArtifactVersionDirectory() )
            {
                path.append( metadata.getBaseVersion() ).append( PATH_SEPARATOR );
            }
        }
        path.append( filename );
        return path.toString();
    }
    public String pathOfRemoteRepositoryMetadata( ArtifactMetadata metadata )
    {
        return pathOfRepositoryMetadata( metadata, metadata.getRemoteFilename() );
    }
    private String formatAsDirectory( String directory )
    {
        return directory.replace( GROUP_SEPARATOR, PATH_SEPARATOR );
    }
}
