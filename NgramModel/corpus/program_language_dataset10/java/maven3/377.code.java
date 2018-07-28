package org.apache.maven.lifecycle.internal;
import org.apache.maven.project.MavenProject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
public class DependencyContext
{
    private static final Collection<?> UNRESOLVED = Arrays.asList();
    private final MavenProject project;
    private final Collection<String> scopesToCollectForCurrentProject;
    private final Collection<String> scopesToResolveForCurrentProject;
    private final Collection<String> scopesToCollectForAggregatedProjects;
    private final Collection<String> scopesToResolveForAggregatedProjects;
    private volatile Collection<?> lastDependencyArtifacts = UNRESOLVED;
    private volatile int lastDependencyArtifactCount = -1;
    public DependencyContext( MavenProject project, Collection<String> scopesToCollect,
                              Collection<String> scopesToResolve )
    {
        this.project = project;
        scopesToCollectForCurrentProject = scopesToCollect;
        scopesToResolveForCurrentProject = scopesToResolve;
        scopesToCollectForAggregatedProjects = Collections.synchronizedSet( new TreeSet<String>() );
        scopesToResolveForAggregatedProjects = Collections.synchronizedSet( new TreeSet<String>() );
    }
    public MavenProject getProject()
    {
        return project;
    }
    public Collection<String> getScopesToCollectForCurrentProject()
    {
        return scopesToCollectForCurrentProject;
    }
    public Collection<String> getScopesToResolveForCurrentProject()
    {
        return scopesToResolveForCurrentProject;
    }
    public Collection<String> getScopesToCollectForAggregatedProjects()
    {
        return scopesToCollectForAggregatedProjects;
    }
    public Collection<String> getScopesToResolveForAggregatedProjects()
    {
        return scopesToResolveForAggregatedProjects;
    }
    public boolean isResolutionRequiredForCurrentProject()
    {
        if ( lastDependencyArtifacts != project.getDependencyArtifacts()
            || ( lastDependencyArtifacts != null && lastDependencyArtifactCount != lastDependencyArtifacts.size() ) )
        {
            return true;
        }
        return false;
    }
    public boolean isResolutionRequiredForAggregatedProjects( Collection<String> scopesToCollect,
                                                              Collection<String> scopesToResolve )
    {
        boolean required =
            scopesToCollectForAggregatedProjects.addAll( scopesToCollect )
                || scopesToResolveForAggregatedProjects.addAll( scopesToResolve );
        return required;
    }
    public void synchronizeWithProjectState()
    {
        lastDependencyArtifacts = project.getDependencyArtifacts();
        lastDependencyArtifactCount = ( lastDependencyArtifacts != null ) ? lastDependencyArtifacts.size() : 0;
    }
}
