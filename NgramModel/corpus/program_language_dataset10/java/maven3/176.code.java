package org.apache.maven.repository.metadata;
public class MetadataRetrievalException
    extends Exception
{
    private ArtifactMetadata artifact;
    public MetadataRetrievalException( String message )
    {
        this( message, null, null );
    }
    public MetadataRetrievalException( Throwable cause )
    {
        this( null, cause, null );
    }
    public MetadataRetrievalException( String message, Throwable cause )
    {
        this( message, cause, null );
    }
    public MetadataRetrievalException( String message, Throwable cause, ArtifactMetadata artifact )
    {
        super( message, cause );
        this.artifact = artifact;
    }
    public ArtifactMetadata getArtifactMetadata()
    {
        return artifact;
    }
}