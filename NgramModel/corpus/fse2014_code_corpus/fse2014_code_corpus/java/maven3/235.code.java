package org.apache.maven.repository.legacy;
import java.io.File;
import java.util.Arrays;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.Authentication;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Server;
import org.codehaus.plexus.PlexusTestCase;
public class LegacyRepositorySystemTest
    extends PlexusTestCase
{
    private RepositorySystem repositorySystem;
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        repositorySystem = lookup( RepositorySystem.class, "default" );
    }
    @Override
    protected void tearDown()
        throws Exception
    {
        repositorySystem = null;
        super.tearDown();
    }
    public void testThatLocalRepositoryWithSpacesIsProperlyHandled()
        throws Exception
    {
        File basedir = new File( "target/spacy path" ).getAbsoluteFile();
        ArtifactRepository repo = repositorySystem.createLocalRepository( basedir );
        assertEquals( basedir, new File( repo.getBasedir() ) );
    }
    public void testAuthenticationHandling()
        throws Exception
    {
        Server server = new Server();
        server.setId( "repository" );
        server.setUsername( "jason" );
        server.setPassword( "abc123" );
        ArtifactRepository repository =
            repositorySystem.createArtifactRepository( "repository", "http://foo", null, null, null );
        repositorySystem.injectAuthentication( Arrays.asList( repository ), Arrays.asList( server ) );
        Authentication authentication = repository.getAuthentication();
        assertNotNull( authentication );
        assertEquals( "jason", authentication.getUsername() );
        assertEquals( "abc123", authentication.getPassword() );
    }
}
