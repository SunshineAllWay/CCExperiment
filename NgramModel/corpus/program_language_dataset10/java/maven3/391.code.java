package org.apache.maven.lifecycle.internal;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.prefix.DefaultPluginPrefixRequest;
import org.apache.maven.plugin.prefix.NoPluginFoundForPrefixException;
import org.apache.maven.plugin.prefix.PluginPrefixRequest;
import org.apache.maven.plugin.prefix.PluginPrefixResolver;
import org.apache.maven.plugin.prefix.PluginPrefixResult;
import org.apache.maven.plugin.version.DefaultPluginVersionRequest;
import org.apache.maven.plugin.version.PluginVersionRequest;
import org.apache.maven.plugin.version.PluginVersionResolutionException;
import org.apache.maven.plugin.version.PluginVersionResolver;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;
@Component( role = MojoDescriptorCreator.class )
public class MojoDescriptorCreator
{
    @Requirement
    private Logger logger;
    @Requirement
    private PluginVersionResolver pluginVersionResolver;
    @Requirement
    private BuildPluginManager pluginManager;
    @Requirement
    private PluginPrefixResolver pluginPrefixResolver;
    @Requirement
    private LifecyclePluginResolver lifecyclePluginResolver;
    @SuppressWarnings( { "UnusedDeclaration" } )
    public MojoDescriptorCreator()
    {
    }
    public MojoDescriptorCreator( PluginVersionResolver pluginVersionResolver, BuildPluginManager pluginManager,
                                  PluginPrefixResolver pluginPrefixResolver,
                                  LifecyclePluginResolver lifecyclePluginResolver )
    {
        this.pluginVersionResolver = pluginVersionResolver;
        this.pluginManager = pluginManager;
        this.pluginPrefixResolver = pluginPrefixResolver;
        this.lifecyclePluginResolver = lifecyclePluginResolver;
    }
    private Plugin findPlugin( String groupId, String artifactId, Collection<Plugin> plugins )
    {
        for ( Plugin plugin : plugins )
        {
            if ( artifactId.equals( plugin.getArtifactId() ) && groupId.equals( plugin.getGroupId() ) )
            {
                return plugin;
            }
        }
        return null;
    }
    public static Xpp3Dom convert( MojoDescriptor mojoDescriptor )
    {
        Xpp3Dom dom = new Xpp3Dom( "configuration" );
        PlexusConfiguration c = mojoDescriptor.getMojoConfiguration();
        PlexusConfiguration[] ces = c.getChildren();
        if ( ces != null )
        {
            for ( PlexusConfiguration ce : ces )
            {
                String value = ce.getValue( null );
                String defaultValue = ce.getAttribute( "default-value", null );
                if ( value != null || defaultValue != null )
                {
                    Xpp3Dom e = new Xpp3Dom( ce.getName() );
                    e.setValue( value );
                    if ( defaultValue != null )
                    {
                        e.setAttribute( "default-value", defaultValue );
                    }
                    dom.addChild( e );
                }
            }
        }
        return dom;
    }
    public MojoDescriptor getMojoDescriptor( String task, MavenSession session, MavenProject project )
        throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
        MojoNotFoundException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
        PluginVersionResolutionException
    {
        String goal = null;
        Plugin plugin = null;
        StringTokenizer tok = new StringTokenizer( task, ":" );
        int numTokens = tok.countTokens();
        if ( numTokens >= 4 )
        {
            plugin = new Plugin();
            plugin.setGroupId( tok.nextToken() );
            plugin.setArtifactId( tok.nextToken() );
            plugin.setVersion( tok.nextToken() );
            goal = tok.nextToken();
            while ( tok.hasMoreTokens() )
            {
                goal += ":" + tok.nextToken();
            }
        }
        else if ( numTokens == 3 )
        {
            plugin = new Plugin();
            plugin.setGroupId( tok.nextToken() );
            plugin.setArtifactId( tok.nextToken() );
            goal = tok.nextToken();
        }
        else if ( numTokens <= 2 )
        {
            String prefix = tok.nextToken();
            if ( numTokens == 2 )
            {
                goal = tok.nextToken();
            }
            else
            {
                goal = "";
            }
            plugin = findPluginForPrefix( prefix, session );
        }
        injectPluginDeclarationFromProject( plugin, project );
        if ( plugin.getVersion() == null )
        {
            resolvePluginVersion( plugin, session, project );
        }
        return pluginManager.getMojoDescriptor( plugin, goal, project.getRemotePluginRepositories(),
                                                session.getRepositorySession() );
    }
    public Plugin findPluginForPrefix( String prefix, MavenSession session )
        throws NoPluginFoundForPrefixException
    {
        if ( session.getCurrentProject() != null )
        {
            try
            {
                lifecyclePluginResolver.resolveMissingPluginVersions( session.getCurrentProject(), session );
            }
            catch ( PluginVersionResolutionException e )
            {
                logger.debug( e.getMessage(), e );
            }
        }
        PluginPrefixRequest prefixRequest = new DefaultPluginPrefixRequest( prefix, session );
        PluginPrefixResult prefixResult = pluginPrefixResolver.resolve( prefixRequest );
        Plugin plugin = new Plugin();
        plugin.setGroupId( prefixResult.getGroupId() );
        plugin.setArtifactId( prefixResult.getArtifactId() );
        return plugin;
    }
    private void resolvePluginVersion( Plugin plugin, MavenSession session, MavenProject project )
        throws PluginVersionResolutionException
    {
        PluginVersionRequest versionRequest =
            new DefaultPluginVersionRequest( plugin, session.getRepositorySession(),
                                             project.getRemotePluginRepositories() );
        plugin.setVersion( pluginVersionResolver.resolve( versionRequest ).getVersion() );
    }
    private void injectPluginDeclarationFromProject( Plugin plugin, MavenProject project )
    {
        Plugin pluginInPom = findPlugin( plugin, project.getBuildPlugins() );
        if ( pluginInPom == null && project.getPluginManagement() != null )
        {
            pluginInPom = findPlugin( plugin, project.getPluginManagement().getPlugins() );
        }
        if ( pluginInPom != null )
        {
            if ( plugin.getVersion() == null )
            {
                plugin.setVersion( pluginInPom.getVersion() );
            }
            plugin.setDependencies( new ArrayList<Dependency>( pluginInPom.getDependencies() ) );
        }
    }
    private Plugin findPlugin( Plugin plugin, Collection<Plugin> plugins )
    {
        return findPlugin( plugin.getGroupId(), plugin.getArtifactId(), plugins );
    }
}
