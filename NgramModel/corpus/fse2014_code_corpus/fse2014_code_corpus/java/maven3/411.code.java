package org.apache.maven.plugin;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
public class CycleDetectedInPluginGraphException
    extends Exception
{
    private final Plugin plugin;
    public CycleDetectedInPluginGraphException( Plugin plugin, CycleDetectedInComponentGraphException e )
    {
        super( "A cycle was detected in the component graph of the plugin: " + plugin.getArtifactId() ); 
        this.plugin = plugin;
    }
    public Plugin getPlugin()
    {
        return plugin;
    }
}
