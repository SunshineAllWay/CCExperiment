package org.apache.maven.settings.building;
import java.io.File;
import java.util.Properties;
public interface SettingsBuildingRequest
{
    File getGlobalSettingsFile();
    SettingsBuildingRequest setGlobalSettingsFile( File globalSettingsFile );
    SettingsSource getGlobalSettingsSource();
    SettingsBuildingRequest setGlobalSettingsSource( SettingsSource globalSettingsSource );
    File getUserSettingsFile();
    SettingsBuildingRequest setUserSettingsFile( File userSettingsFile );
    SettingsSource getUserSettingsSource();
    SettingsBuildingRequest setUserSettingsSource( SettingsSource userSettingsSource );
    Properties getSystemProperties();
    SettingsBuildingRequest setSystemProperties( Properties systemProperties );
    Properties getUserProperties();
    SettingsBuildingRequest setUserProperties( Properties userProperties );
}
