package org.apache.maven.project.inheritance.t09;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import org.apache.maven.model.Build;
import org.apache.maven.model.MailingList;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.inheritance.AbstractProjectInheritanceTestCase;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.logging.Logger;
public class ProjectInheritanceTest
    extends AbstractProjectInheritanceTestCase
{
    public void testDependencyManagementExclusionsExcludeTransitively()
        throws Exception
    {
        File localRepo = getLocalRepositoryPath();
        File pom0 = new File( localRepo, "p0/pom.xml" );
        File pom0Basedir = pom0.getParentFile();
        File pom1 = new File( pom0Basedir, "p1/pom.xml" );
        MavenProject project0 = getProjectWithDependencies( pom0 );
        MavenProject project1 = getProjectWithDependencies( pom1 );
        assertEquals( pom0Basedir, project1.getParent().getBasedir() );
        System.out.println("Project " + project1.getId() + " " + project1);
        Map map = project1.getArtifactMap();
        assertNotNull("No artifacts", map);
        assertTrue("No Artifacts", map.size() > 0);
        assertTrue("Set size should be 2, is " + map.size(), map.size() == 2);
        assertTrue("maven-test-a is not in the project", map.containsKey( "maven-test:maven-test-a" ));
        assertTrue("maven-test-b is not in the project", map.containsKey( "maven-test:maven-test-b" ));
    }
    public void testDependencyManagementExclusionDoesNotOverrideGloballyForTransitives()
        throws Exception
    {
        File localRepo = getLocalRepositoryPath();
        File pom0 = new File( localRepo, "p0/pom.xml" );
        File pom0Basedir = pom0.getParentFile();
        File pom2 = new File( pom0Basedir, "p2/pom.xml" );
        MavenProject project0 = getProjectWithDependencies( pom0 );
        MavenProject project2 = getProjectWithDependencies( pom2 );
        assertEquals( pom0Basedir, project2.getParent().getBasedir() );
        System.out.println( "Project " + project2.getId() + " " + project2 );
        Map map = project2.getArtifactMap();
        assertNotNull( "No artifacts", map );
        assertTrue( "No Artifacts", map.size() > 0 );
        assertTrue( "Set size should be 4, is " + map.size(), map.size() == 4 );
        assertTrue( "maven-test-a is not in the project", map.containsKey( "maven-test:maven-test-a" ) );
        assertTrue( "maven-test-b is not in the project", map.containsKey( "maven-test:maven-test-b" ) );
        assertTrue( "maven-test-c is not in the project", map.containsKey( "maven-test:maven-test-c" ) );
        assertTrue( "maven-test-d is not in the project", map.containsKey( "maven-test:maven-test-d" ) );
    }
}