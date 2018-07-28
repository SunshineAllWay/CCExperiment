package org.apache.maven.project;
public interface ProjectDependenciesResolver
{
    DependencyResolutionResult resolve( DependencyResolutionRequest request )
        throws DependencyResolutionException;
}
