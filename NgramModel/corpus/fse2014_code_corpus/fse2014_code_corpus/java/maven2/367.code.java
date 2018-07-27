package org.apache.maven.settings;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import java.io.File;
import java.io.IOException;
public interface MavenSettingsBuilder
{
    String ROLE = MavenSettingsBuilder.class.getName();
    String ALT_USER_SETTINGS_XML_LOCATION = "org.apache.maven.user-settings";
    String ALT_GLOBAL_SETTINGS_XML_LOCATION = "org.apache.maven.global-settings";
    String ALT_LOCAL_REPOSITORY_LOCATION = "maven.repo.local";
    Settings buildSettings()
        throws IOException, XmlPullParserException;
    Settings buildSettings( boolean useCachedSettings )
        throws IOException, XmlPullParserException;
    Settings buildSettings( File userSettingsFile )
        throws IOException, XmlPullParserException;
    Settings buildSettings( File userSettingsFile, boolean useCachedSettings )
        throws IOException, XmlPullParserException;
}
