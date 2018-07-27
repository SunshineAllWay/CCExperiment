package org.apache.maven.repository;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.aether.impl.internal.SimpleLocalRepositoryManager;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
public class LegacyRepositorySystemTest
    extends PlexusTestCase
{
    private RepositorySystem repositorySystem;
    private ResolutionErrorHandler resolutionErrorHandler;
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        repositorySystem = lookup( RepositorySystem.class, "default" );
        resolutionErrorHandler = lookup( ResolutionErrorHandler.class );
    }
    @Override
    protected void tearDown()
        throws Exception
    {
        repositorySystem = null;
        resolutionErrorHandler = null;
        super.tearDown();
    }
    protected List<ArtifactRepository> getRemoteRepositories()
        throws Exception
    {
        File repoDir = new File( getBasedir(), "src/test/remote-repo" ).getAbsoluteFile();
        RepositoryPolicy policy = new RepositoryPolicy();
        policy.setEnabled( true );
        policy.setChecksumPolicy( "ignore" );
        policy.setUpdatePolicy( "always" );
        Repository repository = new Repository();
        repository.setId( RepositorySystem.DEFAULT_REMOTE_REPO_ID );
        repository.setUrl( "file://" + repoDir.toURI().getPath() );
        repository.setReleases( policy );
        repository.setSnapshots( policy );
        return Arrays.asList( repositorySystem.buildArtifactRepository( repository ) );
    }
    protected ArtifactRepository getLocalRepository()
        throws Exception
    {
        File repoDir = new File( getBasedir(), "target/local-repo" ).getAbsoluteFile();
        return repositorySystem.createLocalRepository( repoDir );
    }
    public void testThatASystemScopedDependencyIsNotResolvedFromRepositories()
        throws Exception
    {
        Dependency d = new Dependency();
        d.setGroupId( "org.apache.maven.its" );
        d.setArtifactId( "b" );
        d.setVersion( "0.1" );
        d.setScope( Artifact.SCOPE_COMPILE );
        Artifact artifact = repositorySystem.createDependencyArtifact( d );
        ArtifactResolutionRequest request = new ArtifactResolutionRequest()
            .setArtifact( artifact )
            .setResolveRoot( true )
            .setResolveTransitively( true )
            .setRemoteRepositories( getRemoteRepositories() )
            .setLocalRepository( getLocalRepository() );            
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
        session.setLocalRepositoryManager( new SimpleLocalRepositoryManager( request.getLocalRepository().getBasedir() ) );
        LegacySupport legacySupport = lookup( LegacySupport.class );
        legacySupport.setSession( new MavenSession( getContainer(), session, new DefaultMavenExecutionRequest(),
                                                    new DefaultMavenExecutionResult() ) );
        ArtifactResolutionResult result = repositorySystem.resolve( request );
        resolutionErrorHandler.throwErrors( request, result );        
        assertEquals( 2, result.getArtifacts().size() );
        d.setScope( Artifact.SCOPE_SYSTEM );
        File file = new File( getBasedir(), "src/test/repository-system/maven-core-2.1.0.jar" );
        assertTrue( file.exists() );
        d.setSystemPath( file.getCanonicalPath() );
        artifact = repositorySystem.createDependencyArtifact( d );
        request = new ArtifactResolutionRequest()
            .setArtifact( artifact )
            .setResolveRoot( true )
            .setResolveTransitively( true );
        result = repositorySystem.resolve( request );
        resolutionErrorHandler.throwErrors( request, result );        
        assertEquals( 1, result.getArtifacts().size() );       
        file = new File( getBasedir(), "src/test/repository-system/maven-monkey-2.1.0.jar" );
        assertFalse( file.exists() );
        d.setSystemPath( file.getCanonicalPath() );
        artifact = repositorySystem.createDependencyArtifact( d );
        request = new ArtifactResolutionRequest()
            .setArtifact( artifact )
            .setResolveRoot( true )
            .setResolveTransitively( true );
        try
        {
            result = repositorySystem.resolve( request );
            resolutionErrorHandler.throwErrors( request, result );
        }
        catch( Exception e )
        {
            assertTrue( result.hasMissingArtifacts() );
        }
    }
    public void testLocalRepositoryBasedir()
        throws Exception
    {
        File localRepoDir = new File( "" ).getAbsoluteFile();
        ArtifactRepository localRepo = repositorySystem.createLocalRepository( localRepoDir );
        String basedir = localRepo.getBasedir();
        assertFalse( basedir.endsWith( "/" ) );
        assertFalse( basedir.endsWith( "\\" ) );
        assertEquals( localRepoDir, new File( basedir ) );
        assertEquals( localRepoDir.getPath(), basedir );
    }
}
