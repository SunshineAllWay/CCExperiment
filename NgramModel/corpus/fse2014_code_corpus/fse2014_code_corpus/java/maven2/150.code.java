package org.apache.maven.plugin;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.GroupRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
public class DefaultPluginMappingManager
    extends AbstractLogEnabled
    implements PluginMappingManager
{
    protected RepositoryMetadataManager repositoryMetadataManager;
    private Map pluginDefinitionsByPrefix = new HashMap();
    public org.apache.maven.model.Plugin getByPrefix( String pluginPrefix, List groupIds, List pluginRepositories,
                                                      ArtifactRepository localRepository )
    {
        if ( !pluginDefinitionsByPrefix.containsKey( pluginPrefix ) )
        {
            getLogger().info( "Searching repository for plugin with prefix: \'" + pluginPrefix + "\'." );
            loadPluginMappings( groupIds, pluginRepositories, localRepository );
        }
        return (org.apache.maven.model.Plugin) pluginDefinitionsByPrefix.get( pluginPrefix );
    }
    private void loadPluginMappings( List groupIds, List pluginRepositories, ArtifactRepository localRepository )
    {
        List pluginGroupIds = new ArrayList( groupIds );
        if ( !pluginGroupIds.contains( "org.apache.maven.plugins" ) )
        {
            pluginGroupIds.add( "org.apache.maven.plugins" );
        }
        if ( !pluginGroupIds.contains( "org.codehaus.mojo" ) )
        {
            pluginGroupIds.add( "org.codehaus.mojo" );
        }
        for ( Iterator it = pluginGroupIds.iterator(); it.hasNext(); )
        {
            String groupId = (String) it.next();
            getLogger().debug( "Loading plugin prefixes from group: " + groupId );
            try
            {
                loadPluginMappings( groupId, pluginRepositories, localRepository );
            }
            catch ( RepositoryMetadataResolutionException e )
            {
                getLogger().warn( "Cannot resolve plugin-mapping metadata for groupId: " + groupId + " - IGNORING." );
                getLogger().debug( "Error resolving plugin-mapping metadata for groupId: " + groupId + ".", e );
            }
        }
    }
    private void loadPluginMappings( String groupId, List pluginRepositories, ArtifactRepository localRepository )
        throws RepositoryMetadataResolutionException
    {
        RepositoryMetadata metadata = new GroupRepositoryMetadata( groupId );
        repositoryMetadataManager.resolve( metadata, pluginRepositories, localRepository );
        Metadata repoMetadata = metadata.getMetadata();
        if ( repoMetadata != null )
        {
            for ( Iterator pluginIterator = repoMetadata.getPlugins().iterator(); pluginIterator.hasNext(); )
            {
                Plugin mapping = (Plugin) pluginIterator.next();
                String prefix = mapping.getPrefix();
                if ( !pluginDefinitionsByPrefix.containsKey( prefix ) )
                {
                    String artifactId = mapping.getArtifactId();
                    org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();
                    plugin.setGroupId( metadata.getGroupId() );
                    plugin.setArtifactId( artifactId );
                    pluginDefinitionsByPrefix.put( prefix, plugin );
                }
            }
        }
    }
}
