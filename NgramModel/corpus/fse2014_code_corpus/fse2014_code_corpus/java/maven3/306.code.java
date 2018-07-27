package org.apache.maven.artifact.resolver.filter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
public class AndArtifactFilter
    implements ArtifactFilter
{
    private Set<ArtifactFilter> filters; 
    public AndArtifactFilter()
    {
        this.filters = new LinkedHashSet<ArtifactFilter>();
    }
    public AndArtifactFilter( List<ArtifactFilter> filters )
    {
        this.filters = new LinkedHashSet<ArtifactFilter>( filters );
    }
    public boolean include( Artifact artifact )
    {
        boolean include = true;
        for ( Iterator<ArtifactFilter> i = filters.iterator(); i.hasNext() && include; )
        {
            ArtifactFilter filter = i.next();
            if ( !filter.include( artifact ) )
            {
                include = false;
            }
        }
        return include;
    }
    public void add( ArtifactFilter artifactFilter )
    {
        filters.add( artifactFilter );
    }
    public List<ArtifactFilter> getFilters()
    {
        return new ArrayList<ArtifactFilter>( filters );
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
        if ( !( obj instanceof AndArtifactFilter ) )
        {
            return false;
        }
        AndArtifactFilter other = (AndArtifactFilter) obj;
        return filters.equals( other.filters );
    }
}
