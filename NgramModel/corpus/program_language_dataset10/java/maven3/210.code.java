package org.apache.maven.project;
import java.io.File;
import java.util.Iterator;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.ArtifactResolver;
public class ProjectClasspathTest
    extends AbstractMavenProjectTestCase
{
    static final String dir = "projects/scope/";
    public void setUp()
        throws Exception
    {
        ArtifactResolver resolver = lookup( ArtifactResolver.class, "classpath" );
        DefaultArtifactDescriptorReader pomReader = (DefaultArtifactDescriptorReader)lookup(ArtifactDescriptorReader.class);
        pomReader.setArtifactResolver( resolver );
        projectBuilder = lookup( ProjectBuilder.class, "classpath" );
        getContainer().addComponent( projectBuilder, ProjectBuilder.class, "default" );
        repositorySystem = lookup( RepositorySystem.class );        
    }
    @Override
    protected String getCustomConfigurationName()
    {
        return null;
    }
    public void testProjectClasspath()
        throws Exception
    {
        File f = getFileForClasspathResource( dir + "project-with-scoped-dependencies.xml" );
        MavenProject project = getProjectWithDependencies( f );
        Artifact artifact;
        assertNotNull( "Test project can't be null!", project );
        checkArtifactIdScope( project, "provided", "provided" );
        checkArtifactIdScope( project, "test", "test" );
        checkArtifactIdScope( project, "compile", "compile" );
        checkArtifactIdScope( project, "runtime", "runtime" );
        checkArtifactIdScope( project, "default", "compile" );
        artifact = getArtifact( project, "maven-test-test", "scope-provided" );
        assertNull( "Check no provided dependencies are transitive", artifact );
        artifact = getArtifact( project, "maven-test-test", "scope-test" );
        assertNull( "Check no test dependencies are transitive", artifact );
        artifact = getArtifact( project, "maven-test-test", "scope-compile" );
        assertNotNull( artifact );
        System.out.println( "a = " + artifact );
        System.out.println( "b = " + artifact.getScope() );
        assertEquals( "Check scope", "test", artifact.getScope() );
        artifact = getArtifact( project, "maven-test-test", "scope-default" );
        assertEquals( "Check scope", "test", artifact.getScope() );
        artifact = getArtifact( project, "maven-test-test", "scope-runtime" );
        assertEquals( "Check scope", "test", artifact.getScope() );
        checkGroupIdScope( project, "provided", "maven-test-provided" );
        artifact = getArtifact( project, "maven-test-provided", "scope-runtime" );
        assertEquals( "Check scope", "provided", artifact.getScope() );
        checkGroupIdScope( project, "runtime", "maven-test-runtime" );
        artifact = getArtifact( project, "maven-test-runtime", "scope-runtime" );
        assertEquals( "Check scope", "runtime", artifact.getScope() );
        checkGroupIdScope( project, "compile", "maven-test-compile" );
        artifact = getArtifact( project, "maven-test-compile", "scope-runtime" );
        assertEquals( "Check scope", "runtime", artifact.getScope() );
        checkGroupIdScope( project, "compile", "maven-test-default" );
        artifact = getArtifact( project, "maven-test-default", "scope-runtime" );
        assertEquals( "Check scope", "runtime", artifact.getScope() );
    }
    private void checkGroupIdScope( MavenProject project, String scopeValue, String groupId )
    {
        Artifact artifact;
        artifact = getArtifact( project, groupId, "scope-compile" );
        assertEquals( "Check scope", scopeValue, artifact.getScope() );
        artifact = getArtifact( project, groupId, "scope-test" );
        assertNull( "Check test dependency is not transitive", artifact );
        artifact = getArtifact( project, groupId, "scope-provided" );
        assertNull( "Check provided dependency is not transitive", artifact );
        artifact = getArtifact( project, groupId, "scope-default" );
        assertEquals( "Check scope", scopeValue, artifact.getScope() );
    }
    private void checkArtifactIdScope( MavenProject project, String scope, String scopeValue )
    {
        String artifactId = "scope-" + scope;
        Artifact artifact = getArtifact( project, "maven-test", artifactId );
        assertNotNull( artifact );
        assertEquals( "Check scope", scopeValue, artifact.getScope() );
    }
    private Artifact getArtifact( MavenProject project, String groupId, String artifactId )
    {
        System.out.println( "[ Looking for " + groupId + ":" + artifactId + " ]" );
        for ( Iterator<Artifact> i = project.getArtifacts().iterator(); i.hasNext(); )
        {
            Artifact a = i.next();
            System.out.println( a.toString() );
            if ( artifactId.equals( a.getArtifactId() ) && a.getGroupId().equals( groupId ) )
            {
                System.out.println( "RETURN" );
                return a;
            }
        }
        System.out.println( "Return null" );
        return null;
    }
}
