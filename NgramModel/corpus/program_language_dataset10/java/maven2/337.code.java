package org.apache.maven.project.inheritance.t02;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.maven.model.Build;
import org.apache.maven.model.MailingList;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.inheritance.AbstractProjectInheritanceTestCase;
public class ProjectInheritanceTest
    extends AbstractProjectInheritanceTestCase
{
    public void testProjectInheritance()
        throws Exception
    {
        File localRepo = getLocalRepositoryPath();
        System.out.println( "Local repository is at: " + localRepo.getAbsolutePath() );
        File pom0 = new File( localRepo, "p0/pom.xml" );
        File pom1 = new File( pom0.getParentFile(), "p1/pom.xml" );
        File pom2 = new File( pom1.getParentFile(), "p2/pom.xml" );
        File pom3 = new File( pom2.getParentFile(), "p3/pom.xml" );
        File pom4 = new File( pom3.getParentFile(), "p4/pom.xml" );
        File pom5 = new File( pom4.getParentFile(), "p5/pom.xml" );
        System.out.println( "Location of project-4's POM: " + pom4.getPath() );
        MavenProject project0 = getProject( pom0 );
        MavenProject project1 = getProject( pom1 );
        MavenProject project2 = getProject( pom2 );
        MavenProject project3 = getProject( pom3 );
        MavenProject project4 = getProject( pom4 );
        MavenProject project5 = getProject( pom5 );
        assertEquals( "p4", project4.getName() );
        assertEquals( "2000", project4.getInceptionYear() );
        assertEquals( "mailing-list", ( (MailingList) project4.getMailingLists().get( 0 ) ).getName() );
        assertEquals( "scm-url/p2/p3/p4", project4.getScm().getUrl() );
        assertEquals( "Codehaus", project4.getOrganization().getName() );
        assertEquals( "4.0.0", project4.getModelVersion() );
        Build build = project4.getBuild();
        List plugins = build.getPlugins();
        Map validPluginCounts = new HashMap();
        String testPluginArtifactId = "maven-compiler-plugin";
        validPluginCounts.put( testPluginArtifactId, new Integer( 0 ) );
        validPluginCounts.put( "maven-deploy-plugin", new Integer( 0 ) );
        validPluginCounts.put( "maven-javadoc-plugin", new Integer( 0 ) );
        validPluginCounts.put( "maven-source-plugin", new Integer( 0 ) );
        Plugin testPlugin = null;
        for ( Iterator it = plugins.iterator(); it.hasNext(); )
        {
            Plugin plugin = (Plugin) it.next();
            String pluginArtifactId = plugin.getArtifactId();
            if ( !validPluginCounts.containsKey( pluginArtifactId ) )
            {
                fail( "Illegal plugin found: " + pluginArtifactId );
            }
            else
            {
                if ( pluginArtifactId.equals( testPluginArtifactId ) )
                {
                    testPlugin = plugin;
                }
                Integer count = (Integer) validPluginCounts.get( pluginArtifactId );
                if ( count.intValue() > 0 )
                {
                    fail( "Multiple copies of plugin: " + pluginArtifactId + " found in POM." );
                }
                else
                {
                    count = new Integer( count.intValue() + 1 );
                    validPluginCounts.put( pluginArtifactId, count );
                }
            }
        }
        List executions = testPlugin.getExecutions();
        assertEquals( 1, executions.size() );
    }
}
