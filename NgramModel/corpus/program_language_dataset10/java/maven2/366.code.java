package org.apache.maven.settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.interpolation.EnvarBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
public class DefaultMavenSettingsBuilder
    extends AbstractLogEnabled
    implements MavenSettingsBuilder, Initializable
{
    public static final String userHome = System.getProperty( "user.home" );
    private String userSettingsPath;
    private String globalSettingsPath;
    private File userSettingsFile;
    private File globalSettingsFile;
    private Settings loadedSettings;
    public void initialize()
    {
        userSettingsFile =
            getFile( userSettingsPath, "user.home", MavenSettingsBuilder.ALT_USER_SETTINGS_XML_LOCATION );
        globalSettingsFile =
            getFile( globalSettingsPath, "maven.home", MavenSettingsBuilder.ALT_GLOBAL_SETTINGS_XML_LOCATION );
        getLogger().debug(
            "Building Maven global-level settings from: '" + globalSettingsFile.getAbsolutePath() + "'" );
        getLogger().debug( "Building Maven user-level settings from: '" + userSettingsFile.getAbsolutePath() + "'" );
    }
    private Settings readSettings( File settingsFile )
        throws IOException, XmlPullParserException
    {
        Settings settings = null;
        if ( settingsFile != null && settingsFile.exists() && settingsFile.isFile() )
        {
            Reader reader = null;
            try
            {
                reader = ReaderFactory.newXmlReader( settingsFile );
                StringWriter sWriter = new StringWriter();
                IOUtil.copy( reader, sWriter );
                String rawInput = sWriter.toString();
                try
                {
                    RegexBasedInterpolator interpolator = new RegexBasedInterpolator();
                    interpolator.addValueSource( new EnvarBasedValueSource() );
                    rawInput = interpolator.interpolate( rawInput, "settings" );
                }
                catch ( Exception e )
                {
                    getLogger().warn(
                        "Failed to initialize environment variable resolver. Skipping environment substitution in settings." );
                    getLogger().debug( "Failed to initialize envar resolver. Skipping resolution.", e );
                }
                StringReader sReader = new StringReader( rawInput );
                SettingsXpp3Reader modelReader = new SettingsXpp3Reader();
                settings = modelReader.read( sReader, true );
                RuntimeInfo rtInfo = new RuntimeInfo( settings );
                rtInfo.setFile( settingsFile );
                settings.setRuntimeInfo( rtInfo );
            }
            finally
            {
                IOUtil.close( reader );
            }
        }
        return settings;
    }
    public Settings buildSettings()
        throws IOException, XmlPullParserException
    {
        return buildSettings( userSettingsFile );
    }
    public Settings buildSettings( boolean useCachedSettings )
        throws IOException, XmlPullParserException
    {
        return buildSettings( userSettingsFile, useCachedSettings );
    }
    public Settings buildSettings( File userSettingsFile )
        throws IOException, XmlPullParserException
    {
        return buildSettings( userSettingsFile, true );
    }
    public Settings buildSettings( File userSettingsFile, boolean useCachedSettings )
        throws IOException, XmlPullParserException
    {
        if ( !useCachedSettings || loadedSettings == null )
        {
            Settings globalSettings = readSettings( globalSettingsFile );
            Settings userSettings = readSettings( userSettingsFile );
            if ( globalSettings == null )
            {
                globalSettings = new Settings();
            }
            if ( userSettings == null )
            {
                userSettings = new Settings();
                userSettings.setRuntimeInfo( new RuntimeInfo( userSettings ) );
            }
            SettingsUtils.merge( userSettings, globalSettings, TrackableBase.GLOBAL_LEVEL );
            activateDefaultProfiles( userSettings );
            setLocalRepository( userSettings );
            loadedSettings = userSettings;
        }
        return loadedSettings;
    }
    private void activateDefaultProfiles( Settings settings )
    {
        List<String> activeProfiles = settings.getActiveProfiles();
        for ( Profile profile : settings.getProfiles() )
        {
            if ( profile.getActivation() != null && profile.getActivation().isActiveByDefault()
                && !activeProfiles.contains( profile.getId() ) )
            {
                settings.addActiveProfile( profile.getId() );
            }
        }
    }
    private void setLocalRepository( Settings userSettings )
    {
        String localRepository = System.getProperty( MavenSettingsBuilder.ALT_LOCAL_REPOSITORY_LOCATION );
        if ( localRepository == null || localRepository.length() < 1 )
        {
            localRepository = userSettings.getLocalRepository();
        }
        if ( localRepository == null || localRepository.length() < 1 )
        {
            File mavenUserConfigurationDirectory = new File( userHome, ".m2" );
            if ( !mavenUserConfigurationDirectory.exists() )
            {
                if ( !mavenUserConfigurationDirectory.mkdirs() )
                {
                }
            }
            localRepository = new File( mavenUserConfigurationDirectory, "repository" ).getAbsolutePath();
        }
        File file = new File( localRepository );
        if ( !file.isAbsolute() && file.getPath().startsWith( File.separator ) )
        {
            localRepository = file.getAbsolutePath();
        }
        userSettings.setLocalRepository( localRepository );
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
