package org.apache.maven.artifact.resolver.filter;
import org.apache.maven.artifact.Artifact;
public class InversionArtifactFilter
    implements ArtifactFilter
{
    private final ArtifactFilter toInvert;
    public InversionArtifactFilter( ArtifactFilter toInvert )
    {
        this.toInvert = toInvert;
    }
    public boolean include( Artifact artifact )
    {
        return !toInvert.include( artifact );
    }
}
