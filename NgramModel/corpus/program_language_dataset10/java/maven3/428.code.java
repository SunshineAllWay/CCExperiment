package org.apache.maven.plugin;
import java.util.List;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
public interface PluginDescriptorCache
{
    interface Key
    {
    }
    Key createKey( Plugin plugin, List<RemoteRepository> repositories, RepositorySystemSession session );
    void put( Key key, PluginDescriptor pluginDescriptor );
    PluginDescriptor get( Key key );
    void flush();
}
