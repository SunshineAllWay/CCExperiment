package org.apache.maven.artifact.resolver.filter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
public class OrArtifactFilter
    implements ArtifactFilter
{
    private Set<ArtifactFilter> filters;
    public OrArtifactFilter()
    {
        this.filters = new LinkedHashSet<ArtifactFilter>();
    }
    public OrArtifactFilter( Collection<ArtifactFilter> filters )
    {
        this.filters = new LinkedHashSet<ArtifactFilter>( filters );
    }
    public boolean include( Artifact artifact )
    {
        for ( ArtifactFilter filter : filters )
        {
            if ( filter.include( artifact ) )
            {
                return true;
            }
        }
        return false;
    }
    public void add( ArtifactFilter artifactFilter )
    {
        filters.add( artifactFilter );
    }
    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + filters.hashCode();
        return hash;
    }
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( !( obj instanceof OrArtifactFilter ) )
        {
            return false;
        }
        OrArtifactFilter other = (OrArtifactFilter) obj;
        return filters.equals( other.filters );
    }
}
