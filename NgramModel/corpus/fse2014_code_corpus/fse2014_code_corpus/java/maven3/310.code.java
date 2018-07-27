package org.apache.maven.artifact.resolver.filter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
public class IncludesArtifactFilter
    implements ArtifactFilter
{
    private final Set<String> patterns;
    public IncludesArtifactFilter( List<String> patterns )
    {
        this.patterns = new LinkedHashSet<String>( patterns );
    }
    public boolean include( Artifact artifact )
    {
        String id = artifact.getGroupId() + ":" + artifact.getArtifactId();
        boolean matched = false;
        for ( Iterator<String> i = patterns.iterator(); i.hasNext() & !matched; )
        {
            if ( id.equals( i.next() ) )
            {
                matched = true;
            }
        }
        return matched;
    }
    public List<String> getPatterns()
    {
        return new ArrayList<String>( patterns );
    }
    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + patterns.hashCode();
        return hash;
    }
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) 
        {
            return true;
        }
        if ( obj == null || getClass() != obj.getClass() )
        {
            return false;
        }
        IncludesArtifactFilter other = (IncludesArtifactFilter) obj;
        return patterns.equals( other.patterns );
    }
}
