package org.apache.maven.plugin.internal;
import java.util.List;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.PluginResolutionException;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;
public interface PluginDependenciesResolver
{
    public Artifact resolve( Plugin plugin, List<RemoteRepository> repositories, RepositorySystemSession session )
        throws PluginResolutionException;
    DependencyNode resolve( Plugin plugin, Artifact pluginArtifact, DependencyFilter dependencyFilter,
                            List<RemoteRepository> repositories, RepositorySystemSession session )
        throws PluginResolutionException;
}
