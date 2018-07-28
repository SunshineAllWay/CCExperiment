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
    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + toInvert.hashCode();
        return hash;
    }
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( !( obj instanceof InversionArtifactFilter ) )
        {
            return false;
        }
        InversionArtifactFilter other = (InversionArtifactFilter) obj;
        return toInvert.equals( other.toInvert );
    }
}
