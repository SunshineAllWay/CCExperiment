package org.apache.maven.settings;
import java.io.File;
import java.io.IOException;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
@Component( role = MavenSettingsBuilder.class )
public class DefaultMavenSettingsBuilder
    extends AbstractLogEnabled
    implements MavenSettingsBuilder
{
    @Requirement
    private SettingsBuilder settingsBuilder;
    public Settings buildSettings()
        throws IOException, XmlPullParserException
    {
        File userSettingsFile =
            getFile( "${user.home}/.m2/settings.xml", "user.home",
                     MavenSettingsBuilder.ALT_USER_SETTINGS_XML_LOCATION );
        return buildSettings( userSettingsFile );
    }
    public Settings buildSettings( boolean useCachedSettings )
        throws IOException, XmlPullParserException
    {
        return buildSettings();
    }
    public Settings buildSettings( File userSettingsFile )
        throws IOException, XmlPullParserException
    {
        File globalSettingsFile =
            getFile( "${maven.home}/conf/settings.xml", "maven.home",
                     MavenSettingsBuilder.ALT_GLOBAL_SETTINGS_XML_LOCATION );
        SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
        request.setUserSettingsFile( userSettingsFile );
        request.setGlobalSettingsFile( globalSettingsFile );
        request.setSystemProperties( System.getProperties() );
        return build( request );
    }
    public Settings buildSettings( File userSettingsFile, boolean useCachedSettings )
        throws IOException, XmlPullParserException
    {
        return buildSettings( userSettingsFile );
    }
    private Settings build( SettingsBuildingRequest request )
        throws IOException, XmlPullParserException
    {
        try
        {
            return settingsBuilder.build( request ).getEffectiveSettings();
        }
        catch ( SettingsBuildingException e )
        {
            throw (IOException) new IOException( e.getMessage() ).initCause( e );
        }
    }
    public Settings buildSettings( MavenExecutionRequest request )
        throws IOException, XmlPullParserException
    {
        SettingsBuildingRequest settingsRequest = new DefaultSettingsBuildingRequest();
        settingsRequest.setUserSettingsFile( request.getUserSettingsFile() );
        settingsRequest.setGlobalSettingsFile( request.getGlobalSettingsFile() );
        settingsRequest.setUserProperties( request.getUserProperties() );
        settingsRequest.setSystemProperties( request.getSystemProperties() );
        return build( settingsRequest );
    }
    private File getFile( String pathPattern, String basedirSysProp, String altLocationSysProp )
    {
        String path = System.getProperty( altLocationSysProp );
        if ( StringUtils.isEmpty( path ) )
        {
            String basedir = System.getProperty( basedirSysProp );
            if ( basedir == null )
            {
                basedir = System.getProperty( "user.dir" );
            }
            basedir = basedir.replaceAll( "\\\\", "/" );
            basedir = basedir.replaceAll( "\\$", "\\\\\\$" );
            path = pathPattern.replaceAll( "\\$\\{" + basedirSysProp + "\\}", basedir );
            path = path.replaceAll( "\\\\", "/" );
            return new File( path ).getAbsoluteFile();
        }
        else
        {
            return new File( path ).getAbsoluteFile();
        }
    }
}
