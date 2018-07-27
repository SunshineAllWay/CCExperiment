package org.apache.maven.project;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Extension;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.util.dag.DAG;
import org.codehaus.plexus.util.dag.TopologicalSorter;
import org.codehaus.plexus.util.dag.Vertex;
public class ProjectSorter
{
    private DAG dag;
    private List<MavenProject> sortedProjects;
    private Map<String, MavenProject> projectMap;
    private MavenProject topLevelProject;
    public ProjectSorter( List<MavenProject> projects )
        throws CycleDetectedException, DuplicateProjectException
    {
        dag = new DAG();
        projectMap = new HashMap<String, MavenProject>( projects.size() * 2 );
        Map<String, Map<String, Vertex>> vertexMap = new HashMap<String, Map<String, Vertex>>( projects.size() * 2 );
        for ( MavenProject project : projects )
        {
            String projectId = getId( project );
            MavenProject conflictingProject = projectMap.put( projectId, project );
            if ( conflictingProject != null )
            {
                throw new DuplicateProjectException( projectId, conflictingProject.getFile(), project.getFile(),
                                                     "Project '" + projectId + "' is duplicated in the reactor" );
            }
            String projectKey = ArtifactUtils.versionlessKey( project.getGroupId(), project.getArtifactId() );
            Map<String, Vertex> vertices = vertexMap.get( projectKey );
            if ( vertices == null )
            {
                vertices = new HashMap<String, Vertex>( 2, 1 );
                vertexMap.put( projectKey, vertices );
            }
            vertices.put( project.getVersion(), dag.addVertex( projectId ) );
        }
        for ( Vertex projectVertex : (List<Vertex>) dag.getVerticies() )
        {
            String projectId = projectVertex.getLabel();
            MavenProject project = projectMap.get( projectId );
            for ( Dependency dependency : project.getDependencies() )
            {
                addEdge( projectMap, vertexMap, project, projectVertex, dependency.getGroupId(),
                         dependency.getArtifactId(), dependency.getVersion(), false, false );
            }
            Parent parent = project.getModel().getParent();
            if ( parent != null )
            {
                addEdge( projectMap, vertexMap, null, projectVertex, parent.getGroupId(), parent.getArtifactId(),
                         parent.getVersion(), true, false );
            }
            List<Plugin> buildPlugins = project.getBuildPlugins();
            if ( buildPlugins != null )
            {
                for ( Plugin plugin : buildPlugins )
                {
                    addEdge( projectMap, vertexMap, project, projectVertex, plugin.getGroupId(),
                             plugin.getArtifactId(), plugin.getVersion(), false, true );
                    for ( Dependency dependency : plugin.getDependencies() )
                    {
                        addEdge( projectMap, vertexMap, project, projectVertex, dependency.getGroupId(),
                                 dependency.getArtifactId(), dependency.getVersion(), false, true );
                    }
                }
            }
            List<Extension> buildExtensions = project.getBuildExtensions();
            if ( buildExtensions != null )
            {
                for ( Extension extension : buildExtensions )
                {
                    addEdge( projectMap, vertexMap, project, projectVertex, extension.getGroupId(),
                             extension.getArtifactId(), extension.getVersion(), false, true );
                }
            }
        }
        List<MavenProject> sortedProjects = new ArrayList<MavenProject>( projects.size() );
        List<String> sortedProjectLabels = TopologicalSorter.sort( dag );
        for ( String id : sortedProjectLabels )
        {
            sortedProjects.add( projectMap.get( id ) );
        }
        this.sortedProjects = Collections.unmodifiableList( sortedProjects );
    }
    private void addEdge( Map<String, MavenProject> projectMap, Map<String, Map<String, Vertex>> vertexMap,
                          MavenProject project, Vertex projectVertex, String groupId, String artifactId,
                          String version, boolean force, boolean safe )
        throws CycleDetectedException
    {
        String projectKey = ArtifactUtils.versionlessKey( groupId, artifactId );
        Map<String, Vertex> vertices = vertexMap.get( projectKey );
        if ( vertices != null )
        {
            if ( isSpecificVersion( version ) )
            {
                Vertex vertex = vertices.get( version );
                if ( vertex != null )
                {
                    addEdge( projectVertex, vertex, project, projectMap, force, safe );
                }
            }
            else
            {
                for ( Vertex vertex : vertices.values() )
                {
                    addEdge( projectVertex, vertex, project, projectMap, force, safe );
                }
            }
        }
    }
    private void addEdge( Vertex fromVertex, Vertex toVertex, MavenProject fromProject,
                          Map<String, MavenProject> projectMap, boolean force, boolean safe )
        throws CycleDetectedException
    {
        if ( fromVertex.equals( toVertex ) )
        {
            return;
        }
        if ( fromProject != null )
        {
            MavenProject toProject = projectMap.get( toVertex.getLabel() );
            fromProject.addProjectReference( toProject );
        }
        if ( force && toVertex.getChildren().contains( fromVertex ) )
        {
            dag.removeEdge( toVertex, fromVertex );
        }
        try
        {
            dag.addEdge( fromVertex, toVertex );
        }
        catch ( CycleDetectedException e )
        {
            if ( !safe )
            {
                throw e;
            }
        }
    }
    private boolean isSpecificVersion( String version )
    {
        return !( StringUtils.isEmpty( version ) || version.startsWith( "[" ) || version.startsWith( "(" ) );
    }
    public MavenProject getTopLevelProject()
    {
        if ( topLevelProject == null )
        {
            for ( Iterator<MavenProject> i = sortedProjects.iterator(); i.hasNext() && ( topLevelProject == null ); )
            {
                MavenProject project = i.next();
                if ( project.isExecutionRoot() )
                {
                    topLevelProject = project;
                }
            }
        }
        return topLevelProject;
    }
    public List<MavenProject> getSortedProjects()
    {
        return sortedProjects;
    }
    public boolean hasMultipleProjects()
    {
        return sortedProjects.size() > 1;
    }
    public List<String> getDependents( String id )
    {
        return dag.getParentLabels( id );
    }
    public List<String> getDependencies( String id )
    {
        return dag.getChildLabels( id );
    }
    public static String getId( MavenProject project )
    {
        return ArtifactUtils.key( project.getGroupId(), project.getArtifactId(), project.getVersion() );
    }
    public DAG getDAG()
    {
        return dag;
    }
    public Map<String, MavenProject> getProjectMap()
    {
        return projectMap;
    }
}
