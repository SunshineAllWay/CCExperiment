package org.apache.maven;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectSorter;
class DefaultProjectDependencyGraph
    implements ProjectDependencyGraph
{
    private ProjectSorter sorter;
    public DefaultProjectDependencyGraph( ProjectSorter sorter )
    {
        if ( sorter == null )
        {
            throw new IllegalArgumentException( "project sorter missing" );
        }
        this.sorter = sorter;
    }
    public List<MavenProject> getSortedProjects()
    {
        return new ArrayList<MavenProject>( sorter.getSortedProjects() );
    }
    public List<MavenProject> getDownstreamProjects( MavenProject project, boolean transitive )
    {
        if ( project == null )
        {
            throw new IllegalArgumentException( "project missing" );
        }
        Collection<String> projectIds = new HashSet<String>();
        getDownstreamProjects( ProjectSorter.getId( project ), projectIds, transitive );
        return getProjects( projectIds );
    }
    private void getDownstreamProjects( String projectId, Collection<String> projectIds, boolean transitive )
    {
        for ( String id : sorter.getDependents( projectId ) )
        {
            if ( projectIds.add( id ) )
            {
                if ( transitive )
                {
                    getDownstreamProjects( id, projectIds, transitive );
                }
            }
        }
    }
    public List<MavenProject> getUpstreamProjects( MavenProject project, boolean transitive )
    {
        if ( project == null )
        {
            throw new IllegalArgumentException( "project missing" );
        }
        Collection<String> projectIds = new HashSet<String>();
        getUpstreamProjects( ProjectSorter.getId( project ), projectIds, transitive );
        return getProjects( projectIds );
    }
    private void getUpstreamProjects( String projectId, Collection<String> projectIds, boolean transitive )
    {
        for ( String id : sorter.getDependencies( projectId ) )
        {
            if ( projectIds.add( id ) )
            {
                if ( transitive )
                {
                    getUpstreamProjects( id, projectIds, transitive );
                }
            }
        }
    }
    private List<MavenProject> getProjects( Collection<String> projectIds )
    {
        List<MavenProject> projects = new ArrayList<MavenProject>();
        for ( MavenProject p : sorter.getSortedProjects() )
        {
            if ( projectIds.contains( ProjectSorter.getId( p ) ) )
            {
                projects.add( p );
            }
        }
        return projects;
    }
    @Override
    public String toString()
    {
        return sorter.getSortedProjects().toString();
    }
}
