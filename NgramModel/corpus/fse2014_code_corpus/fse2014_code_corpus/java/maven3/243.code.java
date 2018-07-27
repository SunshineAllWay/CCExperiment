package org.apache.maven.repository.legacy.resolver.conflict;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ResolutionNode;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.repository.legacy.resolver.conflict.ConflictResolver;
import org.codehaus.plexus.PlexusTestCase;
public abstract class AbstractConflictResolverTest
    extends PlexusTestCase
{
    private static final String GROUP_ID = "test";
    protected Artifact a1;
    protected Artifact a2;
    protected Artifact b1;
    private final String roleHint;
    private ArtifactFactory artifactFactory;
    private ConflictResolver conflictResolver;
    public AbstractConflictResolverTest( String roleHint )
        throws Exception
    {
        this.roleHint = roleHint;
    }
    protected void setUp() throws Exception
    {
        super.setUp();
        artifactFactory = (ArtifactFactory) lookup( ArtifactFactory.ROLE );
        conflictResolver = (ConflictResolver) lookup( ConflictResolver.ROLE, roleHint );
        a1 = createArtifact( "a", "1.0" );
        a2 = createArtifact( "a", "2.0" );
        b1 = createArtifact( "b", "1.0" );
    }
    protected void tearDown() throws Exception
    {
        a1 = null;
        a2 = null;
        b1 = null;
        artifactFactory = null;
        conflictResolver = null;
        super.tearDown();
    }
    protected ConflictResolver getConflictResolver()
    {
        return conflictResolver;
    }
    protected void assertResolveConflict( ResolutionNode expectedNode, ResolutionNode actualNode1, ResolutionNode actualNode2 )
    {
        ResolutionNode resolvedNode = getConflictResolver().resolveConflict( actualNode1, actualNode2 );
        assertNotNull( "Expected resolvable", resolvedNode );
        assertEquals( "Resolution node", expectedNode, resolvedNode );
    }
    protected void assertUnresolvableConflict( ResolutionNode actualNode1, ResolutionNode actualNode2 )
    {
        ResolutionNode resolvedNode = getConflictResolver().resolveConflict( actualNode1, actualNode2 );
        assertNull( "Expected unresolvable", resolvedNode );
    }
    protected Artifact createArtifact( String id, String version ) throws InvalidVersionSpecificationException
    {
        return createArtifact( id, version, Artifact.SCOPE_COMPILE );
    }
    protected Artifact createArtifact( String id, String version, boolean optional )
        throws InvalidVersionSpecificationException
    {
        return createArtifact( id, version, Artifact.SCOPE_COMPILE, null, optional );
    }
    protected Artifact createArtifact( String id, String version, String scope )
        throws InvalidVersionSpecificationException
    {
        return createArtifact( id, version, scope, null, false );
    }
    protected Artifact createArtifact( String id, String version, String scope, String inheritedScope, boolean optional )
        throws InvalidVersionSpecificationException
    {
        VersionRange versionRange = VersionRange.createFromVersionSpec( version );
        return artifactFactory.createDependencyArtifact( GROUP_ID, id, versionRange, "jar", null, scope,
                                                         inheritedScope, optional );
    }
}
