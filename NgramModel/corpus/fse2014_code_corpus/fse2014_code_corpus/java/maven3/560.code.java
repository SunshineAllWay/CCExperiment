package org.apache.maven.lifecycle.internal;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.lifecycle.LifecycleNotFoundException;
import org.apache.maven.lifecycle.LifecyclePhaseNotFoundException;
import org.apache.maven.lifecycle.internal.stub.ProjectDependencyGraphStub;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.prefix.NoPluginFoundForPrefixException;
import org.apache.maven.plugin.version.PluginVersionResolutionException;
import org.apache.maven.project.MavenProject;
import java.util.List;
import static org.apache.maven.lifecycle.internal.stub.ProjectDependencyGraphStub.*;
public class ConcurrencyDependencyGraphTest
    extends junit.framework.TestCase
{
    public void testConcurrencyGraphPrimaryVersion()
        throws InvalidPluginDescriptorException, PluginVersionResolutionException, PluginDescriptorParsingException,
        NoPluginFoundForPrefixException, MojoNotFoundException, PluginNotFoundException, PluginResolutionException,
        LifecyclePhaseNotFoundException, LifecycleNotFoundException
    {
        ProjectDependencyGraph dependencyGraph = new ProjectDependencyGraphStub();
        final MavenSession session = ProjectDependencyGraphStub.getMavenSession();
        ConcurrencyDependencyGraph graph =
            new ConcurrencyDependencyGraph( getProjectBuildList( session ), dependencyGraph );
        final List<MavenProject> projectBuilds = graph.getRootSchedulableBuilds();
        assertEquals( 1, projectBuilds.size() );
        assertEquals( A, projectBuilds.get( 0 ) );
        final List<MavenProject> subsequent = graph.markAsFinished( A );
        assertEquals( 2, subsequent.size() );
        assertEquals( ProjectDependencyGraphStub.B, subsequent.get( 0 ) );
        assertEquals( C, subsequent.get( 1 ) );
        final List<MavenProject> bDescendants = graph.markAsFinished( B );
        assertEquals( 1, bDescendants.size() );
        assertEquals( Y, bDescendants.get( 0 ) );
        final List<MavenProject> cDescendants = graph.markAsFinished( C );
        assertEquals( 2, cDescendants.size() );
        assertEquals( X, cDescendants.get( 0 ) );
        assertEquals( Z, cDescendants.get( 1 ) );
    }
    public void testConcurrencyGraphDifferentCompletionOrder()
        throws InvalidPluginDescriptorException, PluginVersionResolutionException, PluginDescriptorParsingException,
        NoPluginFoundForPrefixException, MojoNotFoundException, PluginNotFoundException, PluginResolutionException,
        LifecyclePhaseNotFoundException, LifecycleNotFoundException
    {
        ProjectDependencyGraph dependencyGraph = new ProjectDependencyGraphStub();
        final MavenSession session = ProjectDependencyGraphStub.getMavenSession();
        ConcurrencyDependencyGraph graph =
            new ConcurrencyDependencyGraph( getProjectBuildList( session ), dependencyGraph );
        graph.markAsFinished( A );
        final List<MavenProject> cDescendants = graph.markAsFinished( C );
        assertEquals( 1, cDescendants.size() );
        assertEquals( Z, cDescendants.get( 0 ) );
        final List<MavenProject> bDescendants = graph.markAsFinished( B );
        assertEquals( 2, bDescendants.size() );
        assertEquals( X, bDescendants.get( 0 ) );
        assertEquals( Y, bDescendants.get( 1 ) );
    }
}
