package org.apache.maven.lifecycle.internal.stub;
import junit.framework.TestCase;
import org.apache.maven.project.MavenProject;
import java.util.List;
public class ProjectDependencyGraphStubTest
    extends TestCase
{
    public void testADependencies()
    {
        ProjectDependencyGraphStub stub = new ProjectDependencyGraphStub();
        final List<MavenProject> mavenProjects = stub.getUpstreamProjects( ProjectDependencyGraphStub.A, false );
        assertEquals( 0, mavenProjects.size() );
    }
    public void testBDepenencies( ProjectDependencyGraphStub stub )
    {
        final List<MavenProject> bProjects = stub.getUpstreamProjects( ProjectDependencyGraphStub.B, false );
        assertEquals( 1, bProjects.size() );
        assertTrue( bProjects.contains( ProjectDependencyGraphStub.A ) );
    }
    public void testCDepenencies( ProjectDependencyGraphStub stub )
    {
        final List<MavenProject> cProjects = stub.getUpstreamProjects( ProjectDependencyGraphStub.C, false );
        assertEquals( 1, cProjects.size() );
        assertTrue( cProjects.contains( ProjectDependencyGraphStub.C ) );
    }
    public void testXDepenencies( ProjectDependencyGraphStub stub )
    {
        final List<MavenProject> cProjects = stub.getUpstreamProjects( ProjectDependencyGraphStub.X, false );
        assertEquals( 2, cProjects.size() );
        assertTrue( cProjects.contains( ProjectDependencyGraphStub.C ) );
        assertTrue( cProjects.contains( ProjectDependencyGraphStub.B ) );
    }
}
