package org.apache.maven.plugin;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.repository.RemoteRepository;
public interface MavenPluginManager
{
    PluginDescriptor getPluginDescriptor( Plugin plugin, List<RemoteRepository> repositories, RepositorySystemSession session )
        throws PluginResolutionException, PluginDescriptorParsingException, InvalidPluginDescriptorException;
    MojoDescriptor getMojoDescriptor( Plugin plugin, String goal, List<RemoteRepository> repositories,
                                      RepositorySystemSession session )
        throws MojoNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
        InvalidPluginDescriptorException;
    void checkRequiredMavenVersion( PluginDescriptor pluginDescriptor )
        throws PluginIncompatibleException;
    void setupPluginRealm( PluginDescriptor pluginDescriptor, MavenSession session, ClassLoader parent,
                           List<String> imports, DependencyFilter filter )
        throws PluginResolutionException, PluginContainerException;
    <T> T getConfiguredMojo( Class<T> mojoInterface, MavenSession session, MojoExecution mojoExecution )
        throws PluginConfigurationException, PluginContainerException;
    void releaseMojo( Object mojo, MojoExecution mojoExecution );
}
