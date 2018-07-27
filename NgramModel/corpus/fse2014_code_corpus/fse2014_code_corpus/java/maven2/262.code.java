package org.apache.maven.plugin.registry;
import java.io.File;
public class RuntimeInfo
{
    private File file;
    private String autoUpdateSourceLevel;
    private String updateIntervalSourceLevel;
    private final PluginRegistry registry;
    public RuntimeInfo( PluginRegistry registry )
    {
        this.registry = registry;
    }
    public String getAutoUpdateSourceLevel()
    {
        if ( autoUpdateSourceLevel == null )
        {
            return registry.getSourceLevel();
        }
        else
        {
            return autoUpdateSourceLevel;
        }
    }
    public void setAutoUpdateSourceLevel( String autoUpdateSourceLevel )
    {
        this.autoUpdateSourceLevel = autoUpdateSourceLevel;
    }
    public File getFile()
    {
        return file;
    }
    public void setFile( File file )
    {
        this.file = file;
    }
    public String getUpdateIntervalSourceLevel()
    {
        if ( updateIntervalSourceLevel == null )
        {
            return registry.getSourceLevel();
        }
        else
        {
            return updateIntervalSourceLevel;
        }
    }
    public void setUpdateIntervalSourceLevel( String updateIntervalSourceLevel )
    {
        this.updateIntervalSourceLevel = updateIntervalSourceLevel;
    }
}
