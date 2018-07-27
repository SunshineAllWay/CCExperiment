package org.apache.maven.project;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.graph.DependencyFilter;
public interface DependencyResolutionRequest
{
    MavenProject getMavenProject();
    DependencyResolutionRequest setMavenProject( MavenProject project );
    DependencyFilter getResolutionFilter();
    DependencyResolutionRequest setResolutionFilter( DependencyFilter filter );
    RepositorySystemSession getRepositorySession();
    DependencyResolutionRequest setRepositorySession( RepositorySystemSession repositorySession );
}
