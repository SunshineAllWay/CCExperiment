package org.apache.maven.project;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.project.validation.ModelValidationResult;
import org.codehaus.plexus.PlexusTestCase;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;
public abstract class AbstractMavenProjectTestCase
    extends PlexusTestCase
{
    protected MavenProjectBuilder projectBuilder;
    protected void setUp()
        throws Exception
    {
        super.setUp();
        if ( getContainer().hasComponent( MavenProjectBuilder.ROLE, "test" ) )
        {
            projectBuilder = (MavenProjectBuilder) lookup( MavenProjectBuilder.ROLE, "test" );
        }
        else
        {
            projectBuilder = (MavenProjectBuilder) lookup( MavenProjectBuilder.ROLE );
        }
    }
    protected File getLocalRepositoryPath()
        throws FileNotFoundException, URISyntaxException
    {
        File markerFile = getFileForClasspathResource( "local-repo/marker.txt" );
        return markerFile.getAbsoluteFile().getParentFile();
    }
    protected File getFileForClasspathResource( String resource )
        throws FileNotFoundException, URISyntaxException
    {
        ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        URL resourceUrl = cloader.getResource( resource );
        if ( resourceUrl == null )
        {
            throw new FileNotFoundException( "Unable to find: " + resource );
        }
        return new File( new URI( resourceUrl.toString() ) );
    }
    protected ArtifactRepository getLocalRepository()
        throws Exception
    {
        ArtifactRepositoryLayout repoLayout = (ArtifactRepositoryLayout) lookup( ArtifactRepositoryLayout.ROLE,
                                                                                 "legacy" );
        ArtifactRepository r = new DefaultArtifactRepository( "local",
                                                              "file://" + getLocalRepositoryPath().getAbsolutePath(),
                                                              repoLayout );
        return r;
    }
    protected MavenProject getProjectWithDependencies( File pom )
        throws Exception
    {
        try
        {
            return projectBuilder.buildWithDependencies( pom, getLocalRepository(), null );
        }
        catch ( Exception e )
        {
            if ( e instanceof InvalidProjectModelException )
            {
                ModelValidationResult validationResult = ((InvalidProjectModelException)e).getValidationResult();
                String message = "In: " + pom + "(" + ((ProjectBuildingException) e).getProjectId() + ")\n\n" + validationResult.render( "  " );
                System.out.println( message );
                fail( message );
            }
            throw e;
        }
    }
    protected MavenProject getProject( File pom )
        throws Exception
    {
        return projectBuilder.build( pom, getLocalRepository(), new DefaultProfileManager( getContainer() ) );
    }
}
