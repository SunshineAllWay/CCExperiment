package org.apache.maven.artifact.repository.metadata;
import java.io.File;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.metadata.MergeableMetadata;
public final class MetadataBridge
    implements MergeableMetadata
{
    private ArtifactMetadata metadata;
    private boolean merged;
    public MetadataBridge( ArtifactMetadata metadata )
    {
        this.metadata = metadata;
    }
    public void merge( File current, File result )
        throws RepositoryException
    {
        try
        {
            if ( current.exists() )
            {
                FileUtils.copyFile( current, result );
            }
            ArtifactRepository localRepo = new MetadataRepository( result );
            metadata.storeInLocalRepository( localRepo, localRepo );
            merged = true;
        }
        catch ( Exception e )
        {
            throw new RepositoryException( e.getMessage(), e );
        }
    }
    public boolean isMerged()
    {
        return merged;
    }
    public String getGroupId()
    {
        return emptify( metadata.getGroupId() );
    }
    public String getArtifactId()
    {
        return metadata.storedInGroupDirectory() ? "" : emptify( metadata.getArtifactId() );
    }
    public String getVersion()
    {
        return metadata.storedInArtifactVersionDirectory() ? emptify( metadata.getBaseVersion() ) : "";
    }
    public String getType()
    {
        return metadata.getRemoteFilename();
    }
    private String emptify( String string )
    {
        return ( string != null ) ? string : "";
    }
    public File getFile()
    {
        return null;
    }
    public MetadataBridge setFile( File file )
    {
        return this;
    }
    public Nature getNature()
    {
        if ( metadata instanceof RepositoryMetadata )
        {
            switch ( ( (RepositoryMetadata) metadata ).getNature() )
            {
                case RepositoryMetadata.RELEASE_OR_SNAPSHOT:
                    return Nature.RELEASE_OR_SNAPSHOT;
                case RepositoryMetadata.SNAPSHOT:
                    return Nature.SNAPSHOT;
                default:
                    return Nature.RELEASE;
            }
        }
        else
        {
            return Nature.RELEASE;
        }
    }
    @SuppressWarnings( "deprecation" )
    static class MetadataRepository
        extends DefaultArtifactRepository
    {
        private File metadataFile;
        public MetadataRepository( File metadataFile )
        {
            super( "local", "", null );
            this.metadataFile = metadataFile;
        }
        @Override
        public String getBasedir()
        {
            return metadataFile.getParent();
        }
        @Override
        public String pathOfLocalRepositoryMetadata( ArtifactMetadata metadata, ArtifactRepository repository )
        {
            return metadataFile.getName();
        }
    }
}
