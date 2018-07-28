package org.apache.maven.plugin;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
public interface BuildPluginManager
{
    PluginDescriptor loadPlugin( Plugin plugin, List<RemoteRepository> repositories, RepositorySystemSession session )
        throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
        InvalidPluginDescriptorException;
    MojoDescriptor getMojoDescriptor( Plugin plugin, String goal, List<RemoteRepository> repositories,
                                      RepositorySystemSession session )
        throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
        MojoNotFoundException, InvalidPluginDescriptorException;
    ClassRealm getPluginRealm( MavenSession session, PluginDescriptor pluginDescriptor )
        throws PluginResolutionException, PluginManagerException;
    void executeMojo( MavenSession session, MojoExecution execution )
        throws MojoFailureException, MojoExecutionException, PluginConfigurationException, PluginManagerException;
}
