package org.apache.maven.artifact.metadata;
import org.apache.maven.artifact.Artifact;
@Deprecated
public abstract class AbstractArtifactMetadata
    extends org.apache.maven.repository.legacy.metadata.AbstractArtifactMetadata
    implements org.apache.maven.artifact.metadata.ArtifactMetadata
{
    protected AbstractArtifactMetadata( Artifact artifact )
    {
        super( artifact );
    }
}
