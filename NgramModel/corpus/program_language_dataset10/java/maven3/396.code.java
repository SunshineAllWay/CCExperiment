package org.apache.maven.lifecycle.internal;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.project.MavenProject;
import java.util.List;
public final class ProjectSegment
{
    private final MavenProject project;
    private final TaskSegment taskSegment;
    private final MavenSession session;
    private final List<MavenProject> nonTransitiveUpstreamProjects;
    private final List<MavenProject> transitiveUpstreamProjects;
    public ProjectSegment( MavenProject project, TaskSegment taskSegment, MavenSession copiedSession )
    {
        this.project = project;
        this.taskSegment = taskSegment;
        this.session = copiedSession;
        final ProjectDependencyGraph dependencyGraph = getSession().getProjectDependencyGraph();
        nonTransitiveUpstreamProjects = dependencyGraph.getUpstreamProjects( getProject(), false );
        transitiveUpstreamProjects = dependencyGraph.getUpstreamProjects( getProject(), true );
    }
    public MavenSession getSession()
    {
        return session;
    }
    public MavenProject getProject()
    {
        return project;
    }
    public TaskSegment getTaskSegment()
    {
        return taskSegment;
    }
    public List<MavenProject> getImmediateUpstreamProjects()
    {
        return nonTransitiveUpstreamProjects;
    }
    public List<MavenProject> getTransitiveUpstreamProjects()
    {
        return transitiveUpstreamProjects;
    }
    @Override
    public String toString()
    {
        return getProject().getId() + " -> " + getTaskSegment();
    }
}
