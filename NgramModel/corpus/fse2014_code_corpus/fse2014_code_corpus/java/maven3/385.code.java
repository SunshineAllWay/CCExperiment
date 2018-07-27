package org.apache.maven.lifecycle.internal;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.plugin.version.DefaultPluginVersionRequest;
import org.apache.maven.plugin.version.PluginVersionRequest;
import org.apache.maven.plugin.version.PluginVersionResolutionException;
import org.apache.maven.plugin.version.PluginVersionResolver;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import java.util.HashMap;
import java.util.Map;
@Component( role = LifecyclePluginResolver.class )
public class LifecyclePluginResolver
{
    @Requirement
    private PluginVersionResolver pluginVersionResolver;
    public LifecyclePluginResolver( PluginVersionResolver pluginVersionResolver )
    {
        this.pluginVersionResolver = pluginVersionResolver;
    }
    @SuppressWarnings( { "UnusedDeclaration" } )
    public LifecyclePluginResolver()
    {
    }
    public void resolveMissingPluginVersions( MavenProject project, MavenSession session )
        throws PluginVersionResolutionException
    {
        Map<String, String> versions = new HashMap<String, String>( 64 );
        for ( Plugin plugin : project.getBuildPlugins() )
        {
            if ( plugin.getVersion() == null )
            {
                PluginVersionRequest request =
                    new DefaultPluginVersionRequest( plugin, session.getRepositorySession(),
                                                     project.getRemotePluginRepositories() );
                plugin.setVersion( pluginVersionResolver.resolve( request ).getVersion() );
            }
            versions.put( plugin.getKey(), plugin.getVersion() );
        }
        PluginManagement pluginManagement = project.getPluginManagement();
        if ( pluginManagement != null )
        {
            for ( Plugin plugin : pluginManagement.getPlugins() )
            {
                if ( plugin.getVersion() == null )
                {
                    plugin.setVersion( versions.get( plugin.getKey() ) );
                    if ( plugin.getVersion() == null )
                    {
                        PluginVersionRequest request =
                            new DefaultPluginVersionRequest( plugin, session.getRepositorySession(),
                                                             project.getRemotePluginRepositories() );
                        plugin.setVersion( pluginVersionResolver.resolve( request ).getVersion() );
                    }
                }
            }
        }
    }
}