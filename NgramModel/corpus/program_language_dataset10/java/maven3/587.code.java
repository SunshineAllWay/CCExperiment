package org.apache.maven.project;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.PlexusTestCase;
public abstract class AbstractMavenProjectTestCase
    extends PlexusTestCase
{
    protected ProjectBuilder projectBuilder;
    protected RepositorySystem repositorySystem;
    protected void setUp()
        throws Exception
    {
        super.setUp();
        if ( getContainer().hasComponent( ProjectBuilder.class, "test" ) )
        {
            projectBuilder = lookup( ProjectBuilder.class, "test" );
        }
        else
        {
            projectBuilder = lookup( ProjectBuilder.class );
        }
        repositorySystem = lookup( RepositorySystem.class );        
    }    
    @Override
    protected void tearDown()
        throws Exception
    {
        projectBuilder = null;
        super.tearDown();
    }
    protected ProjectBuilder getProjectBuilder()
    {
        return projectBuilder;
    }
    @Override
    protected String getCustomConfigurationName()
    {
        String name = AbstractMavenProjectTestCase.class.getName().replace( '.', '/' ) + ".xml";
        System.out.println( name );
        return name;
    }
    protected File getLocalRepositoryPath()
        throws FileNotFoundException, URISyntaxException
    {
        File markerFile = getFileForClasspathResource( "local-repo/marker.txt" );
        return markerFile.getAbsoluteFile().getParentFile();
    }
    protected static File getFileForClasspathResource( String resource )
        throws FileNotFoundException
    {
        ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        URL resourceUrl = cloader.getResource( resource );
        if ( resourceUrl == null )
        {
            throw new FileNotFoundException( "Unable to find: " + resource );
        }
        return new File( URI.create( resourceUrl.toString().replaceAll( " ", "%20" ) ) );
    }
    protected ArtifactRepository getLocalRepository()
        throws Exception
    {
        ArtifactRepositoryLayout repoLayout = lookup( ArtifactRepositoryLayout.class, "legacy" );
        ArtifactRepository r = repositorySystem.createArtifactRepository( "local", "file://" + getLocalRepositoryPath().getAbsolutePath(), repoLayout, null, null );
        return r;
    }
    protected MavenProject getProjectWithDependencies( File pom )
        throws Exception
    {
        ProjectBuildingRequest configuration = new DefaultProjectBuildingRequest();
        configuration.setLocalRepository( getLocalRepository() );
        configuration.setRemoteRepositories( Arrays.asList( new ArtifactRepository[] {} ) );
        configuration.setProcessPlugins( false );
        configuration.setResolveDependencies( true );
        initRepoSession( configuration );
        try
        {
            return projectBuilder.build( pom, configuration ).getProject();
        }
        catch ( Exception e )
        {
            Throwable cause = e.getCause();
            if ( cause instanceof ModelBuildingException )
            {
                String message = "In: " + pom + "\n\n";
                for ( ModelProblem problem : ( (ModelBuildingException) cause ).getProblems() )
                {
                    message += problem + "\n";
                }
                System.out.println( message );
                fail( message );
            }
            throw e;
        }
    }
    protected MavenProject getProject( File pom )
        throws Exception
    {
        ProjectBuildingRequest configuration = new DefaultProjectBuildingRequest();
        configuration.setLocalRepository( getLocalRepository() );
        initRepoSession( configuration );
        return projectBuilder.build( pom, configuration ).getProject();
    }
    protected void initRepoSession( ProjectBuildingRequest request )
    {
        File localRepo = new File( request.getLocalRepository().getBasedir() );
        MavenRepositorySystemSession repoSession = new MavenRepositorySystemSession();
        repoSession.setLocalRepositoryManager( new LegacyLocalRepositoryManager( localRepo ) );
        request.setRepositorySession( repoSession );
    }
}
