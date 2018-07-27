package org.apache.maven.repository.legacy.resolver.conflict;
import org.apache.maven.artifact.resolver.ResolutionNode;
import org.codehaus.plexus.component.annotations.Component;
@Component( role = ConflictResolver.class, hint = "nearest" )
public class NearestConflictResolver
    implements ConflictResolver
{
    public ResolutionNode resolveConflict( ResolutionNode node1, ResolutionNode node2 )
    {
        return node1.getDepth() <= node2.getDepth() ? node1 : node2;
    }
}
