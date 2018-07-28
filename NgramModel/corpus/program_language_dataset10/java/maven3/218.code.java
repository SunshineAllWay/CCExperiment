package org.apache.maven.project.inheritance.t03;
import java.io.File;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.inheritance.AbstractProjectInheritanceTestCase;
public class ProjectInheritanceTest
    extends AbstractProjectInheritanceTestCase
{
    public void testProjectInheritance()
        throws Exception
    {
        File localRepo = getLocalRepositoryPath();
        File pom0 = new File( localRepo, "p0/pom.xml" );
        File pom0Basedir = pom0.getParentFile();
        File pom1 = new File( pom0Basedir, "p1/pom.xml" );
        MavenProject project0 = getProject( pom0 );
        MavenProject project1 = getProject( pom1 );
        assertEquals( pom0Basedir, project1.getParent().getBasedir() );
    }
}
