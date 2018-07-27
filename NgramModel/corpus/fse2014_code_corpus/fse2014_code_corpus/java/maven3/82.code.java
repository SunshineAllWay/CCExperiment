package org.apache.maven.artifact.resolver.filter;
import org.apache.maven.artifact.Artifact;
public class TypeArtifactFilter
    implements ArtifactFilter
{
    private String type = "jar";
    public TypeArtifactFilter( String type )
    {
        this.type = type;
    }
    public boolean include( Artifact artifact )
    {
        return type.equals( artifact.getType() );
    }
    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + type.hashCode();
        return hash;
    }
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( !( obj instanceof TypeArtifactFilter ) )
        {
            return false;
        }
        TypeArtifactFilter other = (TypeArtifactFilter) obj;
        return type.equals( other.type );
    }
}
