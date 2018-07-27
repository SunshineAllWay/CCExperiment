package org.apache.maven.settings.building;
public interface SettingsBuilder
{
    SettingsBuildingResult build( SettingsBuildingRequest request )
        throws SettingsBuildingException;
}
