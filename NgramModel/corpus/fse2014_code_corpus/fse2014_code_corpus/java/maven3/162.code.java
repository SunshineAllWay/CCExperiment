package org.apache.maven.repository.metadata;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
@Component( role = GraphConflictResolutionPolicy.class )
public class DefaultGraphConflictResolutionPolicy
    implements GraphConflictResolutionPolicy
{
    @Configuration( name = "closer-first", value = "true" )
    private boolean closerFirst = true;
    @Configuration( name = "newer-first", value = "true" )
    private boolean newerFirst = true;
    public MetadataGraphEdge apply( MetadataGraphEdge e1, MetadataGraphEdge e2 )
    {
        int depth1 = e1.getDepth();
        int depth2 = e2.getDepth();
        if ( depth1 == depth2 )
        {
            ArtifactVersion v1 = new DefaultArtifactVersion( e1.getVersion() );
            ArtifactVersion v2 = new DefaultArtifactVersion( e2.getVersion() );
            if ( newerFirst )
            {
                return v1.compareTo( v2 ) > 0 ? e1 : e2;
            }
            return v1.compareTo( v2 ) > 0 ? e2 : e1;
        }
        if ( closerFirst )
        {
            return depth1 < depth2 ? e1 : e2;
        }
        return depth1 < depth2 ? e2 : e1;
    }
}
