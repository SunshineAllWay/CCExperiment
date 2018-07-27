package org.apache.maven;
import java.util.Collection;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
public interface ProjectDependenciesResolver
{
    Set<Artifact> resolve( MavenProject project, Collection<String> scopesToResolve, MavenSession session )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    Set<Artifact> resolve( MavenProject project, Collection<String> scopesToCollect,
                           Collection<String> scopesToResolve, MavenSession session )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    Set<Artifact> resolve( MavenProject project, Collection<String> scopesToCollect,
                           Collection<String> scopesToResolve, MavenSession session, Set<Artifact> ignoreableArtifacts )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    Set<Artifact> resolve( Collection<? extends MavenProject> projects, Collection<String> scopes, MavenSession session )
        throws ArtifactResolutionException, ArtifactNotFoundException;
}
