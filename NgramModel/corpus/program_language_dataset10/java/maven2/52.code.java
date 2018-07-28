package org.apache.maven.artifact.resolver.filter;
import org.apache.maven.artifact.Artifact;
import java.util.Iterator;
import java.util.List;
public class IncludesArtifactFilter
    implements ArtifactFilter
{
    private final List patterns;
    public IncludesArtifactFilter( List patterns )
    {
        this.patterns = patterns;
    }
    public boolean include( Artifact artifact )
    {
        String id = artifact.getGroupId() + ":" + artifact.getArtifactId();
        boolean matched = false;
        for ( Iterator i = patterns.iterator(); i.hasNext() & !matched; )
        {
            if ( id.equals( i.next() ) )
            {
                matched = true;
            }
        }
        return matched;
    }
}
