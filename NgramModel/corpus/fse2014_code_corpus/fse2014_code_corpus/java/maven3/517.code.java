package org.apache.maven.repository.legacy.metadata;
import org.apache.maven.artifact.Artifact;
public abstract class AbstractArtifactMetadata
    implements ArtifactMetadata
{
    protected Artifact artifact;
    protected AbstractArtifactMetadata( Artifact artifact )
    {
        this.artifact = artifact;
    }
    public boolean storedInGroupDirectory()
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
    public String extendedToString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append( "\nArtifact Metadata\n--------------------------" );
        buffer.append( "\nGroupId: " ).append( getGroupId() );
        buffer.append( "\nArtifactId: " ).append( getArtifactId() );
        buffer.append( "\nMetadata Type: " ).append( getClass().getName() );
        return buffer.toString();
    }
}
