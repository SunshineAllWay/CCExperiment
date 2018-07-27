package org.apache.maven.lifecycle.internal.stub;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
public class BuildPluginManagerStub
    implements BuildPluginManager
{
    public PluginDescriptor loadPlugin( Plugin plugin, List<RemoteRepository> repositories, RepositorySystemSession session )
    {
        return null;
    }
    public MojoDescriptor getMojoDescriptor( Plugin plugin, String goal, List<RemoteRepository> repositories,
                                             RepositorySystemSession session )
    {
        return MojoExecutorStub.createMojoDescriptor( plugin.getKey() );
    }
    public ClassRealm getPluginRealm( MavenSession session, PluginDescriptor pluginDescriptor )
    {
        return null;
    }
    public void executeMojo( MavenSession session, MojoExecution execution )
    {
    }
}
