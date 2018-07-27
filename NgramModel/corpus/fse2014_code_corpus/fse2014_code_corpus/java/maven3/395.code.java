package org.apache.maven.lifecycle.internal;
import org.apache.maven.project.MavenProject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public final class ProjectIndex
{
    private final Map<String, MavenProject> projects;
    private final Map<String, Integer> indices;
    public ProjectIndex( List<MavenProject> projects )
    {
        this.projects = new HashMap<String, MavenProject>( projects.size() * 2 );
        this.indices = new HashMap<String, Integer>( projects.size() * 2 );
        for ( int i = 0; i < projects.size(); i++ )
        {
            MavenProject project = projects.get( i );
            String key = BuilderCommon.getKey( project );
            this.getProjects().put( key, project );
            this.getIndices().put( key, i );
        }
    }
    public Map<String, MavenProject> getProjects()
    {
        return projects;
    }
    public Map<String, Integer> getIndices()
    {
        return indices;
    }
}
