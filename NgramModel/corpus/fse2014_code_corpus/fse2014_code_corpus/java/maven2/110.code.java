package org.apache.maven.artifact.resolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.AbstractArtifactComponentTestCase;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.metadata.ResolutionGroup;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.wagon.TransferFailedException;
import org.easymock.MockControl;
public class ArtifactResolverTest
    extends AbstractArtifactComponentTestCase
{
    private static class ArtifactMetadataSourceImplementation
        implements ArtifactMetadataSource
    {
        public ResolutionGroup retrieve( Artifact artifact, ArtifactRepository localRepository,
                                         List remoteRepositories )
            throws ArtifactMetadataRetrievalException
        {
            return new ResolutionGroup( artifact, Collections.EMPTY_SET, remoteRepositories );
        }
        public List retrieveAvailableVersions( Artifact artifact, ArtifactRepository localRepository,
                                               List remoteRepositories )
        {
            throw new UnsupportedOperationException( "Cannot get available versions in this test case" );
        }
        public Artifact retrieveRelocatedArtifact( Artifact artifact,
                                                   ArtifactRepository localRepository,
                                                   List remoteRepositories )
            throws ArtifactMetadataRetrievalException
        {
            return artifact;
        }
    }
    private ArtifactResolver artifactResolver;
    private Artifact projectArtifact;
    protected void setUp()
        throws Exception
    {
        super.setUp();
        artifactResolver = (ArtifactResolver) lookup( ArtifactResolver.ROLE );
        projectArtifact = createLocalArtifact( "project", "3.0", null );
    }
    protected String component()
    {
        return "resolver";
    }
    public void testResolutionOfASingleArtifactWhereTheArtifactIsPresentInTheLocalRepository()
        throws Exception
    {
        Artifact a = createLocalArtifact( "a", "1.0", null );
        artifactResolver.resolve( a, remoteRepositories(), localRepository() );
        assertLocalArtifactPresent( a );
    }
    public void testResolutionOfASingleArtifactWhereTheArtifactIsNotPresentLocallyAndMustBeRetrievedFromTheRemoteRepository()
        throws Exception
    {
        Artifact b = createRemoteArtifact( "b", "1.0" );
        deleteLocalArtifact( b );
        artifactResolver.resolve( b, remoteRepositories(), localRepository() );
        assertLocalArtifactPresent( b );
    }
    protected Artifact createArtifact( String groupId, String artifactId, String version, String type )
        throws Exception
    {
        return super.createArtifact( groupId, artifactId, version, type );
    }
    public void testTransitiveResolutionWhereAllArtifactsArePresentInTheLocalRepository()
        throws Exception
    {
        Artifact g = createLocalArtifact( "g", "1.0", null );
        Artifact h = createLocalArtifact( "h", "1.0", null );
        ArtifactMetadataSource mds = new ArtifactMetadataSourceImplementation()
        {
            public ResolutionGroup retrieve( Artifact artifact, ArtifactRepository localRepository,
                                             List remoteRepositories )
                throws ArtifactMetadataRetrievalException
            {
                Set dependencies = new LinkedHashSet();
                if ( "g".equals( artifact.getArtifactId() ) )
                {
                    Artifact a = null;
                    try
                    {
                        a = createArtifact( "org.apache.maven", "h", "1.0", "jar" );
                        dependencies.add( a );
                    }
                    catch ( Exception e )
                    {
                        throw new ArtifactMetadataRetrievalException( "Error retrieving metadata", e, a );
                    }
                }
                return new ResolutionGroup( artifact, dependencies, remoteRepositories );
            }
        };
        ArtifactResolutionResult result = artifactResolver.resolveTransitively( Collections.singleton( g ),
                                                                                projectArtifact, remoteRepositories(),
                                                                                localRepository(), mds );
        assertEquals( 2, result.getArtifacts().size() );
        assertTrue( result.getArtifacts().contains( g ) );
        assertTrue( result.getArtifacts().contains( h ) );
        assertLocalArtifactPresent( g );
        assertLocalArtifactPresent( h );
    }
    public void testTransitiveResolutionWhereAllArtifactsAreNotPresentInTheLocalRepositoryAndMustBeRetrievedFromTheRemoteRepository()
        throws Exception
    {
        Artifact i = createRemoteArtifact( "i", "1.0" );
        deleteLocalArtifact( i );
        Artifact j = createRemoteArtifact( "j", "1.0" );
        deleteLocalArtifact( j );
        ArtifactMetadataSource mds = new ArtifactMetadataSourceImplementation()
        {
            public ResolutionGroup retrieve( Artifact artifact, ArtifactRepository localRepository,
                                             List remoteRepositories )
                throws ArtifactMetadataRetrievalException
            {
                Set dependencies = new LinkedHashSet();
                if ( "i".equals( artifact.getArtifactId() ) )
                {
                    Artifact a = null;
                    try
                    {
                        a = createArtifact( "org.apache.maven", "j", "1.0", "jar" );
                        dependencies.add( a );
                    }
                    catch ( Exception e )
                    {
                        throw new ArtifactMetadataRetrievalException( "Error retrieving metadata", e, a );
                    }
                }
                return new ResolutionGroup( artifact, dependencies, remoteRepositories );
            }
        };
        ArtifactResolutionResult result = artifactResolver.resolveTransitively( Collections.singleton( i ),
                                                                                projectArtifact, remoteRepositories(),
                                                                                localRepository(), mds );
        assertEquals( 2, result.getArtifacts().size() );
        assertTrue( result.getArtifacts().contains( i ) );
        assertTrue( result.getArtifacts().contains( j ) );
        assertLocalArtifactPresent( i );
        assertLocalArtifactPresent( j );
    }
    public void testResolutionFailureWhenArtifactNotPresentInRemoteRepository()
        throws Exception
    {
        Artifact k = createArtifact( "k", "1.0" );
        try
        {
            artifactResolver.resolve( k, remoteRepositories(), localRepository() );
            fail( "Resolution succeeded when it should have failed" );
        }
        catch ( ArtifactNotFoundException expected )
        {
            List repos = expected.getRemoteRepositories();
            assertEquals( 1, repos.size() );
            assertEquals( "test", ( (ArtifactRepository) repos.get( 0 ) ).getId() );
        }
    }
    public void testResolutionFailureWhenArtifactNotPresentInRemoteRepositoryWithMirrors()
        throws Exception
    {
        ArtifactRepository repository = remoteRepository();
        WagonManager wagonManager = (WagonManager) lookup( WagonManager.ROLE );
        wagonManager.addMirror( "mirror", "test", repository.getUrl() );
        Artifact k = createArtifact( "k", "1.0" );
        try
        {
            artifactResolver.resolve( k, Collections.singletonList( repository ), localRepository() );
            fail( "Resolution succeeded when it should have failed" );
        }
        catch ( ArtifactNotFoundException expected )
        {
            List repos = expected.getRemoteRepositories();
            assertEquals( 1, repos.size() );
            assertEquals( "mirror", ( (ArtifactRepository) repos.get( 0 ) ).getId() );
        }
    }
    public void testResolutionOfAnArtifactWhereOneRemoteRepositoryIsBadButOneIsGood()
        throws Exception
    {
        Artifact l = createRemoteArtifact( "l", "1.0" );
        deleteLocalArtifact( l );
        List repositories = new ArrayList();
        repositories.add( remoteRepository() );
        repositories.add( badRemoteRepository() );
        artifactResolver.resolve( l, repositories, localRepository() );
        assertLocalArtifactPresent( l );
    }
    public void testResolutionFailureWhenMultipleArtifactsNotPresentInRemoteRepository()
        throws Exception
    {
        Artifact i = createArtifact( "i", "1.0" );
        Artifact n = createArtifact( "n", "1.0" );
        Artifact o = createArtifact( "o", "1.0" );
        try
        {
            ArtifactMetadataSource mds = new ArtifactMetadataSourceImplementation();
            artifactResolver.resolveTransitively( new HashSet( Arrays.asList( new Artifact[] { i, n, o } ) ),
                                                  projectArtifact, remoteRepositories(), localRepository(), mds );
            fail( "Resolution succeeded when it should have failed" );
        }
        catch ( MultipleArtifactsNotFoundException expected )
        {
            List repos = expected.getRemoteRepositories();
            assertEquals( 1, repos.size() );
            assertEquals( "test", ( (ArtifactRepository) repos.get( 0 ) ).getId() );
            List missingArtifacts = expected.getMissingArtifacts();
            assertEquals( 2, missingArtifacts.size() );
            assertTrue( missingArtifacts.contains( n ) );
            assertTrue( missingArtifacts.contains( o ) );
            assertFalse( missingArtifacts.contains( i ) );
        }
    }
    public void testResolveWithException()
        throws Exception
    {
        ArtifactRepository repository = remoteRepository();
        List remoteRepositories = Collections.singletonList( repository );
        Artifact a1 = createArtifact( "testGroup", "artifactId", "1.0", "jar" );
        ArtifactMetadataSource mds = new ArtifactMetadataSourceImplementation();
        DefaultArtifactResolver artifactResolver = (DefaultArtifactResolver) this.artifactResolver;
        MockControl control = MockControl.createControl( WagonManager.class );
        WagonManager wagonManager = (WagonManager) control.getMock();
        artifactResolver.setWagonManager( wagonManager );
        wagonManager.isOnline();
        control.setReturnValue( true );
        wagonManager.getArtifact( a1, remoteRepositories );
        control.setThrowable( new TransferFailedException( "message" ) );
        wagonManager.getMirrorRepository( repository );
        control.setReturnValue( repository );
        control.replay();
        try
        {
            artifactResolver.resolveTransitively( new LinkedHashSet( Arrays.asList( new Artifact[] { a1 } ) ),
                                                  projectArtifact, remoteRepositories, localRepository(), mds );
            fail( "Resolution succeeded when it should have failed" );
        }
        catch ( ArtifactResolutionException expected )
        {
            List repos = expected.getRemoteRepositories();
            assertEquals( 1, repos.size() );
            assertEquals( "test", ( (ArtifactRepository) repos.get( 0 ) ).getId() );
            assertEquals( "testGroup", expected.getGroupId() );
        }
        control.verify();
    }
    public void testResolveMultipleWithException()
        throws Exception
    {
        ArtifactRepository repository = remoteRepository();
        List remoteRepositories = Collections.singletonList( repository );
        Artifact a1 = createArtifact( "testGroup", "artifactId", "1.0", "jar" );
        Artifact a2 = createArtifact( "testGroup", "anotherId", "1.0", "jar" );
        ArtifactMetadataSource mds = new ArtifactMetadataSourceImplementation();
        DefaultArtifactResolver artifactResolver = (DefaultArtifactResolver) this.artifactResolver;
        MockControl control = MockControl.createControl( WagonManager.class );
        WagonManager wagonManager = (WagonManager) control.getMock();
        artifactResolver.setWagonManager( wagonManager );
        wagonManager.isOnline();
        control.setReturnValue( true );
        wagonManager.getArtifact( a1, remoteRepositories );
        control.setThrowable( new TransferFailedException( "message" ) );
        wagonManager.getMirrorRepository( repository );
        control.setReturnValue( repository );
        wagonManager.isOnline();
        control.setReturnValue( true );
        wagonManager.getArtifact( a2, remoteRepositories );
        control.setThrowable( new TransferFailedException( "message" ) );
        wagonManager.getMirrorRepository( repository );
        control.setReturnValue( repository );
        control.replay();
        try
        {
            artifactResolver.resolveTransitively( new LinkedHashSet( Arrays.asList( new Artifact[] { a1, a2 } ) ),
                                                  projectArtifact, remoteRepositories, localRepository(), mds );
            fail( "Resolution succeeded when it should have failed" );
        }
        catch ( ArtifactResolutionException expected )
        {
            List repos = expected.getRemoteRepositories();
            assertEquals( 1, repos.size() );
            assertEquals( "test", ( (ArtifactRepository) repos.get( 0 ) ).getId() );
            assertEquals( "testGroup", expected.getGroupId() );
        }
        control.verify();
    }
    public void testResolveOlderSpecificSnapshotVersionWhenNewerVersionAlreadyExistsAndBothAreInLocalRepository()
        throws Exception
    {
        Artifact g1 = createLocalArtifact( "g", "1.0-20090608.090416-1", "Write something to make file length longer" );
        long expectedFileLength = g1.getFile().length();
        Artifact g3 = createLocalArtifact( "g", "1.0-SNAPSHOT", null );
        long incorrectFileLength = g3.getFile().length();
        ArtifactMetadataSource mds = new ArtifactMetadataSourceImplementation()
        {
            public ResolutionGroup retrieve( Artifact artifact, ArtifactRepository localRepository,
                                             List remoteRepositories )
                throws ArtifactMetadataRetrievalException
            {
                Set dependencies = new LinkedHashSet();
                return new ResolutionGroup( artifact, dependencies, remoteRepositories );
            }
        };
        ArtifactResolutionResult result = artifactResolver.resolveTransitively( Collections.singleton( g1 ),
                                                                                projectArtifact, remoteRepositories(),
                                                                                localRepository(), mds );
        assertEquals( 1, result.getArtifacts().size() );
        assertTrue( result.getArtifacts().contains( g1 ) );
        assertFalse( result.getArtifacts().contains( g3 ) );
        Artifact artifact = ( (Artifact) result.getArtifacts().iterator().next() );
        assertEquals( expectedFileLength, artifact.getFile().length() );
        assertFalse( "Incorrect artifact file resolved", incorrectFileLength == artifact.getFile().length() );
        assertLocalArtifactPresent( g1 );
        assertLocalArtifactPresent( g3 );
    }
}
