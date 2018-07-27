package org.apache.maven.execution;
import org.apache.maven.project.MavenProject;
import java.util.List;
import junit.framework.TestCase;
public class DefaultMavenExecutionTest
    extends TestCase
{
    public void testCopyDefault()
    {
        MavenExecutionRequest original = new DefaultMavenExecutionRequest();
        MavenExecutionRequest copy = DefaultMavenExecutionRequest.copy( original );
        assertNotNull( copy );
        assertNotSame( copy, original );
    }
    public void testResultWithNullTopologicallySortedProjectsIsEmptyList()
    {
        MavenExecutionResult result = new DefaultMavenExecutionResult();
        result.setTopologicallySortedProjects( null );
        List<MavenProject> projects = result.getTopologicallySortedProjects();
        assertNotNull( projects );
        assertTrue( projects.isEmpty() );
    }
}
