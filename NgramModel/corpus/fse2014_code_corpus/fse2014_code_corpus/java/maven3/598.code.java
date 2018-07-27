package org.apache.maven.project.artifact;
import java.util.Arrays;
import java.util.Collections;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.project.artifact.DefaultMavenMetadataCache.CacheKey;
import org.apache.maven.repository.DelegatingLocalArtifactRepository;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.PlexusTestCase;
public class DefaultMavenMetadataCacheTest
    extends PlexusTestCase
{
    private RepositorySystem repositorySystem;
    protected void setUp()
        throws Exception
    {
        super.setUp();
        repositorySystem = lookup( RepositorySystem.class );
    }
    @Override
    protected void tearDown()
        throws Exception
    {
        repositorySystem = null;
        super.tearDown();
    }
    public void testCacheKey()
        throws Exception
    {
        Artifact a1 = repositorySystem.createArtifact( "testGroup", "testArtifact", "1.2.3", "jar" );
        ArtifactRepository lr1 = new DelegatingLocalArtifactRepository( repositorySystem.createDefaultLocalRepository() );
        ArtifactRepository rr1 = repositorySystem.createDefaultRemoteRepository();
        a1.setDependencyFilter( new ExcludesArtifactFilter( Arrays.asList( "foo" ) ) );
        Artifact a2 = repositorySystem.createArtifact( "testGroup", "testArtifact", "1.2.3", "jar" );
        ArtifactRepository lr2 = new DelegatingLocalArtifactRepository( repositorySystem.createDefaultLocalRepository() );
        ArtifactRepository rr2 = repositorySystem.createDefaultRemoteRepository();
        a2.setDependencyFilter( new ExcludesArtifactFilter( Arrays.asList( "foo" ) ) );
        assertNotSame( a1, a2 );
        assertNotSame( lr1, lr2 );
        assertNotSame( rr1, rr2 );
        CacheKey k1 = new CacheKey( a1, false, lr1, Collections.singletonList( rr1 ) );
        CacheKey k2 = new CacheKey( a2, false, lr2, Collections.singletonList( rr2 ) );
        assertEquals(k1.hashCode(), k2.hashCode());
    }
}
