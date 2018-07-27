package org.apache.maven.plugin.registry;
import org.apache.maven.plugin.registry.io.xpp3.PluginRegistryXpp3Reader;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
public class DefaultPluginRegistryBuilder
    extends AbstractLogEnabled
    implements MavenPluginRegistryBuilder, Initializable
{
    public static final String userHome = System.getProperty( "user.home" );
    private String userRegistryPath;
    private String globalRegistryPath;
    private File userRegistryFile;
    private File globalRegistryFile;
    public void initialize()
    {
        userRegistryFile = getFile( userRegistryPath, "user.home", MavenPluginRegistryBuilder.ALT_USER_PLUGIN_REG_LOCATION );
        getLogger().debug( "Building Maven user-level plugin registry from: '" + userRegistryFile.getAbsolutePath() + "'" );
        if ( System.getProperty( "maven.home" ) != null
             || System.getProperty( MavenPluginRegistryBuilder.ALT_GLOBAL_PLUGIN_REG_LOCATION ) != null )
        {
            globalRegistryFile = getFile( globalRegistryPath, "maven.home", MavenPluginRegistryBuilder.ALT_GLOBAL_PLUGIN_REG_LOCATION );
            getLogger().debug( "Building Maven global-level plugin registry from: '" + globalRegistryFile.getAbsolutePath() + "'" );
        }
    }
    public PluginRegistry buildPluginRegistry()
        throws IOException, XmlPullParserException
    {
        PluginRegistry global = readPluginRegistry( globalRegistryFile );
        PluginRegistry user = readPluginRegistry( userRegistryFile );
        if ( user == null && global != null )
        {
            PluginRegistryUtils.recursivelySetSourceLevel( global, PluginRegistry.GLOBAL_LEVEL );
            user = global;
        }
        else
        {
            PluginRegistryUtils.merge( user, global, TrackableBase.GLOBAL_LEVEL );
        }
        return user;
    }
    private PluginRegistry readPluginRegistry( File registryFile )
        throws IOException, XmlPullParserException
    {
        PluginRegistry registry = null;
        if ( registryFile != null && registryFile.exists() && registryFile.isFile() )
        {
            Reader reader = null;
            try
            {
                reader = ReaderFactory.newXmlReader( registryFile );
                PluginRegistryXpp3Reader modelReader = new PluginRegistryXpp3Reader();
                registry = modelReader.read( reader );
                RuntimeInfo rtInfo = new RuntimeInfo( registry );
                registry.setRuntimeInfo( rtInfo );
                rtInfo.setFile( registryFile );
            }
            finally
            {
                IOUtil.close( reader );
            }
        }
        return registry;
    }
    private File getFile( String pathPattern, String basedirSysProp, String altLocationSysProp )
    {
        String path = System.getProperty( altLocationSysProp );
        if ( StringUtils.isEmpty( path ) )
        {
            String basedir = System.getProperty( basedirSysProp );
            basedir = basedir.replaceAll( "\\\\", "/" );
            basedir = basedir.replaceAll( "\\$", "\\\\\\$" );
            path = pathPattern.replaceAll( "\\$\\{" + basedirSysProp + "\\}", basedir );
            path = path.replaceAll( "\\\\", "/" );
            path = path.replaceAll( "//", "/" );
            return new File( path ).getAbsoluteFile();
        }
        else
        {
            return new File( path ).getAbsoluteFile();
        }
    }
    public PluginRegistry createUserPluginRegistry()
    {
        PluginRegistry registry = new PluginRegistry();
        RuntimeInfo rtInfo = new RuntimeInfo( registry );
        registry.setRuntimeInfo( rtInfo );
        rtInfo.setFile( userRegistryFile );
        return registry;
    }
}
