package org.apache.maven.settings;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
public class RuntimeInfo
{
    private File file;
    private Boolean pluginUpdateForced;
    private Boolean applyToAllPluginUpdates;
    private Map<String, String> activeProfileToSourceLevel = new HashMap<String, String>();
    private String localRepositorySourceLevel = TrackableBase.USER_LEVEL;
    private Map<String, String> pluginGroupIdSourceLevels = new HashMap<String, String>();
    private final Settings settings;
    public RuntimeInfo( Settings settings )
    {
        this.settings = settings;
    }
    public void setFile( File file )
    {
        this.file = file;
    }
    public File getFile()
    {
        return file;
    }
    public void setPluginUpdateOverride( Boolean pluginUpdateForced )
    {
        this.pluginUpdateForced = pluginUpdateForced;
    }
    public Boolean getPluginUpdateOverride()
    {
        return pluginUpdateForced;
    }
    public Boolean getApplyToAllPluginUpdates()
    {
        return applyToAllPluginUpdates;
    }
    public void setApplyToAllPluginUpdates( Boolean applyToAll )
    {
        this.applyToAllPluginUpdates = applyToAll;
    }
    public void setActiveProfileSourceLevel( String activeProfile, String sourceLevel )
    {
        activeProfileToSourceLevel.put( activeProfile, sourceLevel );
    }
    public String getSourceLevelForActiveProfile( String activeProfile )
    {
        String sourceLevel = (String) activeProfileToSourceLevel.get( activeProfile );
        if ( sourceLevel != null )
        {
            return sourceLevel;
        }
        else
        {
            return settings.getSourceLevel();
        }
    }
    public void setPluginGroupIdSourceLevel( String pluginGroupId, String sourceLevel )
    {
        pluginGroupIdSourceLevels.put( pluginGroupId, sourceLevel );
    }
    public String getSourceLevelForPluginGroupId( String pluginGroupId )
    {
        String sourceLevel = (String) pluginGroupIdSourceLevels.get( pluginGroupId );
        if ( sourceLevel != null )
        {
            return sourceLevel;
        }
        else
        {
            return settings.getSourceLevel();
        }
    }
    public void setLocalRepositorySourceLevel( String localRepoSourceLevel )
    {
        this.localRepositorySourceLevel = localRepoSourceLevel;
    }
    public String getLocalRepositorySourceLevel()
    {
        return localRepositorySourceLevel;
    }
}
