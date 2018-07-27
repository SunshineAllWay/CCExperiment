package org.apache.maven.plugin;
import java.util.List;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.repository.RemoteRepository;
public interface PluginArtifactsCache
{
    interface Key
    {
    }
    public static class CacheRecord
    {
        public final List<Artifact> artifacts;
        public final PluginResolutionException exception; 
        public CacheRecord( List<Artifact> artifacts )
        {
            this.artifacts = artifacts;
            this.exception = null;
        }
        public CacheRecord( PluginResolutionException exception )
        {
            this.artifacts = null;
            this.exception = exception;
        }
    }
    Key createKey( Plugin plugin, DependencyFilter extensionFilter, List<RemoteRepository> repositories,
                   RepositorySystemSession session );
    CacheRecord get( Key key ) throws PluginResolutionException;
    CacheRecord put( Key key, List<Artifact> pluginArtifacts );
    CacheRecord put( Key key, PluginResolutionException e );
    void flush();
    void register( MavenProject project, CacheRecord record );
}
