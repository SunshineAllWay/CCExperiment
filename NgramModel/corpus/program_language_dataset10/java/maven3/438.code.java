package org.apache.maven.plugin;
import java.util.List;
import java.util.Map;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.repository.RemoteRepository;
public interface PluginRealmCache
{
    public static class CacheRecord
    {
        public final ClassRealm realm;
        public final List<Artifact> artifacts;
        public CacheRecord( ClassRealm realm, List<Artifact> artifacts )
        {
            this.realm = realm;
            this.artifacts = artifacts;
        }
    }
    interface Key
    {
    }
    Key createKey( Plugin plugin, ClassLoader parentRealm, Map<String, ClassLoader> foreignImports,
                   DependencyFilter dependencyFilter, List<RemoteRepository> repositories,
                   RepositorySystemSession session );
    CacheRecord get( Key key );
    CacheRecord put( Key key, ClassRealm pluginRealm, List<Artifact> pluginArtifacts );
    void flush();
    void register( MavenProject project, CacheRecord record );
}
