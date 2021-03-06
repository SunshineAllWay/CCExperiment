package org.apache.maven.plugin;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryEvent;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryListener;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
public class MavenPluginCollector
    extends AbstractLogEnabled
    implements ComponentDiscoveryListener
{
    private Set pluginsInProcess = new HashSet();
    private Map pluginDescriptors = new HashMap();
    private Map pluginIdsByPrefix = new HashMap();
    public void componentDiscovered( ComponentDiscoveryEvent event )
    {
        ComponentSetDescriptor componentSetDescriptor = event.getComponentSetDescriptor();
        if ( componentSetDescriptor instanceof PluginDescriptor )
        {
            PluginDescriptor pluginDescriptor = (PluginDescriptor) componentSetDescriptor;
            String key = PluginUtils.constructVersionedKey( pluginDescriptor );
            if ( !pluginsInProcess.contains( key ) )
            {
                pluginsInProcess.add( key );
                pluginDescriptors.put( key, pluginDescriptor );
                if ( !pluginIdsByPrefix.containsKey( pluginDescriptor.getGoalPrefix() ) )
                {
                    pluginIdsByPrefix.put( pluginDescriptor.getGoalPrefix(), pluginDescriptor );
                }
            }
        }
    }
    public PluginDescriptor getPluginDescriptor( Plugin plugin )
    {
        String key = PluginUtils.constructVersionedKey( plugin );
        return (PluginDescriptor) pluginDescriptors.get( key );
    }
    public boolean isPluginInstalled( Plugin plugin )
    {
        String key = PluginUtils.constructVersionedKey( plugin );
        return pluginDescriptors.containsKey( key );
    }
    public PluginDescriptor getPluginDescriptorForPrefix( String prefix )
    {
        return (PluginDescriptor) pluginIdsByPrefix.get( prefix );
    }
    public void flushPluginDescriptor( Plugin plugin )
    {
        String key = PluginUtils.constructVersionedKey( plugin );
        pluginsInProcess.remove( key );
        pluginDescriptors.remove( key );
        for ( Iterator it = pluginIdsByPrefix.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) it.next();
            if ( key.equals( PluginUtils.constructVersionedKey( (PluginDescriptor) entry.getValue() ) ) )
            {
                it.remove();
            }
        }
    }
}
