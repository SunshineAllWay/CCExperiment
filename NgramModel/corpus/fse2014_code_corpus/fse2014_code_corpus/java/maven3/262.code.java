package org.apache.maven;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.project.MavenProject;
class FilteredProjectDependencyGraph
    implements ProjectDependencyGraph
{
    private ProjectDependencyGraph projectDependencyGraph;
    private Map<MavenProject, ?> whiteList;
    private List<MavenProject> sortedProjects;
    public FilteredProjectDependencyGraph( ProjectDependencyGraph projectDependencyGraph,
                                           Collection<? extends MavenProject> whiteList )
    {
        if ( projectDependencyGraph == null )
        {
            throw new IllegalArgumentException( "project dependency graph missing" );
        }
        this.projectDependencyGraph = projectDependencyGraph;
        this.whiteList = new IdentityHashMap<MavenProject, Object>();
        for ( MavenProject project : whiteList )
        {
            this.whiteList.put( project, null );
        }
    }
    public List<MavenProject> getSortedProjects()
    {
        if ( sortedProjects == null )
        {
            sortedProjects = applyFilter( projectDependencyGraph.getSortedProjects() );
        }
        return new ArrayList<MavenProject>( sortedProjects );
    }
    public List<MavenProject> getDownstreamProjects( MavenProject project, boolean transitive )
    {
        return applyFilter( projectDependencyGraph.getDownstreamProjects( project, transitive ) );
    }
    public List<MavenProject> getUpstreamProjects( MavenProject project, boolean transitive )
    {
        return applyFilter( projectDependencyGraph.getUpstreamProjects( project, transitive ) );
    }
    private List<MavenProject> applyFilter( Collection<? extends MavenProject> projects )
    {
        List<MavenProject> filtered = new ArrayList<MavenProject>( projects.size() );
        for ( MavenProject project : projects )
        {
            if ( whiteList.containsKey( project ) )
            {
                filtered.add( project );
            }
        }
        return filtered;
    }
    @Override
    public String toString()
    {
        return getSortedProjects().toString();
    }
}
