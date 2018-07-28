package org.apache.maven.project;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.codehaus.plexus.util.FileUtils;
public class DefaultMavenProjectBuilderTest
    extends AbstractMavenProjectTestCase
{
    private List<File> filesToDelete = new ArrayList<File>();
    private File localRepoDir;
    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        projectBuilder = lookup( ProjectBuilder.class );
        localRepoDir = new File( System.getProperty( "java.io.tmpdir" ), "local-repo." + System.currentTimeMillis() );
        localRepoDir.mkdirs();
        filesToDelete.add( localRepoDir );
    }
    @Override
    public void tearDown()
        throws Exception
    {
        super.tearDown();
        if ( !filesToDelete.isEmpty() )
        {
            for ( Iterator<File> it = filesToDelete.iterator(); it.hasNext(); )
            {
                File file = it.next();
                if ( file.exists() )
                {
                    if ( file.isDirectory() )
                    {
                        FileUtils.deleteDirectory( file );
                    }
                    else
                    {
                        file.delete();
                    }
                }
            }
        }
    }
    protected MavenProject getProject( Artifact pom, boolean allowStub )
        throws Exception
    {
        ProjectBuildingRequest configuration = new DefaultProjectBuildingRequest();
        configuration.setLocalRepository( getLocalRepository() );
        initRepoSession( configuration );
        return projectBuilder.build( pom, allowStub, configuration ).getProject();
    }
    public void testBuildFromMiddlePom() throws Exception
    {
        File f1 = getTestFile( "src/test/resources/projects/grandchild-check/child/pom.xml");
        File f2 = getTestFile( "src/test/resources/projects/grandchild-check/child/grandchild/pom.xml");
        getProject( f1 );
        getProject( f2 );
    }
    public void testDuplicatePluginDefinitionsMerged()
        throws Exception
    {
        File f1 = getTestFile( "src/test/resources/projects/duplicate-plugins-merged-pom.xml" );
        MavenProject project = getProject( f1 );
        assertEquals( 2, project.getBuildPlugins().get( 0 ).getDependencies().size() );
        assertEquals( 2, project.getBuildPlugins().get( 0 ).getExecutions().size() );
        assertEquals( "first", project.getBuildPlugins().get( 0 ).getExecutions().get( 0 ).getId() );
    }
    public void testBuildStubModelForMissingRemotePom()
        throws Exception
    {
        Artifact pom = repositorySystem.createProjectArtifact( "org.apache.maven.its", "missing", "0.1" );
        MavenProject project = getProject( pom, true );
        assertNotNull( project.getArtifactId() );
        assertNotNull( project.getRemoteArtifactRepositories() );
        assertFalse( project.getRemoteArtifactRepositories().isEmpty() );
        assertNotNull( project.getPluginArtifactRepositories() );
        assertFalse( project.getPluginArtifactRepositories().isEmpty() );
        assertNull( project.getParent() );
        assertNull( project.getParentArtifact() );
        assertFalse( project.isExecutionRoot() );
    }
    @Override
    protected ArtifactRepository getLocalRepository()
        throws Exception
    {
        ArtifactRepositoryLayout repoLayout = lookup( ArtifactRepositoryLayout.class, "default" );
        ArtifactRepository r = repositorySystem.createArtifactRepository( "local", "file://" + localRepoDir.getAbsolutePath(), repoLayout, null, null );
        return r;
    }
    public void xtestLoop() throws Exception
    {
        while( true )
        {
        File f1 = getTestFile( "src/test/resources/projects/duplicate-plugins-merged-pom.xml" );
        getProject( f1 );
        }
    }
}
