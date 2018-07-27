package org.apache.maven.settings.merge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.settings.IdentifiableBase;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.StringUtils;
public class MavenSettingsMerger
{
    public void merge( Settings dominant, Settings recessive, String recessiveSourceLevel )
    {
        if ( dominant == null || recessive == null )
        {
            return;
        }
        recessive.setSourceLevel( recessiveSourceLevel );
        List<String> dominantActiveProfiles = dominant.getActiveProfiles();
        List<String> recessiveActiveProfiles = recessive.getActiveProfiles();
        if ( recessiveActiveProfiles != null )
        {
            if ( dominantActiveProfiles == null )
            {
                dominantActiveProfiles = new ArrayList<String>();
                dominant.setActiveProfiles( dominantActiveProfiles );
            }
            for ( String profileId : recessiveActiveProfiles )
            {
                if ( !dominantActiveProfiles.contains( profileId ) )
                {
                    dominantActiveProfiles.add( profileId );
                }
            }
        }
        List<String> dominantPluginGroupIds = dominant.getPluginGroups();
        List<String> recessivePluginGroupIds = recessive.getPluginGroups();
        if ( recessivePluginGroupIds != null )
        {
            if ( dominantPluginGroupIds == null )
            {
                dominantPluginGroupIds = new ArrayList<String>();
                dominant.setPluginGroups( dominantPluginGroupIds );
            }
            for ( String pluginGroupId : recessivePluginGroupIds )
            {
                if ( !dominantPluginGroupIds.contains( pluginGroupId ) )
                {
                    dominantPluginGroupIds.add( pluginGroupId );
                }
            }
        }
        if ( StringUtils.isEmpty( dominant.getLocalRepository() ) )
        {
            dominant.setLocalRepository( recessive.getLocalRepository() );
        }
        shallowMergeById( dominant.getMirrors(), recessive.getMirrors(), recessiveSourceLevel );
        shallowMergeById( dominant.getServers(), recessive.getServers(), recessiveSourceLevel );
        shallowMergeById( dominant.getProxies(), recessive.getProxies(), recessiveSourceLevel );
        shallowMergeById( dominant.getProfiles(), recessive.getProfiles(), recessiveSourceLevel );
    }
    private static <T extends IdentifiableBase> void shallowMergeById( List<T> dominant, List<T> recessive,
                                                                       String recessiveSourceLevel )
    {
        Map<String, T> dominantById = mapById( dominant );
        for ( T identifiable : recessive )
        {
            if ( !dominantById.containsKey( identifiable.getId() ) )
            {
                identifiable.setSourceLevel( recessiveSourceLevel );
                dominant.add( identifiable );
            }
        }
    }
    private static <T extends IdentifiableBase> Map<String, T> mapById( List<T> identifiables )
    {
        Map<String, T> byId = new HashMap<String, T>();
        for ( T identifiable : identifiables )
        {
            byId.put( identifiable.getId(), identifiable );
        }
        return byId;
    }
}
