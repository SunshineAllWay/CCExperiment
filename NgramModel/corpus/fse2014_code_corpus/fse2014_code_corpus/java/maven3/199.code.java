package org.apache.maven.artifact.resolver.filter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import junit.framework.TestCase;
public class ScopeArtifactFilterTest
    extends TestCase
{
    private Artifact newArtifact( String scope )
    {
        return new DefaultArtifact( "g", "a", "1.0", scope, "jar", "", null );
    }
    public void testInclude_Compile()
    {
        ScopeArtifactFilter filter = new ScopeArtifactFilter( Artifact.SCOPE_COMPILE );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_COMPILE ) ) );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_SYSTEM ) ) );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_PROVIDED ) ) );
        assertFalse( filter.include( newArtifact( Artifact.SCOPE_RUNTIME ) ) );
        assertFalse( filter.include( newArtifact( Artifact.SCOPE_TEST ) ) );
    }
    public void testInclude_CompilePlusRuntime()
    {
        ScopeArtifactFilter filter = new ScopeArtifactFilter( Artifact.SCOPE_COMPILE_PLUS_RUNTIME );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_COMPILE ) ) );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_SYSTEM ) ) );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_PROVIDED ) ) );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_RUNTIME ) ) );
        assertFalse( filter.include( newArtifact( Artifact.SCOPE_TEST ) ) );
    }
    public void testInclude_Runtime()
    {
        ScopeArtifactFilter filter = new ScopeArtifactFilter( Artifact.SCOPE_RUNTIME );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_COMPILE ) ) );
        assertFalse( filter.include( newArtifact( Artifact.SCOPE_SYSTEM ) ) );
        assertFalse( filter.include( newArtifact( Artifact.SCOPE_PROVIDED ) ) );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_RUNTIME ) ) );
        assertFalse( filter.include( newArtifact( Artifact.SCOPE_TEST ) ) );
    }
    public void testInclude_RuntimePlusSystem()
    {
        ScopeArtifactFilter filter = new ScopeArtifactFilter( Artifact.SCOPE_RUNTIME_PLUS_SYSTEM );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_COMPILE ) ) );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_SYSTEM ) ) );
        assertFalse( filter.include( newArtifact( Artifact.SCOPE_PROVIDED ) ) );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_RUNTIME ) ) );
        assertFalse( filter.include( newArtifact( Artifact.SCOPE_TEST ) ) );
    }
    public void testInclude_Test()
    {
        ScopeArtifactFilter filter = new ScopeArtifactFilter( Artifact.SCOPE_TEST );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_COMPILE ) ) );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_SYSTEM ) ) );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_PROVIDED ) ) );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_RUNTIME ) ) );
        assertTrue( filter.include( newArtifact( Artifact.SCOPE_TEST ) ) );
    }
}
