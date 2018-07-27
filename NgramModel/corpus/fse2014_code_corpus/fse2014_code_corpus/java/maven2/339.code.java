package org.apache.maven.project.inheritance.t04;
import java.io.File;
import java.util.Set;
import java.util.Iterator;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.inheritance.AbstractProjectInheritanceTestCase;
import org.apache.maven.artifact.Artifact;
public class ProjectInheritanceTest
    extends AbstractProjectInheritanceTestCase
{
    public void testDependencyManagementOverridesTransitiveDependencyVersion()
        throws Exception
    {
        File localRepo = getLocalRepositoryPath();
        File pom0 = new File( localRepo, "p0/pom.xml" );
        File pom0Basedir = pom0.getParentFile();
        File pom1 = new File( pom0Basedir, "p1/pom.xml" );
        MavenProject project0 = getProjectWithDependencies( pom0 );
        MavenProject project1 = getProjectWithDependencies( pom1 );
        assertEquals( pom0Basedir, project1.getParent().getBasedir() );
        Set set = project1.getArtifacts();
        assertNotNull( "No artifacts", set );
        assertTrue( "No Artifacts", set.size() > 0 );
        assertTrue( "Set size should be 3, is " + set.size(), set.size() == 3 );
        Iterator iter = set.iterator();
        while ( iter.hasNext() )
        {
            Artifact artifact = (Artifact) iter.next();
            System.out.println( "Artifact: " + artifact.getDependencyConflictId() + " " + artifact.getVersion()
                + " Optional=" + ( artifact.isOptional() ? "true" : "false" ) );
            assertTrue( "Incorrect version for " + artifact.getDependencyConflictId(),
                        artifact.getVersion().equals( "1.0" ) );
        }
    }
}