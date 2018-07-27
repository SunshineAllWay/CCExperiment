package org.apache.maven.lifecycle.internal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
public class ProjectBuildList
    implements Iterable<ProjectSegment>
{
    private final List<ProjectSegment> items;
    public ProjectBuildList( List<ProjectSegment> items )
    {
        this.items = Collections.unmodifiableList( items );
    }
    public ProjectBuildList getByTaskSegment( TaskSegment taskSegment )
    {
        List<ProjectSegment> currentSegment = new ArrayList<ProjectSegment>();
        for ( ProjectSegment projectBuild : items )
        {
            if ( taskSegment == projectBuild.getTaskSegment() )
            { 
                currentSegment.add( projectBuild );
            }
        }
        return new ProjectBuildList( currentSegment );
    }
    public Map<MavenProject, ProjectSegment> selectSegment( TaskSegment taskSegment )
    {
        Map<MavenProject, ProjectSegment> result = new HashMap<MavenProject, ProjectSegment>();
        for ( ProjectSegment projectBuild : items )
        {
            if ( taskSegment == projectBuild.getTaskSegment() )
            { 
                result.put( projectBuild.getProject(), projectBuild );
            }
        }
        return result;
    }
    public ProjectSegment findByMavenProject( MavenProject mavenProject )
    {
        for ( ProjectSegment projectBuild : items )
        {
            if ( mavenProject.equals( projectBuild.getProject() ) )
            {
                return projectBuild;
            }
        }
        return null;
    }
    public Iterator<ProjectSegment> iterator()
    {
        return items.iterator();
    }
    public void closeAll()
    {
        for ( ProjectSegment item : items )
        {
            MavenSession sessionForThisModule = item.getSession();
            sessionForThisModule.setCurrentProject( null );
        }
    }
    public int size()
    {
        return items.size();
    }
    ProjectSegment get( int index )
    {
        return items.get( index );
    }
    public Set<String> getReactorProjectKeys()
    {
        Set<String> projectKeys = new HashSet<String>( items.size() * 2 );
        for ( ProjectSegment projectBuild : items )
        {
            MavenProject project = projectBuild.getProject();
            String key = ArtifactUtils.key( project.getGroupId(), project.getArtifactId(), project.getVersion() );
            projectKeys.add( key );
        }
        return projectKeys;
    }
    public boolean isEmpty()
    {
        return items.isEmpty();
    }
}
