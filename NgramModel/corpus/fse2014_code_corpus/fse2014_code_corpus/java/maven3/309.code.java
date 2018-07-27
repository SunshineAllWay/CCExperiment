package org.apache.maven.artifact.resolver.filter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
public class ExclusionSetFilter
    implements ArtifactFilter
{
    private Set<String> excludes;
    public ExclusionSetFilter( String[] excludes )
    {
        this.excludes = new LinkedHashSet<String>( Arrays.asList( excludes ) );
    }
    public ExclusionSetFilter( Set<String> excludes )
    {
        this.excludes = excludes;
    }
    public boolean include( Artifact artifact )
    {
        String id = artifact.getArtifactId();
        if ( excludes.contains( id ) )
        {
            return false;
        }
        id = artifact.getGroupId() + ':' + id;
        if ( excludes.contains( id ) )
        {
            return false;
        }
        return true;
    }
    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + excludes.hashCode();
        return hash;
    }
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( !( obj instanceof ExclusionSetFilter ) )
        {
            return false;
        }
        ExclusionSetFilter other = (ExclusionSetFilter) obj;
        return excludes.equals( other.excludes );
    }
}
