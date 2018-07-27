package org.apache.maven.lifecycle.internal;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.project.MavenProject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
public class ReactorBuildStatus
{
    private final ProjectDependencyGraph projectDependencyGraph;
    private final Collection<String> blackListedProjects = Collections.synchronizedSet( new HashSet<String>() );
    private volatile boolean halted = false;
    public ReactorBuildStatus( ProjectDependencyGraph projectDependencyGraph )
    {
        this.projectDependencyGraph = projectDependencyGraph;
    }
    public boolean isBlackListed( MavenProject project )
    {
        return blackListedProjects.contains( BuilderCommon.getKey( project ) );
    }
    public void blackList( MavenProject project )
    {
        if ( blackListedProjects.add( BuilderCommon.getKey( project ) ) && projectDependencyGraph != null )
        {
            for ( MavenProject downstreamProject : projectDependencyGraph.getDownstreamProjects( project, true ) )
            {
                blackListedProjects.add( BuilderCommon.getKey( downstreamProject ) );
            }
        }
    }
    public void halt()
    {
        halted = true;
    }
    public boolean isHalted()
    {
        return halted;
    }
    public boolean isHaltedOrBlacklisted( MavenProject mavenProject )
    {
        return isBlackListed( mavenProject ) || isHalted();
    }
}
