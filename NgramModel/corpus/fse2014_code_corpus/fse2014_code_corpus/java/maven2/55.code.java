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
}
