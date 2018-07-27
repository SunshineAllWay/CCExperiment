package org.apache.maven.plugin;
import org.apache.maven.model.Plugin;
public class PluginResolutionException
    extends Exception
{
    private final Plugin plugin;
    public PluginResolutionException( Plugin plugin, Throwable cause )
    {
        super( "Plugin " + plugin.getId() + " or one of its dependencies could not be resolved: " + cause.getMessage(),
               cause );
        this.plugin = plugin;
    }
    public Plugin getPlugin()
    {
        return plugin;
    }
}
