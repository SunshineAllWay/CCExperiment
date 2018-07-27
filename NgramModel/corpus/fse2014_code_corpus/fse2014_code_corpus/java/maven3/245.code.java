package org.apache.maven.repository.legacy.resolver.conflict;
import java.util.Collections;
import org.apache.maven.artifact.resolver.ResolutionNode;
import org.apache.maven.repository.legacy.resolver.conflict.NearestConflictResolver;
public class NearestConflictResolverTest
    extends AbstractConflictResolverTest
{
    public NearestConflictResolverTest()
        throws Exception
    {
        super("nearest");
    }
    public void testDepth()
    {
        ResolutionNode a1n = new ResolutionNode( a1, Collections.EMPTY_LIST );
        ResolutionNode b1n = new ResolutionNode( b1, Collections.EMPTY_LIST );
        ResolutionNode a2n = new ResolutionNode( a2, Collections.EMPTY_LIST, b1n );
        assertResolveConflict( a1n, a1n, a2n );
    }
    public void testDepthReversed()
    {
        ResolutionNode b1n = new ResolutionNode( b1, Collections.EMPTY_LIST );
        ResolutionNode a2n = new ResolutionNode( a2, Collections.EMPTY_LIST, b1n );
        ResolutionNode a1n = new ResolutionNode( a1, Collections.EMPTY_LIST );
        assertResolveConflict( a1n, a2n, a1n );
    }
    public void testEqual()
    {
        ResolutionNode a1n = new ResolutionNode( a1, Collections.EMPTY_LIST );
        ResolutionNode a2n = new ResolutionNode( a2, Collections.EMPTY_LIST );
        assertResolveConflict( a1n, a1n, a2n );
    }
    public void testEqualReversed()
    {
        ResolutionNode a2n = new ResolutionNode( a2, Collections.EMPTY_LIST );
        ResolutionNode a1n = new ResolutionNode( a1, Collections.EMPTY_LIST );
        assertResolveConflict( a2n, a2n, a1n );
    }
}
