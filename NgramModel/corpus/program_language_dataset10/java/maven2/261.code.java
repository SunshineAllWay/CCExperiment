package org.apache.maven.plugin.registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
public final class PluginRegistryUtils
{
    private PluginRegistryUtils()
    {
    }
    public static void merge( PluginRegistry dominant, PluginRegistry recessive, String recessiveSourceLevel )
    {
        if ( dominant == null || recessive == null )
        {
            return;
        }
        RuntimeInfo dominantRtInfo = dominant.getRuntimeInfo();
        String dominantUpdateInterval = dominant.getUpdateInterval();
        if ( dominantUpdateInterval == null )
        {
            String recessiveUpdateInterval = recessive.getUpdateInterval();
            if ( recessiveUpdateInterval != null )
            {
                dominant.setUpdateInterval( recessiveUpdateInterval );
                dominantRtInfo.setUpdateIntervalSourceLevel( recessiveSourceLevel );
            }
        }
        String dominantAutoUpdate = dominant.getAutoUpdate();
        if ( dominantAutoUpdate == null )
        {
            String recessiveAutoUpdate = recessive.getAutoUpdate();
            if ( recessiveAutoUpdate != null )
            {
                dominant.setAutoUpdate( recessiveAutoUpdate );
                dominantRtInfo.setAutoUpdateSourceLevel( recessiveSourceLevel );
            }
        }
        List recessivePlugins = null;
        if ( recessive != null )
        {
            recessivePlugins = recessive.getPlugins();
        }
        else
        {
            recessivePlugins = Collections.EMPTY_LIST;
        }
        shallowMergePlugins( dominant, recessivePlugins, recessiveSourceLevel );
    }
    public static void recursivelySetSourceLevel( PluginRegistry pluginRegistry, String sourceLevel )
    {
        if ( pluginRegistry == null )
        {
            return;
        }
        pluginRegistry.setSourceLevel( sourceLevel );
        for ( Iterator it = pluginRegistry.getPlugins().iterator(); it.hasNext(); )
        {
            Plugin plugin = (Plugin) it.next();
            plugin.setSourceLevel( sourceLevel );
        }
    }
    private static void shallowMergePlugins( PluginRegistry dominant, List recessive, String recessiveSourceLevel )
    {
        Map dominantByKey = dominant.getPluginsByKey();
        List dominantPlugins = dominant.getPlugins();
        for ( Iterator it = recessive.iterator(); it.hasNext(); )
        {
            Plugin recessivePlugin = (Plugin) it.next();
            if ( !dominantByKey.containsKey( recessivePlugin.getKey() ) )
            {
                recessivePlugin.setSourceLevel( recessiveSourceLevel );
                dominantPlugins.add( recessivePlugin );
            }
        }
        dominant.flushPluginsByKey();
    }
    public static PluginRegistry extractUserPluginRegistry( PluginRegistry pluginRegistry )
    {
        PluginRegistry userRegistry = null;
        if ( pluginRegistry != null && !PluginRegistry.GLOBAL_LEVEL.equals( pluginRegistry.getSourceLevel() ) )
        {
            userRegistry = new PluginRegistry();
            RuntimeInfo rtInfo = new RuntimeInfo( userRegistry );
            userRegistry.setRuntimeInfo( rtInfo );
            RuntimeInfo oldRtInfo = pluginRegistry.getRuntimeInfo();
            if ( TrackableBase.USER_LEVEL.equals( oldRtInfo.getAutoUpdateSourceLevel() ) )
            {
                userRegistry.setAutoUpdate( pluginRegistry.getAutoUpdate() );
            }
            if ( TrackableBase.USER_LEVEL.equals( oldRtInfo.getUpdateIntervalSourceLevel() ) )
            {
                userRegistry.setUpdateInterval( pluginRegistry.getUpdateInterval() );
            }
            List plugins = new ArrayList();
            for ( Iterator it = pluginRegistry.getPlugins().iterator(); it.hasNext(); )
            {
                Plugin plugin = (Plugin) it.next();
                if ( TrackableBase.USER_LEVEL.equals( plugin.getSourceLevel() ) )
                {
                    plugins.add( plugin );
                }
            }
            userRegistry.setPlugins( plugins );
            rtInfo.setFile( pluginRegistry.getRuntimeInfo().getFile() );
        }
        return userRegistry;
    }
}
