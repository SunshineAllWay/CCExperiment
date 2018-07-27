package org.apache.maven.lifecycle.internal;
import org.apache.maven.execution.MavenExecutionResult;
public class ReactorContext
{
    private final MavenExecutionResult result;
    private final ProjectIndex projectIndex;
    private final ClassLoader originalContextClassLoader;
    private final ReactorBuildStatus reactorBuildStatus;
    public ReactorContext( MavenExecutionResult result, ProjectIndex projectIndex,
                           ClassLoader originalContextClassLoader, ReactorBuildStatus reactorBuildStatus )
    {
        this.result = result;
        this.projectIndex = projectIndex;
        this.originalContextClassLoader = originalContextClassLoader;
        this.reactorBuildStatus = reactorBuildStatus;
    }
    public ReactorBuildStatus getReactorBuildStatus()
    {
        return reactorBuildStatus;
    }
    public MavenExecutionResult getResult()
    {
        return result;
    }
    public ProjectIndex getProjectIndex()
    {
        return projectIndex;
    }
    public ClassLoader getOriginalContextClassLoader()
    {
        return originalContextClassLoader;
    }
}
