package org.apache.maven.project.inheritance.t11;
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
public class ProjectInheritanceTest extends AbstractProjectInheritanceTestCase
{
    public void testDependencyManagementOverridesTransitiveDependencyVersion() throws Exception
    {
        File localRepo = getLocalRepositoryPath();
        File pom0 = new File( localRepo, "p0/pom.xml" );
        File pom0Basedir = pom0.getParentFile();
        File pom1 = new File( pom0Basedir, "p1/pom.xml" );
        MavenProject project1 = getProjectWithDependencies( pom1 );
        assertEquals( pom0Basedir, project1.getParent().getBasedir() );
        assertNull( "dependencyManagement has overwritten the scope of a child project",
                     project1.getArtifact().getScope() );
    }
}