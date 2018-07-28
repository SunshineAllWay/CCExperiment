package org.apache.maven.project;
import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
import java.io.File;
public class ProjectBaseDirectoryAlignmentTest
    extends AbstractMavenProjectTestCase
{
    private String dir = "src/test/resources/projects/base-directory-alignment/";
    public void testProjectDirectoryBaseDirectoryAlignment()
        throws Exception
    {
        File f = getTestFile( dir + "project-which-needs-directory-alignment.xml" );
        MavenProject project = getProject( f );
        projectBuilder.calculateConcreteState( project, new DefaultProjectBuilderConfiguration() );
        assertNotNull( "Test project can't be null!", project );
        File basedirFile = new File( getBasedir() );
        File sourceDirectoryFile = new File( project.getBuild().getSourceDirectory() );
        File testSourceDirectoryFile = new File( project.getBuild().getTestSourceDirectory() );
        assertEquals( basedirFile.getCanonicalPath(), sourceDirectoryFile.getCanonicalPath().substring( 0, getBasedir().length() ) );
        assertEquals( basedirFile.getCanonicalPath(), testSourceDirectoryFile.getCanonicalPath().substring( 0, getBasedir().length() ) );
        Build build = project.getBuild();
        Resource resource = (Resource) build.getResources().get( 0 );
        assertTrue( resource.getDirectory().startsWith( getBasedir() ) );
    }
}
