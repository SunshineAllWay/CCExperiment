package org.apache.maven.lifecycle.internal;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.project.MavenProject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
public class ConcurrencyDependencyGraph
{
    private final ProjectBuildList projectBuilds;
    private final ProjectDependencyGraph projectDependencyGraph;
    private final HashSet<MavenProject> finishedProjects = new HashSet<MavenProject>();
    public ConcurrencyDependencyGraph( ProjectBuildList projectBuilds, ProjectDependencyGraph projectDependencyGraph )
    {
        this.projectDependencyGraph = projectDependencyGraph;
        this.projectBuilds = projectBuilds;
    }
    public int getNumberOfBuilds()
    {
        return projectBuilds.size();
    }
    public List<MavenProject> getRootSchedulableBuilds()
    {
        List<MavenProject> result = new ArrayList<MavenProject>();
        for ( ProjectSegment projectBuild : projectBuilds )
        {
            if ( projectDependencyGraph.getUpstreamProjects( projectBuild.getProject(), false ).size() == 0 )
            {
                result.add( projectBuild.getProject() );
            }
        }
        return result;
    }
    public List<MavenProject> markAsFinished( MavenProject mavenProject )
    {
        finishedProjects.add( mavenProject );
        return getSchedulableNewProcesses( mavenProject );
    }
    private List<MavenProject> getSchedulableNewProcesses( MavenProject finishedProject )
    {
        List<MavenProject> result = new ArrayList<MavenProject>();
        for ( MavenProject dependentProject : projectDependencyGraph.getDownstreamProjects( finishedProject, false ) )
        {
            final List<MavenProject> upstreamProjects =
                projectDependencyGraph.getUpstreamProjects( dependentProject, false );
            if ( finishedProjects.containsAll( upstreamProjects ) )
            {
                result.add( dependentProject );
            }
        }
        return result;
    }
    public ProjectBuildList getProjectBuilds()
    {
        return projectBuilds;
    }
}